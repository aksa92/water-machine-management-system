package com.campus.water.service;

import com.campus.water.entity.Admin;
import com.campus.water.mapper.AdminRepository;
import com.campus.water.mapper.AreaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final AreaRepository areaRepository;  // 新增注入


    @Autowired
    private PasswordEncoder passwordEncoder;
    /**
     * 获取管理员列表（支持按姓名/角色筛选）
     */
    public List<Admin> getAdminList(String name, Admin.AdminRole role) {
        if (name != null && !name.isEmpty() && role != null) {
            // 按姓名+角色组合查询
            return adminRepository.findByAdminNameContainingAndRole(name, role);
        } else if (role != null) {
            // 仅按角色查询
            return adminRepository.findByRole(role);
        } else if (name != null && !name.isEmpty()) {
            // 仅按姓名查询
            return adminRepository.findByAdminNameContaining(name);
        } else {
            // 查询全部
            return adminRepository.findAll();
        }
    }

    /**
     * 新增：获取指定区域的管理员列表
     */
    public List<Admin> getAdminsByAreaId(String areaId) {
        // 校验区域是否存在
        if (!areaRepository.existsById(areaId)) {
            throw new RuntimeException("区域不存在：" + areaId);
        }
        return adminRepository.findByAreaId(areaId);
    }

    /**
     * 按ID查询管理员
     */
    public Optional<Admin> getAdminById(String adminId) {
        return adminRepository.findByAdminId(adminId);
    }

    /**
     * 新增/修改管理员（支持指定角色）
     * 重写保存方法，增加区域校验（区域管理员必须关联区域）
     */
    public Admin saveAdmin(Admin admin) {
        admin.setUpdatedTime(LocalDateTime.now());
        if (admin.getCreatedTime() == null) {
            admin.setCreatedTime(LocalDateTime.now());
        }

        // 区域管理员必须关联区域
        if (admin.getRole() == Admin.AdminRole.ROLE_AREA_ADMIN) {
            if (admin.getAreaId() == null || admin.getAreaId().trim().isEmpty()) {
                throw new RuntimeException("区域管理员必须关联具体区域");
            }
            // 校验关联的区域是否存在
            if (!areaRepository.existsById(admin.getAreaId())) {
                throw new RuntimeException("关联的区域不存在：" + admin.getAreaId());
            }
        } else {
            // 非区域管理员清空区域ID
            admin.setAreaId(null);
        }

        return adminRepository.save(admin);
    }

    /**
     * 删除管理员
     */
    public void deleteAdmin(String adminId) {
        adminRepository.deleteById(adminId);
    }

    /**
     * 管理员登录验证
     */
    public Optional<Admin> login(String adminName, String password) {
        Optional<Admin> admin = adminRepository.findByAdminName(adminName);
        // 使用MD5加密器验证密码
        return admin.filter(a -> passwordEncoder.matches(password, a.getPassword()));
    }
    /**
     * 获取所有角色枚举（供前端下拉框使用）
     */
    public Admin.AdminRole[] getAllRoles() {
        return Admin.AdminRole.values();
    }

    public AreaRepository getAreaRepository() {
        return areaRepository;
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 个人信息更新（限制可修改字段）
     */
    public Admin updateProfile(Admin profile) {
        // 1. 获取数据库中原始信息
        Admin existingAdmin = adminRepository.findByAdminId(profile.getAdminId())
                .orElseThrow(() -> new RuntimeException("管理员不存在"));

        // 2. 仅更新允许修改的字段（排除角色、区域等敏感信息）
        existingAdmin.setAdminName(profile.getAdminName());
        existingAdmin.setPhone(profile.getPhone());
        existingAdmin.setUpdatedTime(LocalDateTime.now());

        // 3. 密码修改单独处理（如果有密码更新需求）
        if (profile.getPassword() != null && !profile.getPassword().isEmpty()) {
            existingAdmin.setPassword(passwordEncoder.encode(profile.getPassword()));
        }

        return adminRepository.save(existingAdmin);
    }

    /**
     * 辅助方法：通过用户名查询管理员
     */
    public Optional<Admin> getAdminByName(String username) {
        return adminRepository.findByAdminName(username);
    }

}