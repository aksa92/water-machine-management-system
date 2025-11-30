package com.campus.water.repository;

import com.campus.water.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByDeviceId(String deviceId);
    List<Alert> findByAlertType(String alertType);
    List<Alert> findByStatus(Alert.AlertStatus status);
    List<Alert> findByAlertLevel(Alert.AlertLevel alertLevel);
    List<Alert> findByAreaId(String areaId);
    List<Alert> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    List<Alert> findByStatusAndAlertLevel(Alert.AlertStatus status, Alert.AlertLevel level);
    List<Alert> findByTimestampAfter(LocalDateTime timestamp);
    List<Alert> findByResolvedBy(String resolvedBy);
}