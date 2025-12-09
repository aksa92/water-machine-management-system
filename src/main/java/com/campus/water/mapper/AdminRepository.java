package com.campus.water.mapper;

import com.campus.water.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, String> {
    // 登录核心方法
    Optional<Admin> findByAdminName(String adminName);

    // 根据管理员ID查询
    Optional<Admin> findByAdminId(String adminId);

    // 根据管理员姓名模糊查询
    List<Admin> findByAdminNameContaining(String adminName);

    // 根据手机号查询管理员
    Optional<Admin> findByPhone(String phone);

    // 检查管理员ID是否存在
    boolean existsByAdminId(String adminId);

    // 检查手机号是否存在
    boolean existsByPhone(String phone);

    // 检查用户名是否存在
    boolean existsByAdminName(String adminName);
}