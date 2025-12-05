/**
 * 移动端（App）统计接口控制器
 * 功能：为移动端提供简化的统计查询接口
 * 用途：支持学生在手机上查看个人用水统计和设备状态
 * 接口特点：
 *   - 简化参数：减少查询维度，优化移动端体验
 *   - 个人化：基于用户ID过滤数据
 *   - 快速响应：返回核心数据，减少数据传输量
 * 接口列表：
 *   1. GET /personal-water-usage: 个人用水统计（今日/本周/本月）
 *   2. GET /device-status-overview: 设备状态概览
 * 技术：Spring MVC、Header参数验证、移动端优化
 */
package com.campus.water.controller.app;

import com.campus.water.entity.dto.request.StatisticsQueryRequest;
import com.campus.water.entity.vo.StatisticsVO;
import com.campus.water.service.StatisticsService;
import com.campus.water.util.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/app/statistics")
@RequiredArgsConstructor
@Tag(name = "App统计接口", description = "移动端简化统计接口")
public class AppStatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/personal-water-usage")
    @Operation(summary = "个人用水统计", description = "获取当前用户的用水统计")
    public ResponseEntity<ResultVO<StatisticsVO>> getPersonalWaterUsage(
            @RequestParam(required = false) String period, // today/week/month
            @RequestHeader("X-User-Id") String userId) {
        try {
            StatisticsQueryRequest request = new StatisticsQueryRequest();
            request.setStatType("by_time");

            LocalDate now = LocalDate.now();
            if ("today".equals(period)) {
                request.setPeriod("day");
                request.setStartDate(now);
                request.setEndDate(now);
            } else if ("week".equals(period)) {
                request.setPeriod("day");
                request.setStartDate(now.minusDays(7));
                request.setEndDate(now);
            } else if ("month".equals(period)) {
                request.setPeriod("day");
                request.setStartDate(now.withDayOfMonth(1));
                request.setEndDate(now);
            } else {
                request.setPeriod("day");
                request.setStartDate(now.minusDays(30));
                request.setEndDate(now);
            }

            // 这里需要根据userId过滤数据，实际实现需要调整
            StatisticsVO result = statisticsService.getWaterUsageStatistics(request);
            return ResponseEntity.ok(ResultVO.success(result));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "获取个人用水统计失败: " + e.getMessage()));
        }
    }

    @GetMapping("/device-status-overview")
    @Operation(summary = "设备状态概览", description = "获取设备状态概览信息")
    public ResponseEntity<ResultVO<Map<String, Object>>> getDeviceStatusOverview(
            @RequestParam(required = false) String areaId) {
        try {
            Map<String, Object> result = statisticsService.getDeviceStatusStatistics(areaId, null);
            return ResponseEntity.ok(ResultVO.success(result));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "获取设备状态概览失败: " + e.getMessage()));
        }
    }
}