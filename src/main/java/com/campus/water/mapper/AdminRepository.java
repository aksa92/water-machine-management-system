package com.campus.water.mapper;

import com.campus.water.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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

    // 新增：按区域ID查询管理员
    List<Admin> findByAreaId(String areaId);


    // 按角色查询管理员（核心：恢复角色筛选）
    List<Admin> findByRole(Admin.AdminRole role);

    // 按姓名+角色组合查询（可选，增强筛选）
    List<Admin> findByAdminNameContainingAndRole(String name, Admin.AdminRole role);

    // 新增：查询未负责任何片区的区域管理员（role=ROLE_AREA_ADMIN 且 areaId=null）
    List<Admin> findByRoleAndAreaIdIsNull(Admin.AdminRole role);

    // 新增1：查询指定校区的区域管理员（精准查询，用于单个校区权限校验）
    List<Admin> findByRoleAndAreaId(Admin.AdminRole role, String areaId);

    // 新增2：查询所有校区关联的区域管理员（排除市区，用于管理员列表筛选）
    // 备注：此处使用@Query注解，关联Area表过滤区域类型为campus的管理员
    @Query("SELECT a FROM Admin a WHERE a.role = ?1 AND a.areaId IN " +
            "(SELECT ar.areaId FROM Area ar WHERE ar.areaType = com.campus.water.entity.Area.AreaType.campus)")
    List<Admin> findAllAreaAdminsForCampus(Admin.AdminRole role);

    // 检查唯一约束
    boolean existsByAdminId(String adminId);
    boolean existsByPhone(String phone);
    boolean existsByAdminName(String adminName);
}