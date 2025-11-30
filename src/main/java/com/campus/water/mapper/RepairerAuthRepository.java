package com.campus.water.mapper;

import com.campus.water.entity.po.RepairerAuthPO; // 替换为PO包下的实体类
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepairerAuthRepository extends JpaRepository<RepairerAuthPO, Long> {
    // 根据用户名查询认证信息（适配PO类）
    Optional<RepairerAuthPO> findByUsername(String username);

    // 根据维修人员ID查询认证信息（适配PO类）
    Optional<RepairerAuthPO> findByRepairmanId(String repairmanId);

    // 根据账户状态查询认证信息（引用PO内的枚举）
    List<RepairerAuthPO> findByAccountStatus(RepairerAuthPO.AccountStatus accountStatus);

    // 查找活跃状态的维修人员账号（JPQL实体类名改为RepairerAuthPO）
    @Query("SELECT ra FROM RepairerAuthPO ra WHERE ra.username = ?1 AND ra.accountStatus = 'active'")
    Optional<RepairerAuthPO> findActiveByUsername(String username);

    // 检查用户名是否存在
    boolean existsByUsername(String username);

    // 检查维修工ID是否存在
    boolean existsByRepairmanId(String repairmanId);
}