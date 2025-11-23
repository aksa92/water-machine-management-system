package com.campus.water.mapper;

import datebaseclass.business.TerminalUsageStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TerminalUsageStatsRepository extends JpaRepository<TerminalUsageStats, Long> {
    List<TerminalUsageStats> findByTerminalId(String terminalId);
    List<TerminalUsageStats> findByStatDate(LocalDate statDate);
    List<TerminalUsageStats> findByStatDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT t FROM TerminalUsageStats t WHERE t.terminalId = ?1 AND t.statDate BETWEEN ?2 AND ?3")
    List<TerminalUsageStats> findByTerminalIdAndStatDateBetween(String terminalId, LocalDate start, LocalDate end);

    @Query("SELECT t FROM TerminalUsageStats t WHERE t.usageCount > ?1")
    List<TerminalUsageStats> findByUsageCountGreaterThan(Integer usageCount);

    @Query("SELECT t FROM TerminalUsageStats t WHERE t.totalWaterOutput > ?1")
    List<TerminalUsageStats> findByTotalWaterOutputGreaterThan(Double totalWaterOutput);

    @Query("SELECT t FROM TerminalUsageStats t WHERE t.terminalId = ?1 AND t.statDate = ?2")
    Optional<TerminalUsageStats> findByTerminalIdAndStatDate(String terminalId, LocalDate statDate);
}