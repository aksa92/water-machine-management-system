package com.campus.water.mapper;

import com.campus.water.entity.po.AdminPO; // 替换为PO包下的实体类
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<AdminPO, String> {
    // ========== 新增：登录核心方法（必须） ==========
    Optional<AdminPO> findByUsername(String username);

    // ========== 保留原有业务方法（适配PO类） ==========
    // 根据管理员ID查询
    Optional<AdminPO> findByAdminId(String adminId);

    // 根据管理员姓名/用户名模糊查询（适配PO的username字段）
    List<AdminPO> findByUsernameContaining(String username);

    // 根据角色查询管理员（引用PO内的枚举）
    List<AdminPO> findByRole(AdminPO.AdminRole role);

    // 根据状态查询管理员（引用PO内的枚举）
    List<AdminPO> findByStatus(AdminPO.AdminStatus status);

    // 根据手机号查询管理员
    Optional<AdminPO> findByPhone(String phone);

    // 按角色和状态查询管理员（JPQL中实体类名改为AdminPO）
    @Query("SELECT a FROM AdminPO a WHERE a.role = ?1 AND a.status = ?2")
    List<AdminPO> findByRoleAndStatus(AdminPO.AdminRole role, AdminPO.AdminStatus status);

    // 检查管理员ID是否存在
    boolean existsByAdminId(String adminId);

    // 检查手机号是否存在
    boolean existsByPhone(String phone);
}