package com.campus.water.service;

import com.campus.water.entity.Admin;
import com.campus.water.mapper.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;

    /**
     * 获取管理员列表（支持按角色和状态筛选）
     */
    public List<Admin> getAdminList(Admin.AdminRole role, Admin.AdminStatus status) {
        if (role != null && status != null) {
            // 按角色和状态筛选
            return adminRepository.findByRoleAndStatus(role, status);
        } else if (role != null) {
            // 仅按角色筛选
            return adminRepository.findByRole(role);
        } else if (status != null) {
            // 仅按状态筛选
            return adminRepository.findByStatus(status);
        } else {
            // 查询所有管理员
            return adminRepository.findAll();
        }
    }
}