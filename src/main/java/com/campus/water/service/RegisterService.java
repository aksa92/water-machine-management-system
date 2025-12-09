package com.campus.water.service;

import com.campus.water.entity.Admin;
import com.campus.water.entity.RepairerAuth; // 改为entity包下的RepairerAuth
import com.campus.water.entity.User;
import com.campus.water.entity.dto.request.RegisterRequest;
import com.campus.water.mapper.AdminRepository;
import com.campus.water.mapper.RepairerAuthRepository;
import com.campus.water.mapper.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

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

    // RegisterService中handleAdminRegister方法修改
    private void handleAdminRegister(String username, String password, RegisterRequest request) {
        // 检查用户名/ID/手机号是否已存在
        if (adminRepository.existsByAdminName(username)) {
            throw new RuntimeException("管理员用户名已存在");
        }
        if (adminRepository.existsByAdminId(request.getAdminId())) {
            throw new RuntimeException("管理员ID已存在");
        }
        if (request.getPhone() != null && adminRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("手机号已被注册");
        }

        // 构建管理员对象，支持指定角色（需从request中接收role参数）
        Admin admin = new Admin();
        admin.setAdminId(request.getAdminId());
        admin.setAdminName(username);
        admin.setPassword(BCrypt.hashpw(password, BCrypt.gensalt())); // 密码加密
        admin.setPhone(request.getPhone());
        // 从注册请求中获取角色（需在RegisterRequest添加role字段）
        admin.setRole(Admin.AdminRole.valueOf("ROLE_" + request.getRole().toUpperCase()));
        admin.setCreatedTime(LocalDateTime.now());
        admin.setUpdatedTime(LocalDateTime.now());

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
        user.setPhone(request.getPhone()); // 新增：保存手机号
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