package com.campus.water.service;

import com.campus.water.entity.Admin;
import com.campus.water.entity.RepairerAuth;
import com.campus.water.entity.User;
import com.campus.water.entity.dto.request.RegisterRequest;
import com.campus.water.mapper.AdminRepository;
import com.campus.water.mapper.RepairerAuthRepository;
import com.campus.water.mapper.UserRepository;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RegisterService {

    // 允许的管理员角色列表（无前缀，用于校验前端传递的值）
    private static final List<String> ALLOWED_ADMIN_ROLES = Arrays.stream(Admin.AdminRole.values())
            .map(role -> role.name().replace("ROLE_", "").toLowerCase())
            .collect(Collectors.toList());

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final RepairerAuthRepository repairerAuthRepository;

    // 构造器注入（Spring 5+ 可省略 @Autowired）
    public RegisterService(
            AdminRepository adminRepository,
            UserRepository userRepository,
            RepairerAuthRepository repairerAuthRepository) {
        this.adminRepository = adminRepository;
        this.userRepository = userRepository;
        this.repairerAuthRepository = repairerAuthRepository;
    }

    public boolean register(RegisterRequest request) {
        String username = request.getUsername();
        // 使用 MD5 预处理后再进行 BCrypt 加密（根据实际加密策略调整）
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

    private void handleAdminRegister(String username, String password, RegisterRequest request) {
        // 基础校验
        if (adminRepository.existsByAdminName(username)) {
            throw new RuntimeException("管理员用户名已存在");
        }
        if (adminRepository.existsByAdminId(request.getAdminId())) {
            throw new RuntimeException("管理员ID已存在");
        }
        if (request.getPhone() != null && adminRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("手机号已被注册");
        }

        // 角色参数校验与转换
        String rawRole = request.getRole();
        if (rawRole == null || rawRole.trim().isEmpty()) {
            throw new RuntimeException("管理员角色不能为空");
        }
        String cleanedRole = rawRole.trim().toLowerCase();
        if (!ALLOWED_ADMIN_ROLES.contains(cleanedRole)) {
            throw new RuntimeException("无效的管理员角色：" + rawRole + "，允许的值：" + ALLOWED_ADMIN_ROLES);
        }

        // 构建角色枚举值
        Admin.AdminRole adminRole;
        try {
            adminRole = Admin.AdminRole.valueOf("ROLE_" + cleanedRole.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("角色转换失败：" + rawRole, e);
        }

        // 构建并保存管理员对象
        Admin admin = new Admin();
        admin.setAdminId(request.getAdminId());
        admin.setAdminName(username);
        admin.setPassword(BCrypt.hashpw(password, BCrypt.gensalt())); // BCrypt 加密
        admin.setPhone(request.getPhone());
        admin.setRole(adminRole);
        admin.setCreatedTime(LocalDateTime.now());
        admin.setUpdatedTime(LocalDateTime.now());

        adminRepository.save(admin);
    }

    private void handleUserRegister(String studentName, String password, RegisterRequest request) {
        // 学生注册校验
        if (userRepository.existsByStudentName(studentName)) {
            throw new RuntimeException("用户名已存在");
        }
        if (userRepository.existsByStudentId(request.getStudentId())) {
            throw new RuntimeException("学号已被注册");
        }

        // 构建并保存用户对象
        User user = new User();
        user.setPassword(password);
        user.setStudentId(request.getStudentId());
        user.setStudentName(request.getStudentName());
        user.setPhone(request.getPhone());
        user.setStatus(User.UserStatus.active);

        userRepository.save(user);
    }

    private void handleRepairmanRegister(String username, String password, RegisterRequest request) {
        // 维修人员注册校验
        if (repairerAuthRepository.existsByUsername(username)) {
            throw new RuntimeException("维修人员用户名已存在");
        }
        if (repairerAuthRepository.existsByRepairmanId(request.getRepairmanId())) {
            throw new RuntimeException("维修人员ID已被注册");
        }

        // 构建并保存维修人员对象
        RepairerAuth repairman = new RepairerAuth();
        repairman.setUsername(username);
        repairman.setPassword(password); // 建议统一改为 BCrypt 加密
        repairman.setRepairmanId(request.getRepairmanId());
        repairman.setAccountStatus(RepairerAuth.AccountStatus.active);

        repairerAuthRepository.save(repairman);
    }
}