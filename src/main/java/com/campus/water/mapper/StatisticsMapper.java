/**
 * 统计数据处理映射接口（MyBatis Mapper）
 * 功能：定义统计数据查询的数据库操作方法
 * 用途：与数据库交互，执行复杂的统计聚合查询
 * 核心方法：
 *   - 按设备/区域/时间维度统计用水量
 *   - 按设备/区域统计告警次数
 *   - 获取设备状态统计和告警处理统计
 * 备注：对应StatisticsMapper.xml中的SQL映射
 */
package com.campus.water.mapper;

import com.campus.water.entity.dto.request.StatisticsQueryRequest;
import com.campus.water.entity.vo.StatisticsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface StatisticsMapper {

    // 按设备统计用水量
    List<Map<String, Object>> statWaterUsageByDevice(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("areaId") String areaId,
            @Param("limit") Integer limit);

    // 按区域统计用水量
    List<Map<String, Object>> statWaterUsageByArea(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("deviceType") String deviceType,
            @Param("limit") Integer limit);

    // 按时间段统计用水量（日/周/月）
    List<Map<String, Object>> statWaterUsageByTime(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("period") String period,  // day/week/month
            @Param("deviceId") String deviceId,
            @Param("areaId") String areaId);

    // 按设备统计告警次数
    List<Map<String, Object>> statAlarmCountByDevice(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("areaId") String areaId,
            @Param("alarmLevel") String alarmLevel,
            @Param("limit") Integer limit);

    // 按区域统计告警次数
    List<Map<String, Object>> statAlarmCountByArea(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("deviceType") String deviceType,
            @Param("limit") Integer limit);

    // 按时间段统计告警趋势
    List<Map<String, Object>> statAlarmTrendByTime(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("period") String period,
            @Param("deviceId") String deviceId,
            @Param("areaId") String areaId);

    // 统计设备使用情况（使用次数、总用水量）
    List<Map<String, Object>> statDeviceUsage(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("areaId") String areaId,
            @Param("deviceType") String deviceType,
            @Param("limit") Integer limit);

    // 统计终端使用情况
    List<Map<String, Object>> statTerminalUsage(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("areaId") String areaId,
            @Param("limit") Integer limit);

    // 统计水质达标率
    List<Map<String, Object>> statWaterQualityRate(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("deviceId") String deviceId,
            @Param("areaId") String areaId);

    // 获取设备状态统计
    Map<String, Object> getDeviceStatusStatistics(
            @Param("areaId") String areaId,
            @Param("deviceType") String deviceType);

    // 获取告警处理统计
    Map<String, Object> getAlarmHandleStatistics(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("areaId") String areaId);
}