/**
 * 统计查询请求数据传输对象（DTO）
 * 功能：接收前端统计查询的参数，支持多种统计维度和过滤条件
 * 用途：统一统计查询接口的入参规范
 * 参数：
 *   - statType: 统计类型（water_usage/alarm/device_usage）
 *   - period: 统计周期（day/week/month/year/custom）
 *   - startDate/endDate: 时间范围
 *   - deviceId/areaId: 设备/区域筛选
 */
package com.campus.water.entity.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

@Data
public class StatisticsQueryRequest {
    @NotBlank(message = "统计类型不能为空")
    private String statType;  // water_usage/alarm/device_usage

    private String period;    // day/week/month/year/custom

    private LocalDate startDate;

    private LocalDate endDate;

    private String deviceId;   // 设备ID（可选）

    private String areaId;     // 区域ID（可选）

    private String deviceType; // water_maker/water_supply

    private Integer limit = 10; // 返回条数限制
}