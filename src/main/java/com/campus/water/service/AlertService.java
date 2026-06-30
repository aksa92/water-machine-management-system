package com.campus.water.service;

import com.campus.water.entity.Alert;
import com.campus.water.Repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlertService {

    private final AlertRepository alertRepository;

    /**
     * 查询告警历史（支持多条件组合）
     */
    public List<Alert> getAlertHistory(String deviceId, String level, String status,
                                       LocalDateTime startTime, LocalDateTime endTime, String areaId) {
        if (deviceId != null) {
            return alertRepository.findByDeviceIdAndTimestampBetween(deviceId, startTime, endTime);
        } else if (level != null) {
            return alertRepository.findByAlertLevelAndTimestampBetween(
                    Alert.AlertLevel.valueOf(level), startTime, endTime);
        } else if (status != null) {
            return alertRepository.findByStatusAndTimestampBetween(
                    Alert.AlertStatus.valueOf(status), startTime, endTime);
        } else if (areaId != null) {
            return alertRepository.findByAreaIdAndTimestampBetween(areaId, startTime, endTime);
        } else {
            return alertRepository.findByTimestampBetween(startTime, endTime);
        }
    }

    /**
     * 查询未处理告警（紧急优先排序）
     */
    public List<Alert> getPendingAlerts(String areaId) {
        List<Alert> pendingAlerts = areaId != null
                ? alertRepository.findByAreaIdAndStatus(areaId, Alert.AlertStatus.pending)
                : alertRepository.findByStatus(Alert.AlertStatus.pending);

        pendingAlerts.sort((a1, a2) ->
                Integer.compare(a2.getAlertLevel().getPriority(), a1.getAlertLevel().getPriority()));
        return pendingAlerts;
    }

    /**
     * 查询所有告警（支持多条件筛选）
     */
    public List<Alert> getAllAlerts(String deviceId, String level, String status, String areaId) {
        if (deviceId != null) {
            return alertRepository.findByDeviceId(deviceId);
        } else if (level != null) {
            return alertRepository.findByAlertLevel(Alert.AlertLevel.valueOf(level));
        } else if (status != null) {
            return alertRepository.findByStatus(Alert.AlertStatus.valueOf(status));
        } else if (areaId != null) {
            return alertRepository.findByAreaId(areaId);
        } else {
            return alertRepository.findAll();
        }
    }
}
