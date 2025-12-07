package com.campus.water.service;

import com.campus.water.entity.Alert;
import com.campus.water.entity.Device;
import com.campus.water.entity.TerminalUsageStats;
import com.campus.water.entity.dto.request.StatisticsQueryRequest;
import com.campus.water.entity.vo.AlarmStatisticsVO;
import com.campus.water.entity.vo.StatisticsVO;
import com.campus.water.mapper.AlertRepository;
import com.campus.water.mapper.DeviceRepository;
import com.campus.water.mapper.TerminalUsageStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final TerminalUsageStatsRepository terminalUsageStatsRepository;
    private final DeviceRepository deviceRepository;
    private final AlertRepository alertRepository;

    /**
     * 用水量统计（按设备/区域/时间维度）
     */
    public StatisticsVO getWaterUsageStatistics(StatisticsQueryRequest request) {
        StatisticsVO result = new StatisticsVO();
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX);

        // 按终端ID统计
        if (request.getTerminalId() != null && !request.getTerminalId().isEmpty()) {
            result.setType("terminal");
            List<TerminalUsageStats> stats = terminalUsageStatsRepository
                    .findByTerminalIdAndStatDateBetween(request.getTerminalId(), startDate, endDate);

            List<String> dates = stats.stream()
                    .map(stat -> stat.getStatDate().toString())
                    .collect(Collectors.toList());

            // 修复1：BigDecimal转Double（处理null值，避免空指针）
            List<Double> waterUsage = stats.stream()
                    .map(stat -> stat.getTotalWaterOutput() != null ? stat.getTotalWaterOutput().doubleValue() : 0.0)
                    .collect(Collectors.toList());

            result.setDates(dates);
            result.setWaterUsage(waterUsage);

            // 求和（Double类型直接计算）
            double total = waterUsage.stream().mapToDouble(Double::doubleValue).sum();
            result.setTotalUsage(total);
            result.setAvgDailyUsage(dates.size() > 0 ? total / dates.size() : 0);
            return result;
        }

        // 按设备统计
        if ("by_device".equals(request.getStatType())) {
            result.setType("device");
            List<Device> devices = deviceRepository.findByAreaId(request.getAreaId());
            List<String> deviceIds = devices.stream()
                    .map(Device::getDeviceId)
                    .collect(Collectors.toList());

            Map<String, Double> deviceTotal = new HashMap<>();
            for (String deviceId : deviceIds) {
                List<TerminalUsageStats> stats = terminalUsageStatsRepository
                        .findByTerminalIdAndStatDateBetween(deviceId, startDate, endDate);

                // 修复2：mapToDouble中处理BigDecimal转double（含null值）
                double total = stats.stream()
                        .mapToDouble(stat -> stat.getTotalWaterOutput() != null ? stat.getTotalWaterOutput().doubleValue() : 0.0)
                        .sum();
                deviceTotal.put(deviceId, total);
            }

            List<Map.Entry<String, Double>> sorted = new ArrayList<>(deviceTotal.entrySet());
            sorted.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));
            result.setDeviceStats(sorted.stream()
                    .map(entry -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("deviceId", entry.getKey());
                        item.put("totalUsage", entry.getValue());
                        return item;
                    })
                    .collect(Collectors.toList()));
            return result;
        }

        // 按区域统计
        if ("by_area".equals(request.getStatType())) {
            result.setType("area");
            result.setAreaId(request.getAreaId());
            List<TerminalUsageStats> stats = terminalUsageStatsRepository
                    .findByStatDateBetween(startDate, endDate);

            // 修复3：BigDecimal转double求和
            double total = stats.stream()
                    .mapToDouble(stat -> stat.getTotalWaterOutput() != null ? stat.getTotalWaterOutput().doubleValue() : 0.0)
                    .sum();
            result.setTotalUsage(total);
            return result;
        }

        return result;
    }

    /**
     * 告警统计（次数、处理情况）
     */
    public AlarmStatisticsVO getAlarmStatistics(StatisticsQueryRequest request) {
        AlarmStatisticsVO result = new AlarmStatisticsVO();
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atTime(LocalTime.MAX);

        List<Alert> alerts;
        // 按终端ID筛选告警
        if (request.getTerminalId() != null && !request.getTerminalId().isEmpty()) {
            alerts = alertRepository.findByDeviceIdAndTimestampBetween(request.getTerminalId(), startTime, endTime);
        } else {
            alerts = alertRepository.findByTimestampBetween(startTime, endTime);
        }

        // 统计告警级别分布
        Map<String, Long> levelCount = alerts.stream()
                .map(alert -> alert.getAlertLevel().name()) // 获取告警级别枚举名称
                .collect(Collectors.groupingBy(level -> level, Collectors.counting()));
        result.setLevelCount(levelCount);

        // 统计告警状态分布
        Map<String, Long> statusCount = alerts.stream()
                .map(alert -> alert.getStatus().name()) // 获取告警状态枚举名称
                .collect(Collectors.groupingBy(status -> status, Collectors.counting()));
        result.setStatusCount(statusCount);

        // 计算处理率（避免除零错误）
        long total = alerts.size();
        long resolved = alerts.stream()
                .filter(alert -> alert.getStatus() == Alert.AlertStatus.resolved)
                .count();
        double handleRate = total > 0 ? (double) resolved / total * 100 : 0.0;
        result.setHandleRate(handleRate);

        return result;
    }

    // 其他统计方法
    public Map<String, Object> getDeviceStatusStatistics(String areaId, String deviceType) {
        return new HashMap<>();
    }

    public Map<String, Object> getDashboardStatistics() {
        return new HashMap<>();
    }
}