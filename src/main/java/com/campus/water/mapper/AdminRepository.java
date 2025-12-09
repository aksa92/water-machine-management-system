package com.campus.water.mapper;

import com.campus.water.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, String> {
    // 登录核心方法：按用户名查询
    Optional<Admin> findByAdminName(String adminName);

    // 按管理员ID查询
    Optional<Admin> findByAdminId(String adminId);

    // 按姓名模糊查询
    List<Admin> findByAdminNameContaining(String adminName);

    // 按手机号查询
    Optional<Admin> findByPhone(String phone);

    // 按角色查询管理员（核心：恢复角色筛选）
    List<Admin> findByRole(Admin.AdminRole role);

    // 按姓名+角色组合查询（可选，增强筛选）
    List<Admin> findByAdminNameContainingAndRole(String name, Admin.AdminRole role);

    // 检查唯一约束
    boolean existsByAdminId(String adminId);
    boolean existsByPhone(String phone);
    boolean existsByAdminName(String adminName);
}