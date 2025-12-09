package com.campus.water.service;

import com.campus.water.entity.Admin;
import com.campus.water.mapper.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;

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
     * 按ID查询管理员
     */
    public Optional<Admin> getAdminById(String adminId) {
        return adminRepository.findByAdminId(adminId);
    }

    /**
     * 新增/修改管理员（支持指定角色）
     */
    public Admin saveAdmin(Admin admin) {
        admin.setUpdatedTime(LocalDateTime.now());
        if (admin.getCreatedTime() == null) {
            admin.setCreatedTime(LocalDateTime.now());
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
        // 实际生产环境需替换为BCrypt密码加密验证
        return admin.filter(a -> a.getPassword().equals(password));
    }

    /**
     * 获取所有角色枚举（供前端下拉框使用）
     */
    public Admin.AdminRole[] getAllRoles() {
        return Admin.AdminRole.values();
    }
}