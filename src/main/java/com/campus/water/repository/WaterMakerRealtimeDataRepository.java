package com.campus.water.repository;

import com.campus.water.entity.WaterMakerRealtimeData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WaterMakerRealtimeDataRepository extends JpaRepository<WaterMakerRealtimeData, Long> {
    List<WaterMakerRealtimeData> findByDeviceId(String deviceId);

    @Query("SELECT w FROM WaterMakerRealtimeData w WHERE w.recordTime BETWEEN ?1 AND ?2 ORDER BY w.recordTime DESC")
    List<WaterMakerRealtimeData> findByRecordTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT w FROM WaterMakerRealtimeData w WHERE w.deviceId = ?1 AND w.recordTime BETWEEN ?2 AND ?3 ORDER BY w.recordTime DESC")
    List<WaterMakerRealtimeData> findByDeviceIdAndRecordTimeBetween(String deviceId, LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT * FROM water_maker_realtime_data w WHERE w.device_id = ?1 ORDER BY w.record_time DESC LIMIT 10", nativeQuery = true)
    List<WaterMakerRealtimeData> findTop10ByDeviceIdOrderByRecordTimeDesc(String deviceId);

    List<WaterMakerRealtimeData> findByLeakage(Boolean leakage);
    List<WaterMakerRealtimeData> findByStatus(WaterMakerRealtimeData.DeviceStatus status);

    @Query("SELECT w FROM WaterMakerRealtimeData w WHERE w.filterLife < ?1")
    List<WaterMakerRealtimeData> findByFilterLifeLessThan(Integer filterLife);

    @Query("SELECT w FROM WaterMakerRealtimeData w WHERE w.tdsValue1 > ?1")
    List<WaterMakerRealtimeData> findByTdsValue1GreaterThan(Double tdsValue);

    @Query("SELECT w FROM WaterMakerRealtimeData w WHERE w.tdsValue2 > ?1")
    List<WaterMakerRealtimeData> findByTdsValue2GreaterThan(Double tdsValue);

    @Query("SELECT w FROM WaterMakerRealtimeData w WHERE w.tdsValue3 > ?1")
    List<WaterMakerRealtimeData> findByTdsValue3GreaterThan(Double tdsValue);

    @Query("SELECT w FROM WaterMakerRealtimeData w WHERE w.waterPress < ?1")
    List<WaterMakerRealtimeData> findByWaterPressLessThan(Double waterPress);

    @Query(value = "SELECT * FROM water_maker_realtime_data w WHERE w.device_id = ?1 ORDER BY w.record_time DESC LIMIT 1", nativeQuery = true)
    Optional<WaterMakerRealtimeData> findLatestByDeviceId(String deviceId);
}