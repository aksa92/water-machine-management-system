package com.campus.water.repository;

import com.campus.water.entity.po.AdminPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<AdminPO, String> {
    // 登录核心：通过用户名查询
    Optional<AdminPO> findByUsername(String username);

    // 原有业务方法
    Optional<AdminPO> findByAdminId(String adminId);
    List<AdminPO> findByUsernameContaining(String username);
    List<AdminPO> findByRole(AdminPO.AdminRole role);
    List<AdminPO> findByStatus(AdminPO.AdminStatus status);
    Optional<AdminPO> findByPhone(String phone);

    @Query("SELECT a FROM AdminPO a WHERE a.role = ?1 AND a.status = ?2")
    List<AdminPO> findByRoleAndStatus(AdminPO.AdminRole role, AdminPO.AdminStatus status);

    boolean existsByAdminId(String adminId);
    boolean existsByPhone(String phone);
}