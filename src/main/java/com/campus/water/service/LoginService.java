// filePath：main/java/com/campus/water/service/LoginService.java
package com.campus.water.service;

import com.campus.water.entity.Admin;
import com.campus.water.entity.RepairerAuth;
import com.campus.water.entity.User; // 引入User实体类
import com.campus.water.entity.vo.LoginVO;
import com.campus.water.mapper.AdminRepository;
import com.campus.water.mapper.RepairerAuthRepository;
import com.campus.water.mapper.UserRepository;
import com.campus.water.entity.dto.request.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final RepairerAuthRepository repairerAuthRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginVO login(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        String userType = loginRequest.getUserType();

        return switch (userType) {
            case "admin" -> handleAdminLogin(username, password);
            case "user" -> handleUserLogin(username, password);
            case "repairer" -> handleRepairerLogin(username, password);
            default -> throw new RuntimeException("无效的用户类型：" + userType);
        };
    }

   /* private LoginVO handleAdminLogin(String username, String password) {
        Admin admin = adminRepository.findByAdminName(username)
                .orElseThrow(() -> new RuntimeException("管理员不存在"));

        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        return createLoginVO(admin.getAdminId(), username, "admin");
    }*/
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
        // 改为查询User实体，使用studentName字段匹配用户名
        User user = userRepository.findByStudentName(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 验证密码（User的password字段与UserPO一致）
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 使用User的studentId作为用户ID
        return createLoginVO(user.getStudentId(), username, "user");
    }

    private LoginVO handleRepairerLogin(String username, String password) {
        // 此处将RepairerAuthPO改为RepairerAuth
        RepairerAuth repairer = repairerAuthRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("维修人员不存在"));

        if (!passwordEncoder.matches(password, repairer.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        return createLoginVO(repairer.getRepairmanId(), username, "repairer");
    }

    private LoginVO createLoginVO(String userId, String username, String userType) {
        LoginVO vo = new LoginVO();
        vo.setUserId(userId);
        vo.setUsername(username);
        vo.setUserType(userType);
        vo.setToken(UUID.randomUUID().toString().replace("-", ""));
        return vo;
    }
}