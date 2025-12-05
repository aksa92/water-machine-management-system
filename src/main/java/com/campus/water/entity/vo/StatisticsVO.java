/**
 * 统计数据显示视图对象（VO）
 * 功能：封装统计数据查询的响应结果，包含总计、明细、时间序列等数据结构
 * 用途：用于前后端数据交互，展示用水量、告警等统计信息
 * 结构：
 *   - StatisticsVO: 主统计响应类
 *   - StatItemVO: 统计明细项（按设备/区域/时间维度）
 *   - TimeSeriesVO: 时间序列数据（用于图表展示）
 */
package com.campus.water.entity.vo;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class StatisticsVO {
    private Integer totalCount;          // 总数量
    private Double totalAmount;          // 总用水量/总金额
    private Double avgAmount;           // 平均值
    private String period;              // 统计周期：day/week/month/year
    private LocalDate startDate;        // 统计开始日期
    private LocalDate endDate;          // 统计结束日期

    // 明细数据
    private List<StatItemVO> items;

    @Data
    public static class StatItemVO {
        private String dimensionKey;     // 维度键：设备ID/区域ID/时间
        private String dimensionValue;   // 维度值：设备名称/区域名称/时间标签
        private Integer count;           // 数量
        private Double amount;           // 用水量/金额
        private Double percentage;       // 占比百分比
    }

    // 时间序列数据
    @Data
    public static class TimeSeriesVO {
        private List<String> timeLabels; // 时间标签
        private List<Double> values;     // 对应数值
    }
}