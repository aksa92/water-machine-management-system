package com.campus.water.service;

import com.campus.water.entity.Admin; // 替换旧的AdminPO
import com.campus.water.entity.dto.request.RegisterRequest;
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

    // 修正管理员注册逻辑（适配新实体Admin）
    private void handleAdminRegister(String username, String password, RegisterRequest request) {
        // 检查用户名是否已存在（使用新方法existsByAdminName）
        if (adminRepository.existsByAdminName(username)) {
            throw new RuntimeException("管理员用户名已存在");
        }

        Admin admin = new Admin();
        admin.setAdminId(request.getAdminId());
        admin.setAdminName(username); // 字段名从username改为adminName
        admin.setPassword(password);
        // 角色枚举值转换（新实体角色为小写，需统一）
        admin.setRole(Admin.AdminRole.valueOf(request.getAdminRole().toLowerCase()));
        admin.setStatus(Admin.AdminStatus.active); // 状态枚举值改为小写

        adminRepository.save(admin);
    }

    // 学生注册逻辑保持不变
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
        user.setUsername(request.getStudentName()); // 注意：这里重复设置了username，建议修正为setStudentName（如果有该字段）
        // 枚举值改为小写（与UserPO中定义的一致）
        user.setStatus(UserPO.UserStatus.active);

        userRepository.save(user);
    }

    // 维修人员注册逻辑保持不变
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