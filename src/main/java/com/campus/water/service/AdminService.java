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
     * 获取所有管理员列表
     */
    public List<Admin> getAdminList() {
        return adminRepository.findAll();
    }

    /**
     * 根据姓名搜索管理员
     */
    public List<Admin> searchAdminsByName(String name) {
        return adminRepository.findByAdminNameContaining(name);
    }
}