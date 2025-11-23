package com.campus.water.mapper;

import datebaseclass.sensor.WaterQualityHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WaterQualityHistoryRepository extends JpaRepository<WaterQualityHistory, Long> {
    List<WaterQualityHistory> findByTerminalId(String terminalId);
    List<WaterQualityHistory> findByDeviceId(String deviceId);

    @Query("SELECT w FROM WaterQualityHistory w WHERE w.terminalId = ?1 AND w.timestamp BETWEEN ?2 AND ?3 ORDER BY w.timestamp DESC")
    List<WaterQualityHistory> findByTerminalIdAndTimestampBetween(String terminalId, LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT * FROM water_quality_history w WHERE w.terminal_id = ?1 ORDER BY w.timestamp DESC LIMIT 5", nativeQuery = true)
    List<WaterQualityHistory> findTop5ByTerminalIdOrderByTimestampDesc(String terminalId);

    @Query("SELECT w FROM WaterQualityHistory w WHERE w.waterQuality = ?1")
    List<WaterQualityHistory> findByWaterQuality(String waterQuality);

    @Query("SELECT w FROM WaterQualityHistory w WHERE w.tdsValue > ?1")
    List<WaterQualityHistory> findByTdsValueGreaterThan(Double tdsValue);

    @Query("SELECT w FROM WaterQualityHistory w WHERE w.deviceId = ?1 AND w.timestamp BETWEEN ?2 AND ?3 ORDER BY w.timestamp DESC")
    List<WaterQualityHistory> findByDeviceIdAndTimestampBetween(String deviceId, LocalDateTime start, LocalDateTime end);
}