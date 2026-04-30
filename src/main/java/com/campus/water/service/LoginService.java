package com.campus.water.service;

import com.campus.water.entity.Admin;
import com.campus.water.entity.RepairerAuth;
import com.campus.water.entity.Repairman;
import com.campus.water.entity.User;
import com.campus.water.entity.vo.LoginVO;
import com.campus.water.Repository.AdminRepository;
import com.campus.water.Repository.RepairerAuthRepository;
import com.campus.water.Repository.RepairmanRepository;
import com.campus.water.Repository.UserRepository;
import com.campus.water.entity.dto.request.LoginRequest;
import com.campus.water.security.RoleConstants;
import com.campus.water.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
@RequiredArgsConstructor
public class LoginService {

    private final AdminRepository adminRepository;
    private final UserRepository userRepository;
    private final RepairerAuthRepository repairerAuthRepository;
    // 新增：注入RepairmanRepository
    @Autowired
    private RepairmanRepository repairmanRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginVO login(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        String userType = loginRequest.getUserType();

        return switch (userType) {
            case "admin" -> handleAdminLogin(username, password);
            case "user" -> handleUserLogin(username, password);
            case "repairman" -> handleRepairmanLogin(username, password);
            default -> throw new RuntimeException("无效的用户类型：" + userType);
        };
    }

    private LoginVO handleAdminLogin(String username, String password) {
        Admin admin = adminRepository.findByAdminName(username)
                .orElseThrow(() -> new RuntimeException("管理员不存在"));

        // 使用BCrypt验证密码
        if (!passwordEncoder.matches(password, admin.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        return createLoginVO(admin.getAdminId(), username, "admin", admin);
    }

    private LoginVO handleUserLogin(String username, String password) {
        User user = userRepository.findByStudentName(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        return createLoginVO(user.getStudentId(), username, "user");
    }


    private LoginVO handleRepairmanLogin(String username, String password) {
        // 1. 查询登录信息（RepairmanAuth）
        RepairerAuth repairerAuth = repairerAuthRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("维修人员不存在"));

        // 2. 验证密码
        if (!passwordEncoder.matches(password, repairerAuth.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        // 3. 通过repairman_id查询Repairman表获取area_id
        String repairmanId = repairerAuth.getRepairmanId();
        Repairman repairman = repairmanRepository.findById(repairmanId)
                .orElseThrow(() -> new RuntimeException("维修人员信息不存在"));
        String areaId = repairman.getAreaId(); // 假设Repairman类有getAreaId()方法

        // 4. 返回包含areaId的LoginVO
        return createLoginVO(repairmanId, username, "repairman", areaId);
    }

    // 新增重载方法，支持传递areaId
    private LoginVO createLoginVO(String userId, String username, String userType, String areaId) {
        LoginVO vo = new LoginVO();
        vo.setUserId(userId);
        vo.setUsername(username);
        vo.setUserType(userType);
        vo.setAreaId(areaId); // 设置区域ID
        // 生成token（保持原有逻辑）
        String role = RoleConstants.ROLE_REPAIRMAN;
        vo.setToken(jwtTokenProvider.generateToken(username, role));
        return vo;
    }

    /**
     * 处理管理员登录（支持多角色）
     */
    private LoginVO createLoginVO(String userId, String username, String userType, Admin admin) {
        LoginVO vo = new LoginVO();
        vo.setUserId(userId);
        vo.setUsername(username);
        vo.setUserType(userType);

        // 生成包含管理员角色的JWT令牌
        vo.setToken(jwtTokenProvider.generateToken(username, admin.getRole().name()));
        return vo;
    }

    /**
     * 处理用户/维修人员登录
     */
    private LoginVO createLoginVO(String userId, String username, String userType) {
        LoginVO vo = new LoginVO();
        vo.setUserId(userId);
        vo.setUsername(username);
        vo.setUserType(userType);

        String role = switch (userType) {
            case "user" -> RoleConstants.ROLE_STUDENT;
            case "repairman" -> RoleConstants.ROLE_REPAIRMAN;
            default -> throw new RuntimeException("不支持的用户类型：" + userType);
        };

        // 生成包含角色信息的JWT令牌
        vo.setToken(jwtTokenProvider.generateToken(username, role));
        return vo;
    }

    public void setRepairmanRepository(RepairmanRepository repairmanRepository) {
        this.repairmanRepository = repairmanRepository;
    }
}