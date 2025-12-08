package com.campus.water.mapper;

import com.campus.water.entity.RepairerAuth; // 改为entity包下的RepairerAuth
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepairerAuthRepository extends JpaRepository<RepairerAuth, Long> { // 实体类改为RepairerAuth
    // 根据用户名查询认证信息（适配RepairerAuth类）
    Optional<RepairerAuth> findByUsername(String username);

    // 根据维修人员ID查询认证信息（适配RepairerAuth类）
    Optional<RepairerAuth> findByRepairmanId(String repairmanId);

    // 根据账户状态查询认证信息（引用RepairerAuth内的枚举）
    List<RepairerAuth> findByAccountStatus(RepairerAuth.AccountStatus accountStatus);

    // 查找活跃状态的维修人员账号（JPQL实体类名改为RepairerAuth）
    @Query("SELECT ra FROM RepairerAuth ra WHERE ra.username = ?1 AND ra.accountStatus = 'active'")
    Optional<RepairerAuth> findActiveByUsername(String username);

    // 检查用户名是否存在
    boolean existsByUsername(String username);

    // 检查维修工ID是否存在
    boolean existsByRepairmanId(String repairmanId);
}