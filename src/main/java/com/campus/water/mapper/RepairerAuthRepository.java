package com.campus.water.mapper;

import com.campus.water.entity.RepairerAuth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RepairerAuthRepository extends JpaRepository<RepairerAuth, Long> {
    Optional<RepairerAuth> findByUsername(String username);
    Optional<RepairerAuth> findByRepairmanId(String repairmanId);
    List<RepairerAuth> findByAccountStatus(RepairerAuth.AccountStatus accountStatus);

    @Query("SELECT ra FROM RepairerAuth ra WHERE ra.username = ?1 AND ra.accountStatus = 'active'")
    Optional<RepairerAuth> findActiveByUsername(String username);

    boolean existsByUsername(String username);
    boolean existsByRepairmanId(String repairmanId);
}