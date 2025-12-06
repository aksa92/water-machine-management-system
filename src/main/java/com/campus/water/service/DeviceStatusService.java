// 路径：com/campus/water/service/DeviceStatusService.java
package com.campus.water.service;

import com.campus.water.entity.Device;
import com.campus.water.entity.dto.request.DeviceStatusUpdateRequest;

import java.util.List;
import java.util.Map;

public interface DeviceStatusService {
    // 更新设备状态
    boolean updateDeviceStatus(DeviceStatusUpdateRequest request);

    // 标记设备在线
    boolean markDeviceOnline(String deviceId);

    // 标记设备离线
    boolean markDeviceOffline(String deviceId, String reason);

    // 标记设备故障
    boolean markDeviceFault(String deviceId, String faultType, String description);

    // 批量更新设备状态
    boolean batchUpdateDeviceStatus(List<String> deviceIds, String status, String remark);

    // 按状态查询设备
    List<Device> getDevicesByStatus(String status, String areaId, String deviceType);

    // 统计各状态设备数量
    Map<String, Object> getDeviceStatusCount(String areaId, String deviceType);

    // 获取超过阈值的离线设备
    List<Device> getOfflineDevicesExceedThreshold(Integer thresholdMinutes, String areaId);

    // 自动检测离线设备
    void autoDetectOfflineDevices(Integer thresholdMinutes);
}