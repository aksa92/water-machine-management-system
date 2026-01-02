package main.java.com.campus.water.mapper;

import main.java.com.campus.water.entity.TerminalUsageStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TerminalUsageStatsRepository extends JpaRepository<TerminalUsageStats, Long> {
    // 根据终端ID查找使用统计
    List<TerminalUsageStats> findByTerminalId(String terminalId);

    // 根据统计日期查找使用统计
    List<TerminalUsageStats> findByStatDate(LocalDate statDate);

    // 根据日期范围查找使用统计
    List<TerminalUsageStats> findByStatDateBetween(LocalDate start, LocalDate end);

    // 根据终端ID和日期范围查询统计数据
    @Query("SELECT t FROM TerminalUsageStats t WHERE t.terminalId = ?1 AND t.statDate BETWEEN ?2 AND ?3")
    List<TerminalUsageStats> findByTerminalIdAndStatDateBetween(String terminalId, LocalDate start, LocalDate end);

    // 查询使用次数超过阈值的终端
    @Query("SELECT t FROM TerminalUsageStats t WHERE t.usageCount > ?1")
    List<TerminalUsageStats> findByUsageCountGreaterThan(Integer usageCount);

    // 查询总出水量超过阈值的终端
    @Query("SELECT t FROM TerminalUsageStats t WHERE t.totalWaterOutput > ?1")
    List<TerminalUsageStats> findByTotalWaterOutputGreaterThan(Double totalWaterOutput);

    // 按终端和日期查找统计记录
    @Query("SELECT t FROM TerminalUsageStats t WHERE t.terminalId = ?1 AND t.statDate = ?2")
    Optional<TerminalUsageStats> findByTerminalIdAndStatDate(String terminalId, LocalDate statDate);
}