package com.campus.water.service;

import com.campus.water.entity.dto.request.RegisterRequest;
import com.campus.water.entity.po.AdminPO;
import com.campus.water.entity.po.RepairerAuthPO;
import com.campus.water.entity.po.UserPO;
import com.campus.water.mapper.AdminRepository;
import com.campus.water.mapper.RepairerAuthRepository;
import com.campus.water.mapper.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;

@Service
public class RegisterService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RepairerAuthRepository repairerAuthRepository;

    public boolean register(RegisterRequest request) {
        String username = request.getUsername();
        // 密码MD5加密（与登录逻辑保持一致）
        String encryptedPwd = DigestUtils.md5DigestAsHex(
                request.getPassword().getBytes(StandardCharsets.UTF_8)
        );
        String userType = request.getUserType();

        switch (userType) {
            case "admin":
                handleAdminRegister(username, encryptedPwd, request);
                break;
            case "user":
                handleUserRegister(username, encryptedPwd, request);
                break;
            case "repairer":
                handleRepairerRegister(username, encryptedPwd, request);
                break;
            default:
                throw new RuntimeException("无效的用户类型：" + userType);
        }
        return true;
    }

    private void handleAdminRegister(String username, String password, RegisterRequest request) {
        // 检查用户名是否已存在
        if (adminRepository.existsByUsername(username)) {
            throw new RuntimeException("管理员用户名已存在");
        }

        AdminPO admin = new AdminPO();
        admin.setUsername(username);
        admin.setPassword(password);
        admin.setAdminId(request.getAdminId());
        admin.setRole(AdminPO.AdminRole.valueOf(request.getAdminRole()));
        admin.setStatus(AdminPO.AdminStatus.ACTIVE);

        adminRepository.save(admin);
    }

    private void handleUserRegister(String username, String password, RegisterRequest request) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("用户名已存在");
        }
        if (userRepository.existsByStudentId(request.getStudentId())) {
            throw new RuntimeException("学号已被注册");
        }

        UserPO user = new UserPO();
        user.setUsername(username);
        user.setPassword(password);
        user.setStudentId(request.getStudentId());
        user.setUsername(request.getStudentName());
        user.setStatus(UserPO.UserStatus.ACTIVE);
        // 可根据需要补充其他字段默认值

        userRepository.save(user);
    }

    private void handleRepairerRegister(String username, String password, RegisterRequest request) {
        if (repairerAuthRepository.existsByUsername(username)) {
            throw new RuntimeException("维修人员用户名已存在");
        }
        if (repairerAuthRepository.existsByRepairmanId(request.getRepairmanId())) {
            throw new RuntimeException("维修人员ID已被注册");
        }

        RepairerAuthPO repairer = new RepairerAuthPO();
        repairer.setUsername(username);
        repairer.setPassword(password);
        repairer.setRepairmanId(request.getRepairmanId());
        repairer.setAccountStatus(RepairerAuthPO.AccountStatus.active);

        repairerAuthRepository.save(repairer);
    }
}