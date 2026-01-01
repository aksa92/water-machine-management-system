package main.java.com.campus.water.mapper;

import main.java.com.campus.water.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    // 根据设备ID和时间范围查询告警
    List<Alert> findByDeviceIdAndTimestampBetween(String deviceId, LocalDateTime startTime, LocalDateTime endTime);

    // 根据告警级别和时间范围查询告警
    List<Alert> findByAlertLevelAndTimestampBetween(Alert.AlertLevel level, LocalDateTime startTime, LocalDateTime endTime);

    // 根据告警状态和时间范围查询告警
    List<Alert> findByStatusAndTimestampBetween(Alert.AlertStatus status, LocalDateTime startTime, LocalDateTime endTime);

    // 根据区域ID和时间范围查询告警
    List<Alert> findByAreaIdAndTimestampBetween(String areaId, LocalDateTime startTime, LocalDateTime endTime);

    // 根据时间范围查询告警
    List<Alert> findByTimestampBetween(LocalDateTime startTime, LocalDateTime endTime);

    // 根据区域ID和告警状态查询告警
    List<Alert> findByAreaIdAndStatus(String areaId, Alert.AlertStatus status);

    // 根据告警状态查询告警
    List<Alert> findByStatus(Alert.AlertStatus status);

    // 保留原有的其他查询方法（如果有）
    // 根据设备ID查询告警
    List<Alert> findByDeviceId(String deviceId);

    // 根据告警类型查询
    List<Alert> findByAlertType(String alertType);

    // 根据告警级别查询
    List<Alert> findByAlertLevel(Alert.AlertLevel alertLevel);

    // 根据区域ID查询告警
    List<Alert> findByAreaId(String areaId);

    // 按状态和级别查询告警
    List<Alert> findByStatusAndAlertLevel(Alert.AlertStatus status, Alert.AlertLevel level);

    // 查询指定时间后的告警
    List<Alert> findByTimestampAfter(LocalDateTime timestamp);

    // 根据处理人查询告警
    List<Alert> findByResolvedBy(String resolvedBy);

    // 修复：将 AndStatus 改为 AndStatusIn，支持List集合的IN查询
    List<Alert> findByDeviceIdAndAlertTypeAndStatusInAndTimestampAfter(
            String deviceId,
            String alertType,
            List<Alert.AlertStatus> activeStatus,
            LocalDateTime timestamp
    );
}