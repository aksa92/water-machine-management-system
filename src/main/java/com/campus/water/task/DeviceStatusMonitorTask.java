/**
 * 设备状态监控定时任务
 * 功能：定时 */
package com.campus.water.task;

import com.campus.water.service.DeviceStatusService; // 添加这行导入
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeviceStatusMonitorTask {

    private final DeviceStatusService deviceStatusService; // 现在可以正确识别

    // 后续代码不变...
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
            // 补充实际统计逻辑，例如：
            // deviceStatusService.collectAndSaveStatistics();
            log.info("设备状态统计收集完成");
        } catch (Exception e) {
            log.error("统计收集失败: {}", e.getMessage(), e);
        }
    }
}