// com/campus/water/service/DeviceStatusServiceImpl.java
package com.campus.water.service;

import com.campus.water.entity.Device;
import com.campus.water.entity.dto.request.DeviceStatusUpdateRequest;
import com.campus.water.mapper.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceStatusServiceImpl implements DeviceStatusService {

    private final DeviceRepository deviceRepository;

    @Override
    public boolean updateDeviceStatus(DeviceStatusUpdateRequest request) {
        Device device = deviceRepository.findById(request.getDeviceId()).orElse(null);
        if (device == null) {
            log.warn("设备不存在 | 设备ID：{}", request.getDeviceId());
            return false;
        }
        device.setStatus(request.getStatus());
        device.setRemark(request.getRemark());
        deviceRepository.save(device);
        return true;
    }

    @Override
    public boolean markDeviceOnline(String deviceId) {
        Device device = deviceRepository.findById(deviceId).orElse(null);
        if (device == null) return false;
        device.setStatus(Device.DeviceStatus.online);
        deviceRepository.save(device);
        return true;
    }

    @Override
    public boolean markDeviceOffline(String deviceId, String reason) {
        Device device = deviceRepository.findById(deviceId).orElse(null);
        if (device == null) return false;
        device.setStatus(Device.DeviceStatus.offline);
        device.setRemark(reason);
        deviceRepository.save(device);
        return true;
    }

    @Override
    public boolean markDeviceFault(String deviceId, String faultType, String description) {
        Device device = deviceRepository.findById(deviceId).orElse(null);
        if (device == null) return false;
        device.setStatus(Device.DeviceStatus.fault);
        device.setRemark("故障类型：" + faultType + "，描述：" + description);
        deviceRepository.save(device);
        return true;
    }

    @Override
    public boolean batchUpdateDeviceStatus(List<String> deviceIds, String status, String remark) {
        List<Device> devices = deviceRepository.findAllById(deviceIds);
        Device.DeviceStatus targetStatus = Device.DeviceStatus.valueOf(status);
        devices.forEach(device -> {
            device.setStatus(targetStatus);
            device.setRemark(remark);
        });
        deviceRepository.saveAll(devices);
        return true;
    }

    @Override
    public List<Device> getDevicesByStatus(String status, String areaId, String deviceType) {
        Device.DeviceStatus targetStatus = Device.DeviceStatus.valueOf(status);

        // 处理设备类型参数（允许为null）
        Device.DeviceType targetType = null;
        if (deviceType != null && !deviceType.isEmpty()) {
            targetType = Device.DeviceType.valueOf(deviceType);
        }

        // 根据设备类型是否为null执行不同查询
        if (targetType != null) {
            return deviceRepository.findByStatusAndAreaIdAndDeviceType(targetStatus, areaId, targetType);
        } else {
            // 仅按状态和区域查询（如果有区域ID）
            if (areaId != null && !areaId.isEmpty()) {
                return deviceRepository.findByStatusAndAreaId(targetStatus, areaId);
            } else {
                return deviceRepository.findByStatus(targetStatus);
            }
        }
    }

    @Override
    public Map<String, Object> getDeviceStatusCount(String areaId, String deviceType) {
        Device.DeviceType targetType = Device.DeviceType.valueOf(deviceType);
        return Map.of(
                "online", deviceRepository.countByStatusAndAreaIdAndDeviceType(Device.DeviceStatus.online, areaId, targetType),
                "offline", deviceRepository.countByStatusAndAreaIdAndDeviceType(Device.DeviceStatus.offline, areaId, targetType),
                "fault", deviceRepository.countByStatusAndAreaIdAndDeviceType(Device.DeviceStatus.fault, areaId, targetType)
        );
    }

    @Override
    public List<Device> getOfflineDevicesExceedThreshold(Integer thresholdMinutes, String areaId) {
        // 由于没有last_active_time，此处逻辑需调整：
        // 方案1：若设备有最近操作时间，可用作替代；
        // 方案2：仅返回状态为offline的设备（不判断时间）
        return deviceRepository.findByAreaIdAndStatus(areaId, Device.DeviceStatus.offline);
    }

    @Override
    public void autoDetectOfflineDevices(Integer thresholdMinutes) {
        // 同理，无last_active_time时，无法通过时间判断，可注释或简化逻辑
        log.info("自动检测离线设备（不执行时间判断，仅依赖手动标记）");
    }
}