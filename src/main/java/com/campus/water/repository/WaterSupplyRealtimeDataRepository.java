package com.campus.water.repository;

import com.campus.water.entity.WaterSupplyRealtimeData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WaterSupplyRealtimeDataRepository extends JpaRepository<WaterSupplyRealtimeData, Long> {
    List<WaterSupplyRealtimeData> findByDeviceId(String deviceId);

    @Query("SELECT w FROM WaterSupplyRealtimeData w WHERE w.timestamp BETWEEN ?1 AND ?2 ORDER BY w.timestamp DESC")
    List<WaterSupplyRealtimeData> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT w FROM WaterSupplyRealtimeData w WHERE w.deviceId = ?1 AND w.timestamp BETWEEN ?2 AND ?3 ORDER BY w.timestamp DESC")
    List<WaterSupplyRealtimeData> findByDeviceIdAndTimestampBetween(String deviceId, LocalDateTime start, LocalDateTime end);

    @Query(value = "SELECT * FROM water_supply_realtime_data w WHERE w.device_id = ?1 ORDER BY w.timestamp DESC LIMIT 10", nativeQuery = true)
    List<WaterSupplyRealtimeData> findTop10ByDeviceIdOrderByTimestampDesc(String deviceId);

    List<WaterSupplyRealtimeData> findByStatus(WaterSupplyRealtimeData.DeviceStatus status);

    @Query("SELECT w FROM WaterSupplyRealtimeData w WHERE w.waterLevel < ?1")
    List<WaterSupplyRealtimeData> findByWaterLevelLessThan(Double waterLevel);

    @Query("SELECT w FROM WaterSupplyRealtimeData w WHERE w.temperature > ?1")
    List<WaterSupplyRealtimeData> findByTemperatureGreaterThan(Double temperature);

    @Query("SELECT w FROM WaterSupplyRealtimeData w WHERE w.waterPress < ?1")
    List<WaterSupplyRealtimeData> findByWaterPressLessThan(Double waterPress);

    @Query(value = "SELECT * FROM water_supply_realtime_data w WHERE w.device_id = ?1 ORDER BY w.timestamp DESC LIMIT 1", nativeQuery = true)
    Optional<WaterSupplyRealtimeData> findLatestByDeviceId(String deviceId);
}