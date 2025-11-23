package com.campus.water.mapper;

import datebaseclass.system.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, String> {
    Optional<Admin> findByAdminId(String adminId);
    List<Admin> findByAdminNameContaining(String adminName);
    List<Admin> findByRole(Admin.AdminRole role);
    List<Admin> findByStatus(Admin.AdminStatus status);
    Optional<Admin> findByPhone(String phone);

    @Query("SELECT a FROM Admin a WHERE a.role = ?1 AND a.status = ?2")
    List<Admin> findByRoleAndStatus(Admin.AdminRole role, Admin.AdminStatus status);

    boolean existsByAdminId(String adminId);
    boolean existsByPhone(String phone);
}