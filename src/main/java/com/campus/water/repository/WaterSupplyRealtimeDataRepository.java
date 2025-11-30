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
    // 根据设备ID查询实时数据
    List<WaterSupplyRealtimeData> findByDeviceId(String deviceId);

    // 根据时间戳范围查询数据
    @Query("SELECT w FROM WaterSupplyRealtimeData w WHERE w.timestamp BETWEEN ?1 AND ?2 ORDER BY w.timestamp DESC")
    List<WaterSupplyRealtimeData> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    // 查询设备时间段内的实时数据
    @Query("SELECT w FROM WaterSupplyRealtimeData w WHERE w.deviceId = ?1 AND w.timestamp BETWEEN ?2 AND ?3 ORDER BY w.timestamp DESC")
    List<WaterSupplyRealtimeData> findByDeviceIdAndTimestampBetween(String deviceId, LocalDateTime start, LocalDateTime end);

    // 获取设备最近10条记录
    @Query(value = "SELECT * FROM water_supply_realtime_data w WHERE w.device_id = ?1 ORDER BY w.timestamp DESC LIMIT 10", nativeQuery = true)
    List<WaterSupplyRealtimeData> findTop10ByDeviceIdOrderByTimestampDesc(String deviceId);

    // 根据设备状态查询数据
    List<WaterSupplyRealtimeData> findByStatus(WaterSupplyRealtimeData.DeviceStatus status);

    // 查询水位低于阈值的设备
    @Query("SELECT w FROM WaterSupplyRealtimeData w WHERE w.waterLevel < ?1")
    List<WaterSupplyRealtimeData> findByWaterLevelLessThan(Double waterLevel);

    // 查询温度高于阈值的设备
    @Query("SELECT w FROM WaterSupplyRealtimeData w WHERE w.temperature > ?1")
    List<WaterSupplyRealtimeData> findByTemperatureGreaterThan(Double temperature);

    // 查询水压低于阈值的设备
    @Query("SELECT w FROM WaterSupplyRealtimeData w WHERE w.waterPress < ?1")
    List<WaterSupplyRealtimeData> findByWaterPressLessThan(Double waterPress);

    // 获取设备最新供水数据
    @Query(value = "SELECT * FROM water_supply_realtime_data w WHERE w.device_id = ?1 ORDER BY w.timestamp DESC LIMIT 1", nativeQuery = true)
    Optional<WaterSupplyRealtimeData> findLatestByDeviceId(String deviceId);
}