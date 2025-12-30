package com.campus.water.service;

import com.campus.water.entity.Admin;
import com.campus.water.entity.RepairerAuth;
import com.campus.water.entity.Repairman;
import com.campus.water.entity.User;
import com.campus.water.entity.dto.request.RegisterRequest;
import com.campus.water.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RegisterService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RepairerAuthRepository repairerAuthRepository;

    // 新增注入RepairmanRepository
    @Autowired
    private RepairmanRepository repairmanRepository;

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
        admin.setPassword(password);
        admin.setPhone(request.getPhone());
        admin.setRole(Admin.AdminRole.valueOf("ROLE_" + request.getRole().toUpperCase()));
        admin.setCreatedTime(LocalDateTime.now());
        admin.setUpdatedTime(LocalDateTime.now());

        // 核心修改1：添加区域ID赋值（从请求中获取，允许为null/空，实现选填）
        admin.setAreaId(request.getAreaId());

        // 核心修改2：区域管理员若填写了areaId，则校验区域是否存在；不填则不强制（实现选填）
        Admin.AdminRole adminRole = admin.getRole();
        if (adminRole == Admin.AdminRole.ROLE_AREA_ADMIN && request.getAreaId() != null && !request.getAreaId().trim().isEmpty()) {
            // 此处需要注入AreaRepository（与AdminService保持一致），先补充注入
            if (!areaRepository.existsById(request.getAreaId().trim())) {
                throw new RuntimeException("关联的区域不存在：" + request.getAreaId().trim());
            }
        }

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
        // 1. 校验维修人员认证信息唯一性
        if (repairerAuthRepository.existsByUsername(username)) {
            throw new RuntimeException("维修人员用户名已存在");
        }
        if (repairerAuthRepository.existsByRepairmanId(request.getRepairmanId())) {
            throw new RuntimeException("维修人员ID已被注册");
        }

        // 2. 保存维修人员认证信息（RepairerAuth表）
        RepairerAuth repairerAuth = new RepairerAuth();
        repairerAuth.setUsername(username);
        repairerAuth.setPassword(password);
        repairerAuth.setRepairmanId(request.getRepairmanId());
        repairerAuth.setAccountStatus(RepairerAuth.AccountStatus.active);
        repairerAuthRepository.save(repairerAuth);

        // 3. 保存维修人员基本信息（Repairman表）
        Repairman repairman = new Repairman();
        repairman.setRepairmanId(request.getRepairmanId()); // 与认证表关联的ID
        repairman.setRepairmanName(request.getRepairmanName()); // 从请求获取姓名
        repairman.setPhone(request.getPhone()); // 从请求获取手机号
        repairman.setAreaId(request.getAreaId()); // 从请求获取负责区域
        repairman.setSkills(request.getSkills()); // 从请求获取技能描述
        // 其他字段使用默认值：状态默认idle，工作量默认0，评分默认null
        repairmanRepository.save(repairman);
    }
}