package com.campus.water.mapper;

import com.campus.water.entity.Admin; // 改为引用Admin实体类
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, String> {
    // ========== 登录核心方法（适配Admin的adminName字段） ==========
    Optional<Admin> findByAdminName(String admin_name);

    // ========== 业务方法（适配Admin类） ==========
    // 根据管理员ID查询
    Optional<Admin> findByAdminId(String adminId);

    // 根据管理员姓名模糊查询（适配Admin的adminName字段）
    List<Admin> findByAdminNameContaining(String adminName);

    // 根据角色查询管理员（引用Admin内的枚举）
    List<Admin> findByRole(Admin.AdminRole role);

    // 根据状态查询管理员（引用Admin内的枚举）
    List<Admin> findByStatus(Admin.AdminStatus status);

    // 根据手机号查询管理员
    Optional<Admin> findByPhone(String phone);

    // 按角色和状态查询管理员（JPQL中实体类名改为Admin）
    @Query("SELECT a FROM Admin a WHERE a.role = ?1 AND a.status = ?2")
    List<Admin> findByRoleAndStatus(Admin.AdminRole role, Admin.AdminStatus status);

    // 检查管理员ID是否存在
    boolean existsByAdminId(String adminId);

    // 检查手机号是否存在
    boolean existsByPhone(String phone);

    // 检查用户名是否存在（适配Admin的adminName字段）
    boolean existsByAdminName(String admin_name);

}