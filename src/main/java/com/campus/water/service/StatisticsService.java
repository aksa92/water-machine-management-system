/**
 * 统计业务逻辑服务层
 * 功能：处理统计数据的业务逻辑，包括数据转换、计算、聚合
 * 用途：为控制器提供统计数据处理服务
 * 核心方法：
 *   - getWaterUsageStatistics(): 用水量统计逻辑处理
 *   - getAlarmStatistics(): 告警统计逻辑处理
 *   - getDeviceStatusStatistics(): 设备状态统计
 *   - getDashboardStatistics(): 仪表板综合数据
 * 业务逻辑：数据验证、结果格式化、异常处理
 */
package com.campus.water.service;

import com.campus.water.entity.dto.request.StatisticsQueryRequest;
import com.campus.water.entity.vo.AlarmStatisticsVO;
import com.campus.water.entity.vo.StatisticsVO;
import com.campus.water.mapper.StatisticsMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {

    private final StatisticsMapper statisticsMapper;
    private final DeviceService deviceService;

    /**
     * 获取用水量统计
     */
    @Transactional(readOnly = true)
    public StatisticsVO getWaterUsageStatistics(StatisticsQueryRequest request) {
        StatisticsVO result = new StatisticsVO();
        result.setPeriod(request.getPeriod());
        result.setStartDate(request.getStartDate());
        result.setEndDate(request.getEndDate());

        List<Map<String, Object>> data;

        switch (request.getStatType()) {
            case "by_device":
                data = statisticsMapper.statWaterUsageByDevice(
                        request.getStartDate(), request.getEndDate(),
                        request.getAreaId(), request.getLimit());
                break;
            case "by_area":
                data = statisticsMapper.statWaterUsageByArea(
                        request.getStartDate(), request.getEndDate(),
                        request.getDeviceType(), request.getLimit());
                break;
            case "by_time":
                data = statisticsMapper.statWaterUsageByTime(
                        request.getStartDate(), request.getEndDate(),
                        request.getPeriod(), request.getDeviceId(),
                        request.getAreaId());
                break;
            default:
                throw new IllegalArgumentException("不支持的统计类型: " + request.getStatType());
        }

        // 计算总计
        double totalAmount = data.stream()
                .mapToDouble(item -> item.get("totalWaterOutput") != null ?
                        Double.parseDouble(item.get("totalWaterOutput").toString()) : 0)
                .sum();

        int totalCount = data.stream()
                .mapToInt(item -> item.get("usageCount") != null ?
                        Integer.parseInt(item.get("usageCount").toString()) : 0)
                .sum();

        result.setTotalCount(totalCount);
        result.setTotalAmount(totalAmount);
        result.setAvgAmount(totalCount > 0 ? totalAmount / totalCount : 0);

        // 构建明细项
        List<StatisticsVO.StatItemVO> items = new ArrayList<>();
        for (Map<String, Object> item : data) {
            StatisticsVO.StatItemVO statItem = new StatisticsVO.StatItemVO();

            if (request.getStatType().equals("by_time")) {
                statItem.setDimensionKey(item.get("timeLabel").toString());
                statItem.setDimensionValue(item.get("timeLabel").toString());
            } else if (request.getStatType().equals("by_device")) {
                statItem.setDimensionKey(item.get("deviceId").toString());
                statItem.setDimensionValue(item.get("deviceName") != null ?
                        item.get("deviceName").toString() : item.get("deviceId").toString());
            } else {
                statItem.setDimensionKey(item.get("areaId").toString());
                statItem.setDimensionValue(item.get("areaName") != null ?
                        item.get("areaName").toString() : item.get("areaId").toString());
            }

            Integer count = item.get("usageCount") != null ?
                    Integer.parseInt(item.get("usageCount").toString()) : 0;
            Double amount = item.get("totalWaterOutput") != null ?
                    Double.parseDouble(item.get("totalWaterOutput").toString()) : 0;

            statItem.setCount(count);
            statItem.setAmount(amount);
            statItem.setPercentage(totalAmount > 0 ? (amount / totalAmount) * 100 : 0);

            items.add(statItem);
        }

        result.setItems(items);
        return result;
    }

    /**
     * 获取告警统计
     */
    @Transactional(readOnly = true)
    public AlarmStatisticsVO getAlarmStatistics(StatisticsQueryRequest request) {
        AlarmStatisticsVO result = new AlarmStatisticsVO();

        // 获取告警统计
        List<Map<String, Object>> alarmStats;

        if ("by_device".equals(request.getStatType())) {
            alarmStats = statisticsMapper.statAlarmCountByDevice(
                    request.getStartDate(), request.getEndDate(),
                    request.getAreaId(), null, request.getLimit());
        } else if ("by_area".equals(request.getStatType())) {
            alarmStats = statisticsMapper.statAlarmCountByArea(
                    request.getStartDate(), request.getEndDate(),
                    request.getDeviceType(), request.getLimit());
        } else {
            throw new IllegalArgumentException("不支持的告警统计类型: " + request.getStatType());
        }

        // 构建设备告警统计
        List<AlarmStatisticsVO.DeviceAlarmStatVO> deviceStats = alarmStats.stream()
                .map(item -> {
                    AlarmStatisticsVO.DeviceAlarmStatVO deviceStat = new AlarmStatisticsVO.DeviceAlarmStatVO();
                    deviceStat.setDeviceId(item.get("deviceId").toString());
                    deviceStat.setDeviceName(item.get("deviceName") != null ?
                            item.get("deviceName").toString() : "");
                    deviceStat.setTotalAlarms(Integer.parseInt(item.get("totalAlarms").toString()));
                    deviceStat.setPendingAlarms(Integer.parseInt(item.get("pendingAlarms").toString()));
                    deviceStat.setResolvedAlarms(Integer.parseInt(item.get("resolvedAlarms").toString()));
                    return deviceStat;
                })
                .collect(Collectors.toList());

        // 计算总数
        int totalAlarms = deviceStats.stream().mapToInt(AlarmStatisticsVO.DeviceAlarmStatVO::getTotalAlarms).sum();
        int pendingAlarms = deviceStats.stream().mapToInt(AlarmStatisticsVO.DeviceAlarmStatVO::getPendingAlarms).sum();
        int resolvedAlarms = deviceStats.stream().mapToInt(AlarmStatisticsVO.DeviceAlarmStatVO::getResolvedAlarms).sum();

        result.setTotalAlarms(totalAlarms);
        result.setPendingAlarms(pendingAlarms);
        result.setResolvedAlarms(resolvedAlarms);
        result.setDeviceAlarmStats(deviceStats);

        // 获取告警处理统计
        Map<String, Object> handleStats = statisticsMapper.getAlarmHandleStatistics(
                request.getStartDate(), request.getEndDate(), request.getAreaId());

        if (handleStats != null && handleStats.get("avgResponseHours") != null) {
            result.setAverageResponseTime(Double.parseDouble(handleStats.get("avgResponseHours").toString()));
        }

        return result;
    }

    /**
     * 获取设备状态统计
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getDeviceStatusStatistics(String areaId, String deviceType) {
        return statisticsMapper.getDeviceStatusStatistics(areaId, deviceType);
    }

    /**
     * 获取综合仪表盘数据
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> result = new HashMap<>();

        // 今日数据
        LocalDate today = LocalDate.now();
        StatisticsVO todayWaterUsage = getWaterUsageStatistics(createTodayQuery());
        result.put("todayWaterUsage", todayWaterUsage);

        // 本月数据
        StatisticsVO monthWaterUsage = getWaterUsageStatistics(createMonthQuery());
        result.put("monthWaterUsage", monthWaterUsage);

        // 设备状态统计
        Map<String, Object> deviceStats = getDeviceStatusStatistics(null, null);
        result.put("deviceStatus", deviceStats);

        // 告警统计
        AlarmStatisticsVO alarmStats = getAlarmStatistics(createAlarmQuery());
        result.put("alarmStatistics", alarmStats);

        // 热门设备（按用水量）
        StatisticsQueryRequest hotDevicesQuery = new StatisticsQueryRequest();
        hotDevicesQuery.setStatType("by_device");
        hotDevicesQuery.setStartDate(today.minusDays(7));
        hotDevicesQuery.setEndDate(today);
        hotDevicesQuery.setLimit(5);
        StatisticsVO hotDevices = getWaterUsageStatistics(hotDevicesQuery);
        result.put("hotDevices", hotDevices);

        return result;
    }

    private StatisticsQueryRequest createTodayQuery() {
        StatisticsQueryRequest request = new StatisticsQueryRequest();
        request.setStatType("by_time");
        request.setPeriod("day");
        request.setStartDate(LocalDate.now());
        request.setEndDate(LocalDate.now());
        return request;
    }

    private StatisticsQueryRequest createMonthQuery() {
        StatisticsQueryRequest request = new StatisticsQueryRequest();
        request.setStatType("by_time");
        request.setPeriod("month");
        request.setStartDate(LocalDate.now().withDayOfMonth(1));
        request.setEndDate(LocalDate.now());
        return request;
    }

    private StatisticsQueryRequest createAlarmQuery() {
        StatisticsQueryRequest request = new StatisticsQueryRequest();
        request.setStatType("by_device");
        request.setStartDate(LocalDate.now().minusDays(30));
        request.setEndDate(LocalDate.now());
        request.setLimit(10);
        return request;
    }
}