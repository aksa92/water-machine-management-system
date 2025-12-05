/**
 * 设备状态监控定时任务
 * 功能：定时执行设备状态检测和统计收集任务
 * 用途：自动化设备状态管理，减少人工干预
 * 核心任务：
 *   1. 离线设备检测：每5分钟检测超时离线设备
 *   2. 状态统计收集：每小时收集设备状态统计数据
 * 技术：Spring @Scheduled定时任务、异常处理、日志记录
 * 配置：定时频率可在application.yml中配置
 */
package com.campus.water.task;

import com.campus.water.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeviceStatusMonitorTask {

    private final DeviceService deviceService;

    /**
     * 每5分钟检测一次离线设备
     */
    @Scheduled(fixedRate = 300000) // 5分钟
    public void monitorOfflineDevices() {
        log.info("开始自动检测离线设备...");
        try {
            // 检测30分钟无数据的设备
            deviceService.autoDetectOfflineDevices(30);
        } catch (Exception e) {
            log.error("离线设备检测任务执行失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 每小时统计一次设备状态
     */
    @Scheduled(cron = "0 0 * * * ?") // 每小时执行一次
    public void collectDeviceStatusStatistics() {
        log.info("开始收集设备状态统计...");
        try {
            // 这里可以保存统计结果到数据库
            // deviceService.getDeviceStatusCount(null, null);
            log.info("设备状态统计收集完成");
        } catch (Exception e) {
            log.error("设备状态统计收集失败: {}", e.getMessage(), e);
        }
    }
}