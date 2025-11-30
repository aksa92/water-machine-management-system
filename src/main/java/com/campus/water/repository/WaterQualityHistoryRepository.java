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
    List<WaterQualityHistory> findByTerminalId(String terminalId);
    List<WaterQualityHistory> findByDeviceId(String deviceId);

    @Query("SELECT w FROM WaterQualityHistory w WHERE w.terminalId = ?1 AND w.detectedTime BETWEEN ?2 AND ?3 ORDER BY w.detectedTime DESC")
    List<WaterQualityHistory> findByTerminalIdAndDetectedTimeBetween(String terminalId, LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT * FROM water_quality_history w WHERE w.terminal_id = ?1 ORDER BY w.detected_time DESC LIMIT 5", nativeQuery = true)
    List<WaterQualityHistory> findTop5ByTerminalIdOrderByDetectedTimeDesc(String terminalId);

    @Query("SELECT w FROM WaterQualityHistory w WHERE w.waterQuality = ?1")
    List<WaterQualityHistory> findByWaterQuality(String waterQuality);

    @Query("SELECT w FROM WaterQualityHistory w WHERE w.tdsValue1 > ?1")
    List<WaterQualityHistory> findByTdsValue1GreaterThan(Double tdsValue);

    @Query("SELECT w FROM WaterQualityHistory w WHERE w.tdsValue2 > ?1")
    List<WaterQualityHistory> findByTdsValue2GreaterThan(Double tdsValue);

    @Query("SELECT w FROM WaterQualityHistory w WHERE w.tdsValue3 > ?1")
    List<WaterQualityHistory> findByTdsValue3GreaterThan(Double tdsValue);

    @Query("SELECT w FROM WaterQualityHistory w WHERE w.deviceId = ?1 AND w.detectedTime BETWEEN ?2 AND ?3 ORDER BY w.detectedTime DESC")
    List<WaterQualityHistory> findByDeviceIdAndDetectedTimeBetween(String deviceId, LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT * FROM water_quality_history w WHERE w.terminal_id = ?1 ORDER BY w.detected_time DESC LIMIT 1", nativeQuery = true)
    Optional<WaterQualityHistory> findLatestByTerminalId(String terminalId);

    @Query(value = "SELECT * FROM water_quality_history w WHERE w.device_id = ?1 ORDER BY w.detected_time DESC LIMIT 1", nativeQuery = true)
    Optional<WaterQualityHistory> findLatestByDeviceId(String deviceId);

    @Query("SELECT w FROM WaterQualityHistory w WHERE w.detectedTime BETWEEN ?1 AND ?2 ORDER BY w.detectedTime DESC")
    List<WaterQualityHistory> findByDetectedTimeBetween(LocalDateTime start, LocalDateTime end);
}