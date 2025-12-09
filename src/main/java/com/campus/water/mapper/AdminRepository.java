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

    // 检查唯一约束
    boolean existsByAdminId(String adminId);
    boolean existsByPhone(String phone);
    boolean existsByAdminName(String adminName);

    // （可选）若需按角色过滤（仅Admin角色），保留此方法（单角色下可省略）
    List<Admin> findByRole(Admin.AdminRole role);
}