package com.campus.water.controller;

import com.campus.water.entity.*;
import com.campus.water.mapper.*;
import com.campus.water.util.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/water-usage")
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
    @PostMapping("/scan")
    @PreAuthorize("hasAnyRole('STUDENT', 'SUPER_ADMIN', 'AREA_ADMIN', 'VIEWER')")
    @Transactional
    public ResultVO<Map<String, Object>> scanToDrink(
            @RequestParam String terminalId,
            @RequestParam String studentId,
            @RequestParam Double waterConsumption) {

        Map<String, Object> result = new HashMap<>();

        try {
            Optional<DeviceTerminalMapping> mappingOpt = deviceTerminalMappingRepository.findByTerminalId(terminalId);
            if (mappingOpt.isEmpty()) {
                return ResultVO.error("终端设备不存在");
            }

            DeviceTerminalMapping mapping = mappingOpt.get();
            if (mapping.getTerminalStatus() != DeviceTerminalMapping.TerminalStatus.active) {
                return ResultVO.error("终端设备未激活");
            }

            Optional<WaterMakerRealtimeData> realtimeDataOpt =
                    waterMakerRealtimeDataRepository.findLatestByDeviceId(mapping.getDeviceId());

            DrinkRecord drinkRecord = new DrinkRecord();
            drinkRecord.setStudentId(studentId);
            drinkRecord.setTerminalId(terminalId);
            drinkRecord.setDeviceId(mapping.getDeviceId());

            // Double转BigDecimal（适配DrinkRecord的BigDecimal类型字段）
            drinkRecord.setWaterConsumption(waterConsumption != null ? BigDecimal.valueOf(waterConsumption) : BigDecimal.ZERO);
            drinkRecord.setDrinkTime(LocalDateTime.now());
            drinkRecord.setLocation(mapping.getTerminalName());

            if (realtimeDataOpt.isPresent()) {
                WaterMakerRealtimeData realtimeData = realtimeDataOpt.get();
                drinkRecord.setTdsValue(realtimeData.getTdsValue3());
                drinkRecord.setWaterQuality(realtimeData.getWaterQuality());
            }

            drinkRecordRepository.save(drinkRecord);
            // 传入BigDecimal类型的用水量
            updateTerminalUsageStats(terminalId, BigDecimal.valueOf(waterConsumption));

            result.put("waterConsumption", waterConsumption);
            result.put("terminalName", mapping.getTerminalName());
            result.put("deviceId", mapping.getDeviceId());
            result.put("timestamp", LocalDateTime.now());

            return ResultVO.success(result, "用水成功");

        } catch (Exception e) {
            return ResultVO.error("用水失败: " + e.getMessage());
        }
    }

    // 更新终端使用统计（参数改为BigDecimal）
    private void updateTerminalUsageStats(String terminalId, BigDecimal waterConsumption) {
        LocalDateTime now = LocalDateTime.now();
        Optional<TerminalUsageStats> statsOpt = terminalUsageStatsRepository
                .findByTerminalIdAndStatDate(terminalId, now.toLocalDate());

        TerminalUsageStats stats;
        if (statsOpt.isPresent()) {
            stats = statsOpt.get();
            stats.setUsageCount(stats.getUsageCount() + 1);

            // BigDecimal加法（替代+运算符）
            stats.setTotalWaterOutput(stats.getTotalWaterOutput().add(waterConsumption));

            // BigDecimal除法（指定精度和舍入模式）
            stats.setAvgWaterPerUse(
                    stats.getTotalWaterOutput()
                            .divide(BigDecimal.valueOf(stats.getUsageCount()), 2, BigDecimal.ROUND_HALF_UP)
            );
        } else {
            stats = new TerminalUsageStats();
            stats.setTerminalId(terminalId);
            stats.setStatDate(now.toLocalDate());
            stats.setUsageCount(1);

            // 直接赋值BigDecimal（适配TerminalUsageStats的BigDecimal字段）
            stats.setTotalWaterOutput(waterConsumption);
            stats.setAvgWaterPerUse(waterConsumption);
            stats.setPeakHour(String.format("%02d:00", now.getHour()));
        }

        stats.setUpdatedTime(now);
        terminalUsageStatsRepository.save(stats);
    }

    // 获取水质信息
    @GetMapping("/quality/{deviceId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'SUPER_ADMIN', 'AREA_ADMIN', 'VIEWER')")
    public ResultVO<Map<String, Object>> getWaterQualityInfo(@PathVariable String deviceId) {
        Map<String, Object> result = new HashMap<>();

        try {
            Optional<WaterMakerRealtimeData> realtimeDataOpt =
                    waterMakerRealtimeDataRepository.findLatestByDeviceId(deviceId);

            Optional<WaterQualityHistory> qualityHistoryOpt =
                    waterQualityHistoryRepository.findLatestByDeviceId(deviceId);

            if (realtimeDataOpt.isPresent()) {
                WaterMakerRealtimeData realtimeData = realtimeDataOpt.get();
                result.put("deviceId", deviceId);
                // BigDecimal转Double返回给前端
                result.put("rawWaterTds", realtimeData.getTdsValue1() != null ? realtimeData.getTdsValue1().doubleValue() : null);
                result.put("pureWaterTds", realtimeData.getTdsValue2() != null ? realtimeData.getTdsValue2().doubleValue() : null);
                result.put("mineralWaterTds", realtimeData.getTdsValue3() != null ? realtimeData.getTdsValue3().doubleValue() : null);
                result.put("waterQuality", realtimeData.getWaterQuality());
                result.put("filterLife", realtimeData.getFilterLife());
                result.put("status", realtimeData.getStatus());
                result.put("updateTime", realtimeData.getRecordTime());
            }

            if (qualityHistoryOpt.isPresent()) {
                WaterQualityHistory qualityHistory = qualityHistoryOpt.get();
                result.put("lastDetectionTime", qualityHistory.getDetectedTime());
            }

            return ResultVO.success(result);

        } catch (Exception e) {
            return ResultVO.error("获取水质信息失败: " + e.getMessage());
        }
    }

    // 获取终端设备信息
    @GetMapping("/terminal/{terminalId}")
    @PreAuthorize("hasAnyRole('STUDENT', 'SUPER_ADMIN', 'AREA_ADMIN', 'VIEWER')")
    public ResultVO<Map<String, Object>> getTerminalInfo(@PathVariable String terminalId) {
        Map<String, Object> result = new HashMap<>();

        Optional<DeviceTerminalMapping> mappingOpt = deviceTerminalMappingRepository.findByTerminalId(terminalId);
        if (mappingOpt.isPresent()) {
            DeviceTerminalMapping mapping = mappingOpt.get();
            result.put("terminalId", terminalId);
            result.put("terminalName", mapping.getTerminalName());
            result.put("deviceId", mapping.getDeviceId());
            result.put("status", mapping.getTerminalStatus());

            // 获取水质信息
            ResultVO<Map<String, Object>> qualityResult = getWaterQualityInfo(mapping.getDeviceId());
            if (qualityResult.getCode() == 200 && qualityResult.getData() != null) {
                result.putAll(qualityResult.getData());
            }

            return ResultVO.success(result);
        } else {
            return ResultVO.error("终端设备不存在");
        }
    }
}