/**
 * Web端统计接口控制器
 * 功能：提供Web管理端的统计数据查询API接口
 * 用途：前后端分离架构中的后端API服务
 * 接口列表：
 *   1. POST /water-usage: 用水量统计（支持多维度）
 *   2. POST /alarm: 告警统计（次数、处理情况）
 *   3. GET /device-status: 设备状态数量统计
 *   4. GET /dashboard: 仪表板综合数据
 *   5. GET /hot-devices: 热门设备用水量排名
 * 技术：Spring MVC、参数验证、统一响应格式
 */
package com.campus.water.controller.web;

import com.campus.water.entity.dto.request.StatisticsQueryRequest;
import com.campus.water.entity.vo.AlarmStatisticsVO;
import com.campus.water.entity.vo.StatisticsVO;
import com.campus.water.service.StatisticsService;
import com.campus.water.util.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/web/statistics")
@RequiredArgsConstructor
@Tag(name = "统计分析接口", description = "Web管理端统计分析接口")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @PostMapping("/water-usage")
    @Operation(summary = "用水量统计", description = "按设备/区域/时间统计用水量")
    public ResponseEntity<ResultVO<StatisticsVO>> getWaterUsageStatistics(
            @Valid @RequestBody StatisticsQueryRequest request) {
        try {
            StatisticsVO result = statisticsService.getWaterUsageStatistics(request);
            return ResponseEntity.ok(ResultVO.success(result));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "统计失败: " + e.getMessage()));
        }
    }

    @PostMapping("/alarm")
    @Operation(summary = "告警统计", description = "统计告警次数和处理情况")
    public ResponseEntity<ResultVO<AlarmStatisticsVO>> getAlarmStatistics(
            @Valid @RequestBody StatisticsQueryRequest request) {
        try {
            AlarmStatisticsVO result = statisticsService.getAlarmStatistics(request);
            return ResponseEntity.ok(ResultVO.success(result));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "告警统计失败: " + e.getMessage()));
        }
    }

    @GetMapping("/device-status")
    @Operation(summary = "设备状态统计", description = "统计各状态设备数量")
    public ResponseEntity<ResultVO<Map<String, Object>>> getDeviceStatusStatistics(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String deviceType) {
        try {
            Map<String, Object> result = statisticsService.getDeviceStatusStatistics(areaId, deviceType);
            return ResponseEntity.ok(ResultVO.success(result));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "设备状态统计失败: " + e.getMessage()));
        }
    }

    @GetMapping("/dashboard")
    @Operation(summary = "仪表盘数据", description = "获取综合仪表盘统计数据")
    public ResponseEntity<ResultVO<Map<String, Object>>> getDashboardStatistics() {
        try {
            Map<String, Object> result = statisticsService.getDashboardStatistics();
            return ResponseEntity.ok(ResultVO.success(result));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "获取仪表盘数据失败: " + e.getMessage()));
        }
    }

    @GetMapping("/hot-devices")
    @Operation(summary = "热门设备统计", description = "获取用水量最高的设备")
    public ResponseEntity<ResultVO<StatisticsVO>> getHotDevices(
            @RequestParam(defaultValue = "7") Integer days,
            @RequestParam(defaultValue = "10") Integer limit) {
        try {
            StatisticsQueryRequest request = new StatisticsQueryRequest();
            request.setStatType("by_device");
            request.setStartDate(java.time.LocalDate.now().minusDays(days));
            request.setEndDate(java.time.LocalDate.now());
            request.setLimit(limit);

            StatisticsVO result = statisticsService.getWaterUsageStatistics(request);
            return ResponseEntity.ok(ResultVO.success(result));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "获取热门设备失败: " + e.getMessage()));
        }
    }
}