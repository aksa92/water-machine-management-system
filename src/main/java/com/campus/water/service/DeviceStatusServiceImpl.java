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

    /**
     * 按状态加载设备（支持区域筛选）
     * @param status 设备状态（字符串类型，需转换为枚举）
     * @param areaId 区域ID（可为null，null时查询所有区域）
     */
    @Override
    public List<Device> getDevicesByStatusWithArea(String status, String areaId) {
        try {
            Device.DeviceStatus targetStatus = Device.DeviceStatus.valueOf(status);
            if (areaId != null && !areaId.isEmpty()) {
                return deviceRepository.findByStatusAndAreaId(targetStatus, areaId);
            } else {
                return deviceRepository.findByStatus(targetStatus);
            }
        } catch (IllegalArgumentException e) {
            log.error("设备状态枚举转换失败，状态值：{}", status, e);
            throw new RuntimeException("无效的设备状态：" + status);
        }
    }

    /**
     * 按设备类型加载设备（支持区域筛选）
     * @param deviceType 设备类型（字符串类型，需转换为枚举）
     * @param areaId 区域ID（可为null，null时查询所有区域）
     */
    @Override
    public List<Device> getDevicesByTypeWithArea(String deviceType, String areaId) {
        try {
            Device.DeviceType targetType = Device.DeviceType.valueOf(deviceType);
            if (areaId != null && !areaId.isEmpty()) {
                return deviceRepository.findByDeviceTypeAndAreaId(targetType, areaId);
            } else {
                return deviceRepository.findByDeviceType(targetType);
            }
        } catch (IllegalArgumentException e) {
            log.error("设备类型枚举转换失败，类型值：{}", deviceType, e);
            throw new RuntimeException("无效的设备类型：" + deviceType);
        }
    }

    @Override
    public Map<String, Object> getDeviceStatusCount(String areaId, String deviceType) {
        try {
            Device.DeviceType targetType = Device.DeviceType.valueOf(deviceType);
            return Map.of(
                    "online", deviceRepository.countByStatusAndAreaIdAndDeviceType(Device.DeviceStatus.online, areaId, targetType),
                    "offline", deviceRepository.countByStatusAndAreaIdAndDeviceType(Device.DeviceStatus.offline, areaId, targetType),
                    "fault", deviceRepository.countByStatusAndAreaIdAndDeviceType(Device.DeviceStatus.fault, areaId, targetType)
            );
        } catch (IllegalArgumentException e) {
            log.error("设备类型枚举转换失败，类型值：{}", deviceType, e);
            throw new RuntimeException("无效的设备类型：" + deviceType);
        }
    }

    @Override
    public List<Device> getOfflineDevicesExceedThreshold(Integer thresholdMinutes, String areaId) {
        return deviceRepository.findByAreaIdAndStatus(areaId, Device.DeviceStatus.offline);
    }

    @Override
    public void autoDetectOfflineDevices(Integer thresholdMinutes) {
        log.info("自动检测离线设备（不执行时间判断，仅依赖手动标记）");
    }
}