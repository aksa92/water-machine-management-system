package com.campus.water.service;

import com.campus.water.entity.Admin;
import com.campus.water.entity.po.RepairerAuthPO;
import com.campus.water.entity.po.UserPO;
import com.campus.water.entity.vo.LoginVO;
import com.campus.water.mapper.AdminRepository;
import com.campus.water.mapper.RepairerAuthRepository;
import com.campus.water.mapper.UserRepository;
import com.campus.water.entity.dto.request.LoginRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor // 自动生成构造函数（需要Lombok依赖）
public class LoginService {

    // 依赖注入：通过构造函数初始化（@RequiredArgsConstructor自动生成构造函数）
    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final RepairerAuthRepository repairerAuthRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginVO login(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        String userType = loginRequest.getUserType();

        // 增强版switch（解决"Switch语句可替换为增强的switch"警告）
        return switch (userType) {
            case "admin" -> handleAdminLogin(username, password);
            case "user" -> handleUserLogin(username, password);
            case "repairer" -> handleRepairerLogin(username, password);
            default -> throw new RuntimeException("无效的用户类型：" + userType);
        };
    }

    private LoginVO handleAdminLogin(String username, String password) {
        Admin admin = adminRepository.findByAdminName(username)
                .orElseThrow(() -> new RuntimeException("管理员不存在"));

        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        return createLoginVO(admin.getAdminId(), username, "admin");
    }

    private LoginVO handleUserLogin(String username, String password) {
        UserPO user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        return createLoginVO(user.getStudentId(), username, "user");
    }

    private LoginVO handleRepairerLogin(String username, String password) {
        RepairerAuthPO repairer = repairerAuthRepository.findByUsername(username)
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