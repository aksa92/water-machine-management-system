package com.campus.water.service;

import com.campus.water.entity.Admin;
import com.campus.water.entity.po.RepairerAuthPO;
import com.campus.water.entity.po.UserPO;
import com.campus.water.entity.vo.LoginVO;
import com.campus.water.mapper.AdminRepository;
import com.campus.water.mapper.RepairerAuthRepository;
import com.campus.water.mapper.UserRepository;
import com.campus.water.entity.dto.request.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LoginService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RepairerAuthRepository repairerAuthRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // 使用Security配置的密码加密器

    public LoginVO login(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword(); // 不再手动MD5加密，使用Security的加密器
        String userType = loginRequest.getUserType();

        switch (userType) {
            case "admin":
                return handleAdminLogin(username, password);
            case "user":
                return handleUserLogin(username, password);
            case "repairer":
                return handleRepairerLogin(username, password);
            default:
                throw new RuntimeException("无效的用户类型：" + userType);
        }
    }

    // 管理员登录
    private LoginVO handleAdminLogin(String username, String password) {
        Admin admin = adminRepository.findByAdminName(username)
                .orElseThrow(() -> new RuntimeException("管理员不存在"));

        // 使用密码加密器验证密码
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        return createLoginVO(admin.getAdminId(), username, "admin");
    }

    // 学生登录
    private LoginVO handleUserLogin(String username, String password) {
        UserPO user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        return createLoginVO(user.getStudentId(), username, "user");
    }

    // 维修人员登录
    private LoginVO handleRepairerLogin(String username, String password) {
        RepairerAuthPO repairer = repairerAuthRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("维修人员不存在"));

        if (!passwordEncoder.matches(password, repairer.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        return createLoginVO(repairer.getRepairmanId(), username, "repairer");
    }

    // 构建登录响应VO
    private LoginVO createLoginVO(String userId, String username, String userType) {
        LoginVO vo = new LoginVO();
        vo.setUserId(userId);
        vo.setUsername(username);
        vo.setUserType(userType);
        // 后续应替换为JWT生成逻辑
        vo.setToken(UUID.randomUUID().toString().replace("-", ""));
        return vo;
    }
}