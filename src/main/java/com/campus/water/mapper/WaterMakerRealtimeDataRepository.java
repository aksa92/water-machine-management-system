package com.campus.water.mapper;

import datebaseclass.sensor.WaterMakerRealtimeData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WaterMakerRealtimeDataRepository extends JpaRepository<WaterMakerRealtimeData, Long> {
    List<WaterMakerRealtimeData> findByDeviceId(String deviceId);

    @Query("SELECT w FROM WaterMakerRealtimeData w WHERE w.timestamp BETWEEN ?1 AND ?2 ORDER BY w.timestamp DESC")
    List<WaterMakerRealtimeData> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT w FROM WaterMakerRealtimeData w WHERE w.deviceId = ?1 AND w.timestamp BETWEEN ?2 AND ?3 ORDER BY w.timestamp DESC")
    List<WaterMakerRealtimeData> findByDeviceIdAndTimestampBetween(String deviceId, LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT * FROM water_maker_realtime_data w WHERE w.device_id = ?1 ORDER BY w.timestamp DESC LIMIT 10", nativeQuery = true)
    List<WaterMakerRealtimeData> findTop10ByDeviceIdOrderByTimestampDesc(String deviceId);

    List<WaterMakerRealtimeData> findByLeakage(Boolean leakage);
    List<WaterMakerRealtimeData> findByStatus(WaterMakerRealtimeData.DeviceStatus status);

    @Query("SELECT w FROM WaterMakerRealtimeData w WHERE w.filterLife < ?1")
    List<WaterMakerRealtimeData> findByFilterLifeLessThan(Integer filterLife);

    @Query("SELECT w FROM WaterMakerRealtimeData w WHERE w.tdsValue > ?1")
    List<WaterMakerRealtimeData> findByTdsValueGreaterThan(Double tdsValue);
}