package com.campus.water.controller;

import com.campus.water.entity.*;
import com.campus.water.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Controller
public class WaterUsageController {

    @Autowired
    private DeviceTerminalMappingRepository deviceTerminalMappingRepository;

    @Autowired
    private WaterMakerRealtimeDataRepository waterMakerRealtimeDataRepository;

    @Autowired
    private WaterQualityHistoryRepository waterQualityHistoryRepository;

    @Autowired
    private DrinkRecordRepository drinkRecordRepository;

    @Autowired
    private TerminalUsageStatsRepository terminalUsageStatsRepository;

    // 扫码用水
    @Transactional
    public Map<String, Object> scanToDrink(String terminalId, String studentId, Double waterConsumption) {
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<DeviceTerminalMapping> mappingOpt = deviceTerminalMappingRepository.findByTerminalId(terminalId);
            if (mappingOpt.isEmpty()) {
                result.put("success", false);
                result.put("message", "终端设备不存在");
                return result;
            }

            DeviceTerminalMapping mapping = mappingOpt.get();
            if (mapping.getTerminalStatus() != DeviceTerminalMapping.TerminalStatus.active) {
                result.put("success", false);
                result.put("message", "终端设备未激活");
                return result;
            }

            Optional<WaterMakerRealtimeData> realtimeDataOpt =
                    waterMakerRealtimeDataRepository.findLatestByDeviceId(mapping.getDeviceId());

            DrinkRecord drinkRecord = new DrinkRecord();
            drinkRecord.setStudentId(studentId);
            drinkRecord.setTerminalId(terminalId);
            drinkRecord.setDeviceId(mapping.getDeviceId());
            drinkRecord.setWaterConsumption(waterConsumption);
            drinkRecord.setDrinkTime(LocalDateTime.now());
            drinkRecord.setLocation(mapping.getTerminalName());

            if (realtimeDataOpt.isPresent()) {
                WaterMakerRealtimeData realtimeData = realtimeDataOpt.get();
                drinkRecord.setTdsValue(realtimeData.getTdsValue3());
                drinkRecord.setWaterQuality(realtimeData.getWaterQuality());
            }

            drinkRecordRepository.save(drinkRecord);
            updateTerminalUsageStats(terminalId, waterConsumption);

            result.put("success", true);
            result.put("message", "用水成功");
            result.put("waterConsumption", waterConsumption);
            result.put("timestamp", LocalDateTime.now());
            return result;

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "用水失败: " + e.getMessage());
            return result;
        }
    }

    // 更新终端使用统计
    private void updateTerminalUsageStats(String terminalId, Double waterConsumption) {
        LocalDateTime now = LocalDateTime.now();
        Optional<TerminalUsageStats> statsOpt = terminalUsageStatsRepository
                .findByTerminalIdAndStatDate(terminalId, now.toLocalDate());

        TerminalUsageStats stats;
        if (statsOpt.isPresent()) {
            stats = statsOpt.get();
            stats.setUsageCount(stats.getUsageCount() + 1);
            stats.setTotalWaterOutput(stats.getTotalWaterOutput() + waterConsumption);
            stats.setAvgWaterPerUse(stats.getTotalWaterOutput() / stats.getUsageCount());
        } else {
            stats = new TerminalUsageStats();
            stats.setTerminalId(terminalId);
            stats.setStatDate(now.toLocalDate());
            stats.setUsageCount(1);
            stats.setTotalWaterOutput(waterConsumption);
            stats.setAvgWaterPerUse(waterConsumption);
            stats.setPeakHour(String.format("%02d:00", now.getHour()));
        }

        stats.setUpdatedTime(now);
        terminalUsageStatsRepository.save(stats);
    }

    // 获取水质信息
    public Map<String, Object> getWaterQualityInfo(String deviceId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<WaterMakerRealtimeData> realtimeDataOpt =
                    waterMakerRealtimeDataRepository.findLatestByDeviceId(deviceId);

            Optional<WaterQualityHistory> qualityHistoryOpt =
                    waterQualityHistoryRepository.findLatestByDeviceId(deviceId);

            if (realtimeDataOpt.isPresent()) {
                WaterMakerRealtimeData realtimeData = realtimeDataOpt.get();
                result.put("deviceId", deviceId);
                result.put("rawWaterTds", realtimeData.getTdsValue1());
                result.put("pureWaterTds", realtimeData.getTdsValue2());
                result.put("mineralWaterTds", realtimeData.getTdsValue3());
                result.put("waterQuality", realtimeData.getWaterQuality());
                result.put("filterLife", realtimeData.getFilterLife());
                result.put("status", realtimeData.getStatus());
                result.put("updateTime", realtimeData.getRecordTime());
            }

            if (qualityHistoryOpt.isPresent()) {
                WaterQualityHistory qualityHistory = qualityHistoryOpt.get();
                result.put("lastDetectionTime", qualityHistory.getDetectedTime());
            }

            result.put("success", true);
            return result;

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取水质信息失败: " + e.getMessage());
            return result;
        }
    }

    // 获取终端设备信息
    public Map<String, Object> getTerminalInfo(String terminalId) {
        Map<String, Object> result = new HashMap<>();

        Optional<DeviceTerminalMapping> mappingOpt = deviceTerminalMappingRepository.findByTerminalId(terminalId);
        if (mappingOpt.isPresent()) {
            DeviceTerminalMapping mapping = mappingOpt.get();
            result.put("terminalId", terminalId);
            result.put("terminalName", mapping.getTerminalName());
            result.put("deviceId", mapping.getDeviceId());
            result.put("status", mapping.getTerminalStatus());
            result.put("success", true);

            Map<String, Object> qualityInfo = getWaterQualityInfo(mapping.getDeviceId());
            result.putAll(qualityInfo);
        } else {
            result.put("success", false);
            result.put("message", "终端设备不存在");
        }

        return result;
    }
}