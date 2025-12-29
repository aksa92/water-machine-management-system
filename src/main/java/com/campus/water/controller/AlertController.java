package com.campus.water.controller;

import com.campus.water.entity.Alert;
import com.campus.water.mapper.AlertRepository;
import com.campus.water.util.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
@RequiredArgsConstructor
@Tag(name = "告警管理接口")

public class AlertController {

    private final AlertRepository alertRepository;

    @GetMapping("/test")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','AREA_ADMIN', 'REPAIRMAN')")
    public ResultVO<String> testAuth() {
        return ResultVO.success("权限验证通过");
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','AREA_ADMIN', 'REPAIRMAN')")
    @Operation(summary = "分页查询告警历史（支持多条件筛选）")
    public ResultVO<List<Alert>> getAlertHistory(
            @Parameter(description = "设备ID（可选）") @RequestParam(required = false) String deviceId,
            @Parameter(description = "告警级别（可选，如error、critical）") @RequestParam(required = false) String level,
            @Parameter(description = "告警状态（可选，如pending、resolved）") @RequestParam(required = false) String status,
            @Parameter(description = "开始时间（可选，格式：yyyy-MM-dd HH:mm:ss）") @RequestParam(required = false) LocalDateTime startTime,
            @Parameter(description = "结束时间（可选）") @RequestParam(required = false) LocalDateTime endTime,
            @Parameter(description = "所属区域（维修人员仅能查询自己的区域）") @RequestParam(required = false) String areaId
    ) {
        List<Alert> alerts;

        if (deviceId != null) {
            alerts = alertRepository.findByDeviceIdAndTimestampBetween(deviceId, startTime, endTime);
        } else if (level != null) {
            alerts = alertRepository.findByAlertLevelAndTimestampBetween(
                    Alert.AlertLevel.valueOf(level), startTime, endTime);
        } else if (status != null) {
            alerts = alertRepository.findByStatusAndTimestampBetween(
                    Alert.AlertStatus.valueOf(status), startTime, endTime);
        } else if (areaId != null) {
            alerts = alertRepository.findByAreaIdAndTimestampBetween(areaId, startTime, endTime);
        } else {
            alerts = alertRepository.findByTimestampBetween(startTime, endTime);
        }

        return ResultVO.success(alerts);
    }

    /**
     * 查询未处理告警（紧急优先）
     */
    // AlertController.java

    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ROLE_SUPER_ADMIN','ROLE_AREA_ADMIN', 'ROLE_REPAIRMAN')") // 添加 ROLE_ 前缀
    public ResultVO<List<Alert>> getPendingAlerts(
            @Parameter(description = "区域ID（可选）") @RequestParam(required = false) String areaId) {

        List<Alert> pendingAlerts = areaId != null
                ? alertRepository.findByAreaIdAndStatus(areaId, Alert.AlertStatus.pending)
                : alertRepository.findByStatus(Alert.AlertStatus.pending);

        // 按优先级排序（紧急在前）
        pendingAlerts.sort((a1, a2) ->
                Integer.compare(a2.getAlertLevel().getPriority(), a1.getAlertLevel().getPriority()));

        return ResultVO.success(pendingAlerts);
    }

    // 添加分页查询接口
@GetMapping("/all")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','AREA_ADMIN', 'REPAIRMAN')")
@Operation(summary = "查询所有告警（支持多条件筛选）")
public ResultVO<List<Alert>> getAllAlerts(
        @Parameter(description = "设备ID（可选）") @RequestParam(required = false) String deviceId,
        @Parameter(description = "告警级别（可选，如error、critical）") @RequestParam(required = false) String level,
        @Parameter(description = "告警状态（可选，如pending、resolved）") @RequestParam(required = false) String status,
        @Parameter(description = "所属区域（维修人员仅能查询自己的区域）") @RequestParam(required = false) String areaId
) {
    List<Alert> alerts;

    if (deviceId != null) {
        alerts = alertRepository.findByDeviceId(deviceId);
    } else if (level != null) {
        alerts = alertRepository.findByAlertLevel(Alert.AlertLevel.valueOf(level));
    } else if (status != null) {
        alerts = alertRepository.findByStatus(Alert.AlertStatus.valueOf(status));
    } else if (areaId != null) {
        alerts = alertRepository.findByAreaId(areaId);
    } else {
        alerts = alertRepository.findAll();
    }

    return ResultVO.success(alerts);
}


}