/**
 * 设备状态管理业务服务层
 * 功能：处理设备状态相关的业务逻辑
 * 用途：为设备状态控制器提供业务处理服务
 * 核心方法：
 *   - 状态更新：单设备状态变更
 *   - 状态标记：在线/离线/故障标记
 *   - 批量操作：批量状态更新
 *   - 自动检测：定时检测离线设备
 *   - 故障告警：设备故障时自动创建告警
 * 业务逻辑：状态验证、事务管理、日志记录
 */
package com.campus.water.service;

import com.campus.water.entity.Device;
import com.campus.water.entity.dto.request.DeviceStatusUpdateRequest;
import com.campus.water.mapper.DeviceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceService {

    private final DeviceMapper deviceMapper;
    private final AlertService alertService;

    /**
     * 更新设备状态
     */
    @Transactional
    public boolean updateDeviceStatus(DeviceStatusUpdateRequest request) {
        try {
            int rows = deviceMapper.updateDeviceStatus(
                    request.getDeviceId(),
                    request.getStatus(),
                    request.getRemark()
            );

            if (rows > 0) {
                log.info("设备状态更新成功: deviceId={}, status={}",
                        request.getDeviceId(), request.getStatus());

                // 如果是故障状态，自动创建告警
                if ("fault".equals(request.getStatus())) {
                    createFaultAlert(request.getDeviceId(), request.getRemark());
                }

                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("设备状态更新失败: deviceId={}, error={}",
                    request.getDeviceId(), e.getMessage(), e);
            throw new RuntimeException("设备状态更新失败: " + e.getMessage());
        }
    }

    /**
     * 标记设备在线
     */
    @Transactional
    public boolean markDeviceOnline(String deviceId) {
        try {
            int rows = deviceMapper.markDeviceOnline(deviceId);
            if (rows > 0) {
                log.info("设备标记为在线: deviceId={}", deviceId);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("标记设备在线失败: deviceId={}, error={}", deviceId, e.getMessage());
            throw new RuntimeException("标记设备在线失败: " + e.getMessage());
        }
    }

    /**
     * 标记设备离线
     */
    @Transactional
    public boolean markDeviceOffline(String deviceId, String reason) {
        try {
            int rows = deviceMapper.markDeviceOffline(deviceId, reason);
            if (rows > 0) {
                log.info("设备标记为离线: deviceId={}, reason={}", deviceId, reason);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("标记设备离线失败: deviceId={}, error={}", deviceId, e.getMessage());
            throw new RuntimeException("标记设备离线失败: " + e.getMessage());
        }
    }

    /**
     * 标记设备故障
     */
    @Transactional
    public boolean markDeviceFault(String deviceId, String faultType, String description) {
        try {
            int rows = deviceMapper.markDeviceFault(deviceId, faultType, description);
            if (rows > 0) {
                log.info("设备标记为故障: deviceId={}, type={}, desc={}",
                        deviceId, faultType, description);

                // 创建故障告警
                createFaultAlert(deviceId, String.format("故障类型: %s, 描述: %s", faultType, description));

                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("标记设备故障失败: deviceId={}, error={}", deviceId, e.getMessage());
            throw new RuntimeException("标记设备故障失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新设备状态
     */
    @Transactional
    public boolean batchUpdateDeviceStatus(List<String> deviceIds, String status, String remark) {
        try {
            if (deviceIds == null || deviceIds.isEmpty()) {
                return false;
            }

            int rows = deviceMapper.batchUpdateDeviceStatus(deviceIds, status, remark);
            log.info("批量更新设备状态: count={}, status={}, updated={}",
                    deviceIds.size(), status, rows);

            // 如果是故障状态，为每个设备创建告警
            if ("fault".equals(status)) {
                for (String deviceId : deviceIds) {
                    createFaultAlert(deviceId, remark);
                }
            }

            return rows > 0;
        } catch (Exception e) {
            log.error("批量更新设备状态失败: error={}", e.getMessage(), e);
            throw new RuntimeException("批量更新设备状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取设备状态统计
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getDeviceStatusCount(String areaId, String deviceType) {
        return deviceMapper.countByStatus(areaId, deviceType);
    }

    /**
     * 查询离线设备（超过阈值）
     */
    @Transactional(readOnly = true)
    public List<Device> getOfflineDevicesExceedThreshold(Integer thresholdMinutes, String areaId) {
        return deviceMapper.findOfflineDevicesExceedThreshold(thresholdMinutes, areaId);
    }

    /**
     * 获取设备最后在线时间
     */
    @Transactional(readOnly = true)
    public LocalDateTime getDeviceLastOnlineTime(String deviceId) {
        Device device = deviceMapper.getDeviceLastOnlineTime(deviceId);
        return device != null ? device.getUpdatedTime() : null;
    }

    /**
     * 根据状态查询设备
     */
    @Transactional(readOnly = true)
    public List<Device> getDevicesByStatus(String status, String areaId, String deviceType) {
        return deviceMapper.findByStatus(status, areaId, deviceType);
    }

    /**
     * 自动检测并标记离线设备
     */
    @Transactional
    public void autoDetectOfflineDevices(Integer thresholdMinutes) {
        List<Device> offlineDevices = getOfflineDevicesExceedThreshold(thresholdMinutes, null);

        for (Device device : offlineDevices) {
            markDeviceOffline(device.getDeviceId(),
                    String.format("自动检测离线，超过%d分钟无数据", thresholdMinutes));
        }

        if (!offlineDevices.isEmpty()) {
            log.warn("自动标记离线设备完成: count={}", offlineDevices.size());
        }
    }

    /**
     * 创建故障告警
     */
    private void createFaultAlert(String deviceId, String description) {
        try {
            alertService.createManualAlert(
                    deviceId,
                    "DEVICE_FAULT",
                    "设备故障",
                    String.format("设备故障告警 - 设备ID: %s, 描述: %s", deviceId, description),
                    "fault"
            );
        } catch (Exception e) {
            log.error("创建故障告警失败: deviceId={}, error={}", deviceId, e.getMessage());
        }
    }
}