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
     * 获取所有管理员（默认仅Admin角色）
     */
    public List<Admin> getAdminList() {
        // 单角色下直接查全部，也可调用findByRole(Admin.AdminRole.Admin)
        return adminRepository.findAll();
    }

    /**
     * 按姓名搜索管理员
     */
    public List<Admin> searchAdminsByName(String name) {
        return adminRepository.findByAdminNameContaining(name);
    }

    /**
     * 按ID查询管理员
     */
    public Optional<Admin> getAdminById(String adminId) {
        return adminRepository.findByAdminId(adminId);
    }

    /**
     * 新增/修改管理员（默认角色为Admin）
     */
    public Admin saveAdmin(Admin admin) {
        // 强制设置为Admin角色，避免手动修改
        admin.setRole(Admin.AdminRole.Admin);
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
        // 此处仅示例，实际需结合密码加密（如BCrypt）验证
        return admin.filter(a -> a.getPassword().equals(password));
    }
}