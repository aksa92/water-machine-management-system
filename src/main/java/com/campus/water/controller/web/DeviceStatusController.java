/**
 * Web端设备状态管理接口控制器
 * 功能：提供设备状态管理的RESTful API接口
 * 用途：支持Web管理端对设备状态的手动和自动管理
 * 接口列表：
 *   1. 状态更新：单设备状态变更
 *   2. 状态标记：在线/离线/故障快捷操作
 *   3. 批量操作：批量更新设备状态
 *   4. 状态查询：按状态筛选设备列表
 *   5. 离线检测：查询超时离线设备
 *   6. 自动检测：触发离线设备检测任务
 * 安全：需要权限验证，记录操作日志
 */
package com.campus.water.controller.web;

import com.campus.water.entity.Device;
import com.campus.water.entity.dto.request.DeviceStatusUpdateRequest;
import com.campus.water.service.DeviceStatusService;
import com.campus.water.util.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/web/device-status")
@RequiredArgsConstructor
@Tag(name = "设备状态管理接口", description = "Web管理端设备状态管理接口")
public class DeviceStatusController {

    private final DeviceStatusService deviceStatusService;

    @PostMapping("/update")
    @Operation(summary = "更新设备状态", description = "手动更新设备状态（在线/离线/故障）")
    public ResponseEntity<ResultVO<Boolean>> updateDeviceStatus(
            @Valid @RequestBody DeviceStatusUpdateRequest request) {
        try {
            boolean result = deviceStatusService.updateDeviceStatus(request);
            return ResponseEntity.ok(ResultVO.success(result, "设备状态更新成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "设备状态更新失败: " + e.getMessage()));
        }
    }

    @PostMapping("/{deviceId}/online")
    @Operation(summary = "标记设备在线", description = "将设备标记为在线状态")
    public ResponseEntity<ResultVO<Boolean>> markDeviceOnline(@PathVariable String deviceId) {
        try {
            boolean result = deviceStatusService.markDeviceOnline(deviceId);
            return ResponseEntity.ok(ResultVO.success(result, "设备已标记为在线"));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "标记设备在线失败: " + e.getMessage()));
        }
    }

    @PostMapping("/{deviceId}/offline")
    @Operation(summary = "标记设备离线", description = "将设备标记为离线状态")
    public ResponseEntity<ResultVO<Boolean>> markDeviceOffline(
            @PathVariable String deviceId,
            @RequestParam(required = false) String reason) {
        try {
            boolean result = deviceStatusService.markDeviceOffline(deviceId, reason);
            return ResponseEntity.ok(ResultVO.success(result, "设备已标记为离线"));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "标记设备离线失败: " + e.getMessage()));
        }
    }

    @PostMapping("/{deviceId}/fault")
    @Operation(summary = "标记设备故障", description = "将设备标记为故障状态")
    public ResponseEntity<ResultVO<Boolean>> markDeviceFault(
            @PathVariable String deviceId,
            @RequestParam String faultType,
            @RequestParam String description) {
        try {
            boolean result = deviceStatusService.markDeviceFault(deviceId, faultType, description);
            return ResponseEntity.ok(ResultVO.success(result, "设备已标记为故障"));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "标记设备故障失败: " + e.getMessage()));
        }
    }

    @PostMapping("/batch-update")
    @Operation(summary = "批量更新设备状态", description = "批量更新多个设备的状态")
    public ResponseEntity<ResultVO<Boolean>> batchUpdateDeviceStatus(
            @RequestParam List<String> deviceIds,
            @RequestParam String status,
            @RequestParam(required = false) String remark) {
        try {
            boolean result = deviceStatusService.batchUpdateDeviceStatus(deviceIds, status, remark);
            return ResponseEntity.ok(ResultVO.success(result, "批量更新设备状态成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "批量更新设备状态失败: " + e.getMessage()));
        }
    }

    @GetMapping("/by-status")
    @Operation(summary = "按状态查询设备", description = "根据状态和设备类型查询设备列表")
    public ResponseEntity<ResultVO<List<Device>>> getDevicesByStatus(
            @RequestParam String status,
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String deviceType) { // 保留设备类型参数，去除默认值

        try {
            // 调用服务层方法时传递所有参数（包括可能为null的deviceType）
            List<Device> devices = deviceStatusService.getDevicesByStatus(status, areaId, deviceType);
            return ResponseEntity.ok(ResultVO.success(devices));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "查询设备失败: " + e.getMessage()));
        }
    }


    @GetMapping("/status-count")
    @Operation(summary = "设备状态数量统计", description = "统计各状态设备数量")
    public ResponseEntity<ResultVO<Map<String, Object>>> getDeviceStatusCount(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String deviceType) {
        try {
            Map<String, Object> result = deviceStatusService.getDeviceStatusCount(areaId, deviceType);
            return ResponseEntity.ok(ResultVO.success(result));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "设备状态统计失败: " + e.getMessage()));
        }
    }

    @GetMapping("/offline-detection")
    @Operation(summary = "离线设备检测", description = "检测离线时间超过阈值的设备")
    public ResponseEntity<ResultVO<List<Device>>> getOfflineDevices(
            @RequestParam(defaultValue = "30") Integer thresholdMinutes,
            @RequestParam(required = false) String areaId) {
        try {
            List<Device> devices = deviceStatusService.getOfflineDevicesExceedThreshold(thresholdMinutes, areaId);
            return ResponseEntity.ok(ResultVO.success(devices));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "离线设备检测失败: " + e.getMessage()));
        }
    }

    @PostMapping("/auto-detect-offline")
    @Operation(summary = "自动检测离线设备", description = "自动检测并标记离线设备")
    public ResponseEntity<ResultVO<String>> autoDetectOfflineDevices(
            @RequestParam(defaultValue = "30") Integer thresholdMinutes) {
        try {
            deviceStatusService.autoDetectOfflineDevices(thresholdMinutes);
            return ResponseEntity.ok(ResultVO.success("离线设备检测完成"));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "自动检测离线设备失败: " + e.getMessage()));
        }
    }
}