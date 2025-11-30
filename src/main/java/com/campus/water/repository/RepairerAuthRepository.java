package com.campus.water.repository;

import com.campus.water.entity.po.RepairerAuthPO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepairerAuthRepository extends JpaRepository<RepairerAuthPO, Long> {
    // 登录核心：通过用户名查询
    Optional<RepairerAuthPO> findByUsername(String username);

    // 按维修人员ID查询
    Optional<RepairerAuthPO> findByRepairmanId(String repairmanId);

    // 按账号状态查询（引用PO内的枚举）
    List<RepairerAuthPO> findByAccountStatus(RepairerAuthPO.AccountStatus accountStatus);

    // 自定义查询：查询活跃状态的用户
    @Query("SELECT ra FROM RepairerAuthPO ra WHERE ra.username = ?1 AND ra.accountStatus = 'active'")
    Optional<RepairerAuthPO> findActiveByUsername(String username);

    // 校验用户名是否存在
    boolean existsByUsername(String username);

    // 校验维修人员ID是否存在
    boolean existsByRepairmanId(String repairmanId);
}