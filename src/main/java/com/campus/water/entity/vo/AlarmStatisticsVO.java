/**
 * 告警统计数据视图对象（VO）
 * 功能：封装告警相关的统计数据，包括告警级别、类型、设备分布等
 * 用途：展示告警统计分析和处理情况
 * 结构：
 *   - AlarmStatisticsVO: 告警统计主类
 *   - DeviceAlarmStatVO: 设备告警统计明细项
 */
package com.campus.water.entity.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AlarmStatisticsVO {
    private Integer totalAlarms;                // 总告警数
    private Integer pendingAlarms;              // 未处理告警数
    private Integer resolvedAlarms;             // 已处理告警数
    private Double averageResponseTime;         // 平均响应时间(小时)

    // 按级别统计
    private Map<String, Integer> alarmLevelCount;

    // 按类型统计
    private Map<String, Integer> alarmTypeCount;

    // 按设备统计
    private List<DeviceAlarmStatVO> deviceAlarmStats;

    @Data
    public static class DeviceAlarmStatVO {
        private String deviceId;
        private String deviceName;
        private Integer totalAlarms;
        private Integer pendingAlarms;
        private Integer resolvedAlarms;
        private String mostCommonAlarmType;     // 最常见告警类型
    }
}