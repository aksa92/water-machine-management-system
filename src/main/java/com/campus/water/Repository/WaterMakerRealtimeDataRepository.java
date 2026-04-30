package com.campus.water.Repository;

import com.campus.water.entity.WaterMakerRealtimeData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WaterMakerRealtimeDataRepository extends JpaRepository<WaterMakerRealtimeData, Long> {
    // 根据设备ID查询实时数据
    List<WaterMakerRealtimeData> findByDeviceId(String deviceId);

    // 根据记录时间范围查询数据
    @Query("SELECT w FROM WaterMakerRealtimeData w WHERE w.recordTime BETWEEN ?1 AND ?2 ORDER BY w.recordTime DESC")
    List<WaterMakerRealtimeData> findByRecordTimeBetween(LocalDateTime start, LocalDateTime end);

    // 查询设备时间段内的实时数据
    @Query("SELECT w FROM WaterMakerRealtimeData w WHERE w.deviceId = ?1 AND w.recordTime BETWEEN ?2 AND ?3 ORDER BY w.recordTime DESC")
    List<WaterMakerRealtimeData> findByDeviceIdAndRecordTimeBetween(String deviceId, LocalDateTime start, LocalDateTime end);

    // 获取设备最近10条记录
    @Query(value = "SELECT * FROM water_maker_realtime_data w WHERE w.device_id = ?1 ORDER BY w.record_time DESC LIMIT 10", nativeQuery = true)
    List<WaterMakerRealtimeData> findTop10ByDeviceIdOrderByRecordTimeDesc(String deviceId);

    // 根据泄漏状态查询设备数据
    List<WaterMakerRealtimeData> findByLeakage(Boolean leakage);

    // 根据设备状态查询数据
    List<WaterMakerRealtimeData> findByStatus(WaterMakerRealtimeData.DeviceStatus status);

    // 查询滤芯寿命低的设备
    @Query("SELECT w FROM WaterMakerRealtimeData w WHERE w.filterLife < ?1")
    List<WaterMakerRealtimeData> findByFilterLifeLessThan(Integer filterLife);

    // 按TDS阈值查询设备数据（原水TDS）
    @Query("SELECT w FROM WaterMakerRealtimeData w WHERE w.tdsValue1 > ?1")
    List<WaterMakerRealtimeData> findByTdsValue1GreaterThan(Double tdsValue);

    // 按TDS阈值查询设备数据（纯水TDS）
    @Query("SELECT w FROM WaterMakerRealtimeData w WHERE w.tdsValue2 > ?1")
    List<WaterMakerRealtimeData> findByTdsValue2GreaterThan(Double tdsValue);

    // 按TDS阈值查询设备数据（矿化水TDS）
    @Query("SELECT w FROM WaterMakerRealtimeData w WHERE w.tdsValue3 > ?1")
    List<WaterMakerRealtimeData> findByTdsValue3GreaterThan(Double tdsValue);

    // 查询水压低于阈值的设备
    @Query("SELECT w FROM WaterMakerRealtimeData w WHERE w.waterPress < ?1")
    List<WaterMakerRealtimeData> findByWaterPressLessThan(Double waterPress);

    // 获取设备最新运行数据
    @Query(value = "SELECT * FROM water_maker_realtime_data w WHERE w.device_id = ?1 ORDER BY w.record_time DESC LIMIT 1", nativeQuery = true)
    Optional<WaterMakerRealtimeData> findLatestByDeviceId(String deviceId);
}