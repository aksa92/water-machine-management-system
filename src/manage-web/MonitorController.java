package com.campus.water.web.controller;

import com.campus.water.web.entity.SensorDataWebVO;
import com.campus.water.web.service.RealTimeService;
import com.campus.water.web.service.HistoryDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 监控数据接口（供Web前端Vue调用，适配管理员“监控设备数据”需求）
 */
@RestController
@RequestMapping("/api/admin/monitor")
public class MonitorController {
    @Autowired
    private RealTimeService realTimeService;
    @Autowired
    private HistoryDataService historyDataService;

    // 1. 获取单设备实时数据（适配前端设备详情页）
    @GetMapping("/realTime/{deviceId}")
    public SensorDataWebVO getRealTimeData(@PathVariable String deviceId) {
        return realTimeService.getRealTimeData(deviceId);
    }

    // 2. 获取全量设备实时状态（适配前端监控总览页）
    @GetMapping("/realTime/all")
    public List<SensorDataWebVO> getAllRealTimeData() {
        return realTimeService.getAllRealTimeData();
    }

    // 3. 获取设备历史数据（适配前端历史曲线，需求中“查看历史水质”）
    @GetMapping("/history/{deviceId}")
    public List<SensorDataWebVO> getHistoryData(
            @PathVariable String deviceId,
            @RequestParam String startTime, // 开始时间（如2024-05-20 00:00:00）
            @RequestParam String endTime    // 结束时间
    ) {
        return historyDataService.getHistoryData(deviceId, startTime, endTime);
    }
}