package com.campus.water.service;

import com.campus.water.entity.Admin;
import com.campus.water.entity.RepairerAuth; // 改为entity包下的RepairerAuth
import com.campus.water.entity.User;
import com.campus.water.entity.dto.request.RegisterRequest;
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
        // 使用与MD5PasswordEncoder相同的加密逻辑（UTF-8编码）
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
            case "repairman":
                handleRepairmanRegister(username, encryptedPwd, request);
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
    private void handleUserRegister(String studentName, String password, RegisterRequest request) {
        // 检查用户名和学号是否已存在（保持不变）
        if (userRepository.existsByStudentName(studentName)) {
            throw new RuntimeException("用户名已存在");
        }
        if (userRepository.existsByStudentId(request.getStudentId())) {
            throw new RuntimeException("学号已被注册");
        }

        // 创建 User 实体对象（而非 UserPO）
        User user = new User();
        user.setPassword(password); // 设置密码
        user.setStudentId(request.getStudentId()); // 设置学号
        user.setStudentName(request.getStudentName()); // 设置学生姓名
        user.setStatus(User.UserStatus.active); // 设置状态（使用 User 类的枚举）

        // 保存 User 实体（与 UserRepository 类型匹配）
        userRepository.save(user);
    }

    // 维修人员注册逻辑保持不变
    private void handleRepairmanRegister(String username, String password, RegisterRequest request) {
        if (repairerAuthRepository.existsByUsername(username)) {
            throw new RuntimeException("维修人员用户名已存在");
        }
        if (repairerAuthRepository.existsByRepairmanId(request.getRepairmanId())) {
            throw new RuntimeException("维修人员ID已被注册");
        }

        RepairerAuth repairman = new RepairerAuth();
        repairman.setUsername(username);
        repairman.setPassword(password);
        repairman.setRepairmanId(request.getRepairmanId());
        repairman.setAccountStatus(RepairerAuth.AccountStatus.active);

        repairerAuthRepository.save(repairman);
    }
}