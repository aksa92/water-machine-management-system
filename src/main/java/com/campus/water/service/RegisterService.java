package com.campus.water.service;

import com.campus.water.entity.Admin;
import com.campus.water.entity.RepairerAuth;
import com.campus.water.entity.User;
import com.campus.water.entity.dto.request.RegisterRequest;
import com.campus.water.mapper.AdminRepository;
import com.campus.water.mapper.RepairerAuthRepository;
import com.campus.water.mapper.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RegisterService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RepairerAuthRepository repairerAuthRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean register(RegisterRequest request) {
        String username = request.getUsername();
        // 使用BCrypt加密密码
        String encryptedPwd = passwordEncoder.encode(request.getPassword());
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

    private void handleAdminRegister(String username, String password, RegisterRequest request) {
        if (adminRepository.existsByAdminName(username)) {
            throw new RuntimeException("管理员用户名已存在");
        }
        if (adminRepository.existsByAdminId(request.getAdminId())) {
            throw new RuntimeException("管理员ID已存在");
        }
        if (request.getPhone() != null && adminRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("手机号已被注册");
        }

        Admin admin = new Admin();
        admin.setAdminId(request.getAdminId());
        admin.setAdminName(username);
        admin.setPassword(password); // 使用BCrypt加密后的密码
        admin.setPhone(request.getPhone());
        admin.setRole(Admin.AdminRole.valueOf("ROLE_" + request.getRole().toUpperCase()));
        admin.setCreatedTime(LocalDateTime.now());
        admin.setUpdatedTime(LocalDateTime.now());

        adminRepository.save(admin);
    }

    private void handleUserRegister(String studentName, String password, RegisterRequest request) {
        if (userRepository.existsByStudentName(studentName)) {
            throw new RuntimeException("用户名已存在");
        }
        if (userRepository.existsByStudentId(request.getStudentId())) {
            throw new RuntimeException("学号已被注册");
        }

        User user = new User();
        user.setPassword(password); // 使用BCrypt加密后的密码
        user.setStudentId(request.getStudentId());
        user.setStudentName(request.getStudentName());
        user.setPhone(request.getPhone());
        user.setStatus(User.UserStatus.active);

        userRepository.save(user);
    }

    private void handleRepairmanRegister(String username, String password, RegisterRequest request) {
        if (repairerAuthRepository.existsByUsername(username)) {
            throw new RuntimeException("维修人员用户名已存在");
        }
        if (repairerAuthRepository.existsByRepairmanId(request.getRepairmanId())) {
            throw new RuntimeException("维修人员ID已被注册");
        }

        RepairerAuth repairman = new RepairerAuth();
        repairman.setUsername(username);
        repairman.setPassword(password);// 使用BCrypt加密后的密码
        repairman.setRepairmanId(request.getRepairmanId());
        repairman.setAccountStatus(RepairerAuth.AccountStatus.active);

        repairerAuthRepository.save(repairman);
    }
}