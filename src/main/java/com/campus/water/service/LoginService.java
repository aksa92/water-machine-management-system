package com.campus.water.service;

import com.campus.water.entity.dto.request.LoginRequest; // 对齐你的DTO目录
import com.campus.water.entity.po.AdminPO;
import com.campus.water.entity.po.RepairerAuthPO;
import com.campus.water.entity.po.UserPO;
import com.campus.water.entity.vo.LoginVO;
import com.campus.water.repository.AdminRepository;
import com.campus.water.repository.RepairerAuthRepository;
import com.campus.water.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
public class LoginService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RepairerAuthRepository repairerAuthRepository;

    public LoginVO login(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        // 密码MD5加密（和数据库存储的一致）
        String encryptedPwd = DigestUtils.md5DigestAsHex(
                loginRequest.getPassword().getBytes(StandardCharsets.UTF_8)
        );
        String userType = loginRequest.getUserType();

        switch (userType) {
            case "admin":
                return handleAdminLogin(username, encryptedPwd);
            case "user":
                return handleUserLogin(username, encryptedPwd);
            case "repairer":
                return handleRepairerLogin(username, encryptedPwd);
            default:
                throw new RuntimeException("无效的用户类型：" + userType);
        }
    }

    // 管理员登录（复用JPA的findByUsername）
    private LoginVO handleAdminLogin(String username, String password) {
        AdminPO admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("管理员不存在"));
        if (!admin.getPassword().equals(password)) {
            throw new RuntimeException("密码错误");
        }
        return createLoginVO(admin.getAdminId(), username, "admin");
    }

    // 学生登录
    private LoginVO handleUserLogin(String username, String password) {
        UserPO user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("密码错误");
        }
        return createLoginVO(user.getStudentId(), username, "user");
    }

    // 维修人员登录
    private LoginVO handleRepairerLogin(String username, String password) {
        RepairerAuthPO repairer = repairerAuthRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("维修人员不存在"));
        if (!repairer.getPassword().equals(password)) {
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
        // 临时Token（后续可替换为JWT）
        vo.setToken(UUID.randomUUID().toString().replace("-", ""));
        return vo;
    }
}