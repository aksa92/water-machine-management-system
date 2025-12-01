package com.campus.water.mapper;

import com.campus.water.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    // 根据设备ID查询告警
    List<Alert> findByDeviceId(String deviceId);

    // 根据告警类型查询
    List<Alert> findByAlertType(String alertType);

    // 根据告警状态查询
    List<Alert> findByStatus(Alert.AlertStatus status);

    // 根据告警级别查询
    List<Alert> findByAlertLevel(Alert.AlertLevel alertLevel);

    // 根据区域ID查询告警
    List<Alert> findByAreaId(String areaId);

    // 根据时间范围查询告警记录
    List<Alert> findByTimestampBetween(LocalDateTime start, LocalDateTime end);

    // 按状态和级别查询告警
    List<Alert> findByStatusAndAlertLevel(Alert.AlertStatus status, Alert.AlertLevel level);

    // 查询指定时间后的告警
    List<Alert> findByTimestampAfter(LocalDateTime timestamp);

    // 根据处理人查询告警
    List<Alert> findByResolvedBy(String resolvedBy);

    // 新增：检查重复未处理告警
    List<Alert> findByDeviceIdAndAlertTypeAndStatusAndTimestampAfter(
            String deviceId,
            String alertType,
            List<Alert.AlertStatus> activeStatus,
            LocalDateTime timestamp
    );
}