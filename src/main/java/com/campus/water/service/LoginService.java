// filePath：main/java/com/campus/water/service/LoginService.java
package com.campus.water.service;

import com.campus.water.entity.Admin;
import com.campus.water.entity.RepairerAuth;
import com.campus.water.entity.User;
import com.campus.water.entity.vo.LoginVO;
import com.campus.water.mapper.AdminRepository;
import com.campus.water.mapper.RepairerAuthRepository;
import com.campus.water.mapper.UserRepository;
import com.campus.water.entity.dto.request.LoginRequest;
import com.campus.water.security.RoleConstants;
import com.campus.water.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final RepairerAuthRepository repairerAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginVO login(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        String userType = loginRequest.getUserType();

        return switch (userType) {
            case "admin" -> handleAdminLogin(username, password);
            case "user" -> handleUserLogin(username, password);
            case "repairman" -> handleRepairmanLogin(username, password);
            default -> throw new RuntimeException("无效的用户类型：" + userType);
        };
    }

    private LoginVO handleAdminLogin(String username, String password) {
        Admin admin = adminRepository.findByAdminName(username)
                .orElseThrow(() -> new RuntimeException("管理员不存在"));

        boolean matches;
        // 临时支持 MD5 验证（仅用于测试环境）
        if (admin.getPassword().startsWith("$2a$") || admin.getPassword().startsWith("$2y$")) {
            // BCrypt 格式密码
            matches = passwordEncoder.matches(password, admin.getPassword());
        } else {
            // MD5 格式密码
            String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
            matches = md5Password.equals(admin.getPassword());
        }

        if (!matches) {
            throw new RuntimeException("密码错误");
        }

        return createLoginVO(admin.getAdminId(), username, "admin");
    }

    private LoginVO handleUserLogin(String username, String password) {
        User user = userRepository.findByStudentName(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        return createLoginVO(user.getStudentId(), username, "user");
    }

    private LoginVO handleRepairmanLogin(String username, String password) {
        RepairerAuth repairer = repairerAuthRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("维修人员不存在"));

        if (!passwordEncoder.matches(password, repairer.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        return createLoginVO(repairer.getRepairmanId(), username, "repairman");
    }

    /**
     * 生成包含JWT令牌和角色信息的登录响应
     * 角色映射：
     * - admin -> ROLE_ADMIN
     * - user -> ROLE_STUDENT
     * - repairman -> ROLE_REPAIRMAN
     */
    private LoginVO createLoginVO(String userId, String username, String userType) {
        LoginVO vo = new LoginVO();
        vo.setUserId(userId);
        vo.setUsername(username);
        vo.setUserType(userType);

        // 根据用户类型获取对应的角色
        String role = switch (userType) {
            case "admin" -> RoleConstants.ROLE_ADMIN;
            case "user" -> RoleConstants.ROLE_STUDENT;
            case "repairman" -> RoleConstants.ROLE_REPAIRMAN;
            default -> throw new RuntimeException("不支持的用户类型：" + userType);
        };

        // 使用JWT生成包含角色信息的令牌
        vo.setToken(jwtTokenProvider.generateToken(username, role));
        return vo;
    }
}