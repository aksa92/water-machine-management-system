package com.campus.water.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/monitor")
public class MonitorController {
    @Autowired
    private RealTimeService realTimeService;
    @Autowired
    private HistoryDataService historyDataService;

    @GetMapping("/realTime/{deviceId}")
    public Object getRealTimeData(@PathVariable String deviceId) {
        return realTimeService.getRealTimeData(deviceId);
    }

    @GetMapping("/realTime/all")
    public List<Object> getAllRealTimeData() {
        return realTimeService.getAllRealTimeData();
    }

    @GetMapping("/history/{deviceId}")
    public List<Object> getHistoryData(
            @PathVariable String deviceId,
            @RequestParam String startTime,
            @RequestParam String endTime
    ) {
        return historyDataService.getHistoryData(deviceId, startTime, endTime);
    }
}