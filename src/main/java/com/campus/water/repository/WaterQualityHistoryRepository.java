package com.campus.water.repository;

import com.campus.water.entity.WaterQualityHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WaterQualityHistoryRepository extends JpaRepository<WaterQualityHistory, Long> {
    // 根据终端ID查询水质历史
    List<WaterQualityHistory> findByTerminalId(String terminalId);

    // 根据设备ID查询水质历史
    List<WaterQualityHistory> findByDeviceId(String deviceId);

    // 查询终端时间段内的水质记录
    @Query("SELECT w FROM WaterQualityHistory w WHERE w.terminalId = ?1 AND w.detectedTime BETWEEN ?2 AND ?3 ORDER BY w.detectedTime DESC")
    List<WaterQualityHistory> findByTerminalIdAndDetectedTimeBetween(String terminalId, LocalDateTime start, LocalDateTime end);

    // 获取终端最近5次检测记录
    @Query(value = "SELECT * FROM water_quality_history w WHERE w.terminal_id = ?1 ORDER BY w.detected_time DESC LIMIT 5", nativeQuery = true)
    List<WaterQualityHistory> findTop5ByTerminalIdOrderByDetectedTimeDesc(String terminalId);

    // 根据水质评级查询
    @Query("SELECT w FROM WaterQualityHistory w WHERE w.waterQuality = ?1")
    List<WaterQualityHistory> findByWaterQuality(String waterQuality);

    // 按TDS阈值查询水质记录（原水TDS）
    @Query("SELECT w FROM WaterQualityHistory w WHERE w.tdsValue1 > ?1")
    List<WaterQualityHistory> findByTdsValue1GreaterThan(Double tdsValue);

    // 按TDS阈值查询水质记录（纯水TDS）
    @Query("SELECT w FROM WaterQualityHistory w WHERE w.tdsValue2 > ?1")
    List<WaterQualityHistory> findByTdsValue2GreaterThan(Double tdsValue);

    // 按TDS阈值查询水质记录（矿化水TDS）
    @Query("SELECT w FROM WaterQualityHistory w WHERE w.tdsValue3 > ?1")
    List<WaterQualityHistory> findByTdsValue3GreaterThan(Double tdsValue);

    // 查询设备时间段内的水质记录
    @Query("SELECT w FROM WaterQualityHistory w WHERE w.deviceId = ?1 AND w.detectedTime BETWEEN ?2 AND ?3 ORDER BY w.detectedTime DESC")
    List<WaterQualityHistory> findByDeviceIdAndDetectedTimeBetween(String deviceId, LocalDateTime start, LocalDateTime end);

    // 获取终端最新水质记录
    @Query(value = "SELECT * FROM water_quality_history w WHERE w.terminal_id = ?1 ORDER BY w.detected_time DESC LIMIT 1", nativeQuery = true)
    Optional<WaterQualityHistory> findLatestByTerminalId(String terminalId);

    // 获取设备最新水质记录
    @Query(value = "SELECT * FROM water_quality_history w WHERE w.device_id = ?1 ORDER BY w.detected_time DESC LIMIT 1", nativeQuery = true)
    Optional<WaterQualityHistory> findLatestByDeviceId(String deviceId);

    // 根据检测时间范围查询水质记录
    @Query("SELECT w FROM WaterQualityHistory w WHERE w.detectedTime BETWEEN ?1 AND ?2 ORDER BY w.detectedTime DESC")
    List<WaterQualityHistory> findByDetectedTimeBetween(LocalDateTime start, LocalDateTime end);
}