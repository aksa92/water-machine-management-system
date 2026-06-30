/**
 * 设备状态监控定时任务
 * 功能：定时检测离线设备、收集设备状态统计
 */
package com.campus.water.task;

import com.campus.water.entity.Device;
import com.campus.water.Repository.DeviceRepository;
import com.campus.water.service.DeviceStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeviceStatusMonitorTask {

    private final DeviceStatusService deviceStatusService;
    private final DeviceRepository deviceRepository;

    @Scheduled(fixedRate = 300000)
    public void monitorOfflineDevices() {
        log.info("开始自动检测离线设备...");
        try {
            deviceStatusService.autoDetectOfflineDevices(30);
        } catch (Exception e) {
            log.error("离线设备检测任务失败: {}", e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 0 * * * ?")
    public void collectDeviceStatusStatistics() {
        log.info("开始收集设备状态统计...");
        try {
            long online = deviceRepository.findByStatus(Device.DeviceStatus.online).size();
            long offline = deviceRepository.findByStatus(Device.DeviceStatus.offline).size();
            long fault = deviceRepository.findByStatus(Device.DeviceStatus.fault).size();
            long total = online + offline + fault;
            log.info("设备状态统计 | 总计：{} | 在线：{} | 离线：{} | 故障：{}",
                    total, online, offline, fault);
        } catch (Exception e) {
            log.error("统计收集失败: {}", e.getMessage(), e);
        }
    }
}
