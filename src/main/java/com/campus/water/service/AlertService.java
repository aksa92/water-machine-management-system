package com.campus.water.service;

import com.campus.water.entity.Alert;
import com.campus.water.entity.WorkOrder;
import com.campus.water.entity.Device;
import com.campus.water.entity.dto.request.AlarmStatisticsRequest;
import com.campus.water.entity.vo.AlarmStatisticsVO;
import com.campus.water.mapper.AlertRepository;
import com.campus.water.mapper.WorkOrderRepository;
import com.campus.water.mapper.DeviceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 告警服务层
 * 功能：处理告警相关的业务逻辑，包括告警创建、查询、统计、处理等
 * 用途：
 *   1. 告警生命周期管理（创建、处理、关闭）
 *   2. 告警统计和分析
 *   3. 告警与工单关联管理
 *   4. 告警通知和推送
 * 核心方法：
 *   - 告警创建：自动触发告警、手动创建告警
 *   - 告警处理：更新告警状态、指派处理人
 *   - 告警统计：多维度的告警数据分析
 *   - 告警查询：支持多条件筛选和分页
 * 技术：Spring Data JPA、事务管理、复杂查询构建
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final AlertRepository alertRepository;
    private final WorkOrderRepository workOrderRepository;
    private final DeviceRepository deviceRepository;

    // ========== 告警创建相关方法 ==========

    /**
     * 自动创建告警（从传感器数据触发）
     * @param deviceId 设备ID
     * @param alertType 告警类型
     * @param alertLevel 告警级别
     * @param message 告警信息
     * @param areaId 区域ID
     * @return 创建的告警对象
     */
    @Transactional
    public Alert createAutoAlert(String deviceId, String alertType,
                                 Alert.AlertLevel alertLevel, String message,
                                 String areaId) {
        try {
            // 检查是否存在重复未处理告警
            if (hasDuplicatePendingAlert(deviceId, alertType)) {
                log.info("存在重复未处理告警，跳过创建: deviceId={}, alertType={}", deviceId, alertType);
                return null;
            }

            Alert alert = new Alert();
            alert.setDeviceId(deviceId);
            alert.setAlertType(alertType);
            alert.setAlertLevel(alertLevel);
            alert.setAlertMessage(message);
            alert.setAreaId(areaId);
            alert.setStatus(Alert.AlertStatus.pending);
            alert.setTimestamp(LocalDateTime.now());
            alert.setCreatedTime(LocalDateTime.now());

            Alert savedAlert = alertRepository.save(alert);
            log.info("自动告警创建成功: alertId={}, deviceId={}, type={}, level={}",
                    savedAlert.getAlertId(), deviceId, alertType, alertLevel);

            // 根据告警类型自动创建工单
            if (shouldCreateWorkOrder(alertType, alertLevel)) {
                createWorkOrderForAlert(savedAlert);
            }

            return savedAlert;
        } catch (Exception e) {
            log.error("自动告警创建失败: deviceId={}, error={}", deviceId, e.getMessage(), e);
            throw new RuntimeException("告警创建失败: " + e.getMessage());
        }
    }

    /**
     * 手动创建告警（由管理员或维修人员创建）
     * @param deviceId 设备ID
     * @param alertType 告警类型
     * @param alertLevel 告警级别字符串
     * @param message 告警信息
     * @param areaId 区域ID
     * @return 创建的告警对象
     */
    @Transactional
    public Alert createManualAlert(String deviceId, String alertType,
                                   String alertLevel, String message,
                                   String areaId) {
        try {
            Alert.AlertLevel level;
            try {
                level = Alert.AlertLevel.valueOf(alertLevel.toLowerCase());
            } catch (IllegalArgumentException e) {
                level = Alert.AlertLevel.warning; // 默认级别
            }

            Alert alert = new Alert();
            alert.setDeviceId(deviceId);
            alert.setAlertType(alertType);
            alert.setAlertLevel(level);
            alert.setAlertMessage(message);
            alert.setAreaId(areaId);
            alert.setStatus(Alert.AlertStatus.pending);
            alert.setTimestamp(LocalDateTime.now());
            alert.setCreatedTime(LocalDateTime.now());

            Alert savedAlert = alertRepository.save(alert);
            log.info("手动告警创建成功: alertId={}, deviceId={}, type={}, level={}",
                    savedAlert.getAlertId(), deviceId, alertType, alertLevel);

            // 手动创建的告警默认创建工单
            createWorkOrderForAlert(savedAlert);

            return savedAlert;
        } catch (Exception e) {
            log.error("手动告警创建失败: deviceId={}, error={}", deviceId, e.getMessage(), e);
            throw new RuntimeException("告警创建失败: " + e.getMessage());
        }
    }

    /**
     * 批量创建告警（用于批量导入或同步）
     * @param alerts 告警列表
     * @return 成功创建的告警列表
     */
    @Transactional
    public List<Alert> batchCreateAlerts(List<Alert> alerts) {
        try {
            List<Alert> savedAlerts = alertRepository.saveAll(alerts);
            log.info("批量创建告警成功: count={}", savedAlerts.size());

            // 为每个告警创建工单
            for (Alert alert : savedAlerts) {
                if (shouldCreateWorkOrder(alert.getAlertType(), alert.getAlertLevel())) {
                    createWorkOrderForAlert(alert);
                }
            }

            return savedAlerts;
        } catch (Exception e) {
            log.error("批量创建告警失败: error={}", e.getMessage(), e);
            throw new RuntimeException("批量创建告警失败: " + e.getMessage());
        }
    }

    // ========== 告警处理相关方法 ==========

    /**
     * 处理告警（开始处理）
     * @param alertId 告警ID
     * @param repairmanId 维修人员ID
     * @return 是否处理成功
     */
    @Transactional
    public boolean processAlert(Long alertId, String repairmanId) {
        try {
            Optional<Alert> alertOpt = alertRepository.findById(alertId);
            if (alertOpt.isEmpty()) {
                log.warn("告警不存在: alertId={}", alertId);
                return false;
            }

            Alert alert = alertOpt.get();
            if (alert.getStatus() != Alert.AlertStatus.pending) {
                log.warn("告警状态不允许处理: alertId={}, currentStatus={}", alertId, alert.getStatus());
                return false;
            }

            alert.setStatus(Alert.AlertStatus.processing);
            alert.setResolvedBy(repairmanId);
            alert.setUpdatedTime(LocalDateTime.now());

            alertRepository.save(alert);
            log.info("告警开始处理: alertId={}, repairmanId={}", alertId, repairmanId);
            return true;
        } catch (Exception e) {
            log.error("处理告警失败: alertId={}, error={}", alertId, e.getMessage(), e);
            throw new RuntimeException("处理告警失败: " + e.getMessage());
        }
    }

    /**
     * 解决告警（完成处理）
     * @param alertId 告警ID
     * @param resolvedBy 解决人
     * @param resolutionNotes 解决说明
     * @return 是否解决成功
     */
    @Transactional
    public boolean resolveAlert(Long alertId, String resolvedBy, String resolutionNotes) {
        try {
            Optional<Alert> alertOpt = alertRepository.findById(alertId);
            if (alertOpt.isEmpty()) {
                log.warn("告警不存在: alertId={}", alertId);
                return false;
            }

            Alert alert = alertOpt.get();
            if (alert.getStatus() != Alert.AlertStatus.processing &&
                    alert.getStatus() != Alert.AlertStatus.pending) {
                log.warn("告警状态不允许解决: alertId={}, currentStatus={}", alertId, alert.getStatus());
                return false;
            }

            alert.setStatus(Alert.AlertStatus.resolved);
            alert.setResolvedBy(resolvedBy);
            alert.setResolvedTime(LocalDateTime.now());
            alert.setAlertMessage(alert.getAlertMessage() + " [解决方案: " + resolutionNotes + "]");
            alert.setUpdatedTime(LocalDateTime.now());

            alertRepository.save(alert);
            log.info("告警已解决: alertId={}, resolvedBy={}", alertId, resolvedBy);
            return true;
        } catch (Exception e) {
            log.error("解决告警失败: alertId={}, error={}", alertId, e.getMessage(), e);
            throw new RuntimeException("解决告警失败: " + e.getMessage());
        }
    }

    /**
     * 关闭告警（无需处理或误报）
     * @param alertId 告警ID
     * @param closedBy 关闭人
     * @param closeReason 关闭原因
     * @return 是否关闭成功
     */
    @Transactional
    public boolean closeAlert(Long alertId, String closedBy, String closeReason) {
        try {
            Optional<Alert> alertOpt = alertRepository.findById(alertId);
            if (alertOpt.isEmpty()) {
                log.warn("告警不存在: alertId={}", alertId);
                return false;
            }

            Alert alert = alertOpt.get();
            alert.setStatus(Alert.AlertStatus.closed);
            alert.setResolvedBy(closedBy);
            alert.setResolvedTime(LocalDateTime.now());
            alert.setAlertMessage(alert.getAlertMessage() + " [关闭原因: " + closeReason + "]");
            alert.setUpdatedTime(LocalDateTime.now());

            alertRepository.save(alert);
            log.info("告警已关闭: alertId={}, closedBy={}, reason={}", alertId, closedBy, closeReason);
            return true;
        } catch (Exception e) {
            log.error("关闭告警失败: alertId={}, error={}", alertId, e.getMessage(), e);
            throw new RuntimeException("关闭告警失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新告警状态
     * @param alertIds 告警ID列表
     * @param status 目标状态
     * @param updatedBy 更新人
     * @return 成功更新的数量
     */
    @Transactional
    public int batchUpdateAlertStatus(List<Long> alertIds, Alert.AlertStatus status, String updatedBy) {
        try {
            List<Alert> alerts = alertRepository.findAllById(alertIds);
            for (Alert alert : alerts) {
                alert.setStatus(status);
                alert.setResolvedBy(updatedBy);
                if (status == Alert.AlertStatus.resolved || status == Alert.AlertStatus.closed) {
                    alert.setResolvedTime(LocalDateTime.now());
                }
                alert.setUpdatedTime(LocalDateTime.now());
            }

            alertRepository.saveAll(alerts);
            log.info("批量更新告警状态: count={}, status={}, updatedBy={}",
                    alerts.size(), status, updatedBy);
            return alerts.size();
        } catch (Exception e) {
            log.error("批量更新告警状态失败: error={}", e.getMessage(), e);
            throw new RuntimeException("批量更新告警状态失败: " + e.getMessage());
        }
    }

    // ========== 告警查询相关方法 ==========

    /**
     * 根据ID查询告警
     * @param alertId 告警ID
     * @return 告警对象（Optional）
     */
    @Transactional(readOnly = true)
    public Optional<Alert> findById(Long alertId) {
        return alertRepository.findById(alertId);
    }

    /**
     * 多条件分页查询告警
     * @param deviceId 设备ID（可选）
     * @param alertType 告警类型（可选）
     * @param alertLevel 告警级别（可选）
     * @param status 告警状态（可选）
     * @param areaId 区域ID（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @param pageable 分页参数
     * @return 分页告警列表
     */
    @Transactional(readOnly = true)
    public Page<Alert> findAlertsByConditions(String deviceId, String alertType,
                                              Alert.AlertLevel alertLevel, Alert.AlertStatus status,
                                              String areaId, LocalDateTime startTime,
                                              LocalDateTime endTime, Pageable pageable) {
        Specification<Alert> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(deviceId)) {
                predicates.add(criteriaBuilder.equal(root.get("deviceId"), deviceId));
            }

            if (StringUtils.hasText(alertType)) {
                predicates.add(criteriaBuilder.equal(root.get("alertType"), alertType));
            }

            if (alertLevel != null) {
                predicates.add(criteriaBuilder.equal(root.get("alertLevel"), alertLevel));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (StringUtils.hasText(areaId)) {
                predicates.add(criteriaBuilder.equal(root.get("areaId"), areaId));
            }

            if (startTime != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("timestamp"), startTime));
            }

            if (endTime != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("timestamp"), endTime));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return alertRepository.findAll(spec, pageable);
    }

    /**
     * 查询设备的最新告警
     * @param deviceId 设备ID
     * @param limit 限制条数
     * @return 告警列表
     */
    @Transactional(readOnly = true)
    public List<Alert> findRecentAlertsByDeviceId(String deviceId, int limit) {
        return alertRepository.findByDeviceIdOrderByTimestampDesc(deviceId)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * 查询未处理告警
     * @param areaId 区域ID（可选）
     * @return 未处理告警列表
     */
    @Transactional(readOnly = true)
    public List<Alert> findPendingAlerts(String areaId) {
        if (StringUtils.hasText(areaId)) {
            return alertRepository.findByStatusAndAreaId(Alert.AlertStatus.pending, areaId);
        } else {
            return alertRepository.findByStatus(Alert.AlertStatus.pending);
        }
    }

    /**
     * 查询正在处理的告警
     * @param repairmanId 维修人员ID（可选）
     * @return 正在处理的告警列表
     */
    @Transactional(readOnly = true)
    public List<Alert> findProcessingAlerts(String repairmanId) {
        if (StringUtils.hasText(repairmanId)) {
            return alertRepository.findByStatusAndResolvedBy(Alert.AlertStatus.processing, repairmanId);
        } else {
            return alertRepository.findByStatus(Alert.AlertStatus.processing);
        }
    }

    // ========== 告警统计相关方法 ==========

    /**
     * 获取告警统计概览
     * @param areaId 区域ID（可选）
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 告警统计概览
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getAlertOverview(String areaId, LocalDateTime startTime, LocalDateTime endTime) {
        Map<String, Object> overview = new HashMap<>();

        // 获取统计时间段内的告警
        List<Alert> alerts;
        if (startTime != null && endTime != null) {
            alerts = alertRepository.findByTimestampBetween(startTime, endTime);
        } else if (startTime != null) {
            alerts = alertRepository.findByTimestampAfter(startTime);
        } else {
            alerts = alertRepository.findAll();
        }

        // 按区域过滤
        if (StringUtils.hasText(areaId)) {
            alerts = alerts.stream()
                    .filter(alert -> areaId.equals(alert.getAreaId()))
                    .collect(Collectors.toList());
        }

        // 计算统计指标
        long totalAlerts = alerts.size();
        long pendingAlerts = alerts.stream()
                .filter(a -> a.getStatus() == Alert.AlertStatus.pending)
                .count();
        long processingAlerts = alerts.stream()
                .filter(a -> a.getStatus() == Alert.AlertStatus.processing)
                .count();
        long resolvedAlerts = alerts.stream()
                .filter(a -> a.getStatus() == Alert.AlertStatus.resolved)
                .count();

        // 计算平均响应时间（小时）
        double avgResponseHours = alerts.stream()
                .filter(a -> a.getResolvedTime() != null && a.getTimestamp() != null)
                .mapToDouble(a -> ChronoUnit.HOURS.between(a.getTimestamp(), a.getResolvedTime()))
                .average()
                .orElse(0.0);

        // 按级别统计
        Map<Alert.AlertLevel, Long> levelCount = alerts.stream()
                .collect(Collectors.groupingBy(Alert::getAlertLevel, Collectors.counting()));

        // 按类型统计
        Map<String, Long> typeCount = alerts.stream()
                .collect(Collectors.groupingBy(Alert::getAlertType, Collectors.counting()));

        overview.put("totalAlerts", totalAlerts);
        overview.put("pendingAlerts", pendingAlerts);
        overview.put("processingAlerts", processingAlerts);
        overview.put("resolvedAlerts", resolvedAlerts);
        overview.put("resolvedRate", totalAlerts > 0 ? (double) resolvedAlerts / totalAlerts * 100 : 0);
        overview.put("avgResponseHours", avgResponseHours);
        overview.put("alertLevelCount", levelCount);
        overview.put("alertTypeCount", typeCount);

        return overview;
    }

    /**
     * 获取告警趋势统计（按时间分组）
     * @param period 统计周期（day/week/month）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param areaId 区域ID（可选）
     * @return 时间序列告警数据
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAlertTrend(String period,
                                                   LocalDateTime startTime,
                                                   LocalDateTime endTime,
                                                   String areaId) {
        List<Alert> alerts = alertRepository.findByTimestampBetween(startTime, endTime);

        if (StringUtils.hasText(areaId)) {
            alerts = alerts.stream()
                    .filter(alert -> areaId.equals(alert.getAreaId()))
                    .collect(Collectors.toList());
        }

        // 按时间分组
        Map<String, List<Alert>> groupedAlerts = new TreeMap<>();

        for (Alert alert : alerts) {
            String timeKey = formatTimeKey(alert.getTimestamp(), period);
            groupedAlerts.computeIfAbsent(timeKey, k -> new ArrayList<>()).add(alert);
        }

        // 构建结果
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, List<Alert>> entry : groupedAlerts.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("timeLabel", entry.getKey());
            item.put("totalAlerts", entry.getValue().size());

            // 按级别统计
            Map<Alert.AlertLevel, Long> levelCount = entry.getValue().stream()
                    .collect(Collectors.groupingBy(Alert::getAlertLevel, Collectors.counting()));
            item.put("alertLevelCount", levelCount);

            result.add(item);
        }

        return result;
    }

    /**
     * 获取设备告警排名
     * @param topN 前N名
     * @param startTime 开始时间（可选）
     * @param endTime 结束时间（可选）
     * @return 设备告警排名
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDeviceAlertRanking(int topN,
                                                           LocalDateTime startTime,
                                                           LocalDateTime endTime) {
        List<Alert> alerts;
        if (startTime != null && endTime != null) {
            alerts = alertRepository.findByTimestampBetween(startTime, endTime);
        } else {
            alerts = alertRepository.findAll();
        }

        // 按设备分组统计
        Map<String, List<Alert>> deviceAlerts = alerts.stream()
                .collect(Collectors.groupingBy(Alert::getDeviceId));

        // 获取设备信息
        List<Map<String, Object>> ranking = new ArrayList<>();
        for (Map.Entry<String, List<Alert>> entry : deviceAlerts.entrySet()) {
            String deviceId = entry.getKey();
            List<Alert> deviceAlertList = entry.getValue();

            Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
            String deviceName = deviceOpt.map(Device::getDeviceName).orElse("未知设备");

            Map<String, Object> item = new HashMap<>();
            item.put("deviceId", deviceId);
            item.put("deviceName", deviceName);
            item.put("totalAlerts", deviceAlertList.size());
            item.put("pendingAlerts", deviceAlertList.stream()
                    .filter(a -> a.getStatus() == Alert.AlertStatus.pending)
                    .count());
            item.put("resolvedAlerts", deviceAlertList.stream()
                    .filter(a -> a.getStatus() == Alert.AlertStatus.resolved)
                    .count());

            // 计算最常见的告警类型
            String mostCommonType = deviceAlertList.stream()
                    .collect(Collectors.groupingBy(Alert::getAlertType, Collectors.counting()))
                    .entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("未知");
            item.put("mostCommonAlertType", mostCommonType);

            ranking.add(item);
        }

        // 按告警总数排序并限制数量
        return ranking.stream()
                .sorted((a, b) -> Integer.compare(
                        (Integer) b.get("totalAlerts"),
                        (Integer) a.get("totalAlerts")))
                .limit(topN)
                .collect(Collectors.toList());
    }

    // ========== 告警分析相关方法 ==========

    /**
     * 分析告警模式（发现频繁出现的告警组合）
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param areaId 区域ID（可选）
     * @return 告警模式分析结果
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> analyzeAlertPatterns(LocalDateTime startTime,
                                                          LocalDateTime endTime,
                                                          String areaId) {
        List<Alert> alerts = alertRepository.findByTimestampBetween(startTime, endTime);

        if (StringUtils.hasText(areaId)) {
            alerts = alerts.stream()
                    .filter(alert -> areaId.equals(alert.getAreaId()))
                    .collect(Collectors.toList());
        }

        // 按设备和时间窗口分组
        Map<String, Map<String, List<Alert>>> deviceTimeAlerts = new HashMap<>();

        for (Alert alert : alerts) {
            String deviceId = alert.getDeviceId();
            String timeWindow = formatTimeWindow(alert.getTimestamp());

            deviceTimeAlerts.computeIfAbsent(deviceId, k -> new HashMap<>())
                    .computeIfAbsent(timeWindow, k -> new ArrayList<>())
                    .add(alert);
        }

        // 分析同一时间窗口内的告警组合
        List<Map<String, Object>> patterns = new ArrayList<>();
        for (Map<String, List<Alert>> timeAlerts : deviceTimeAlerts.values()) {
            for (List<Alert> windowAlerts : timeAlerts.values()) {
                if (windowAlerts.size() >= 2) {
                    // 发现多个告警同时出现
                    Set<String> alertTypes = windowAlerts.stream()
                            .map(Alert::getAlertType)
                            .collect(Collectors.toSet());

                    if (alertTypes.size() > 1) {
                        Map<String, Object> pattern = new HashMap<>();
                        pattern.put("alertTypes", alertTypes);
                        pattern.put("count", windowAlerts.size());
                        pattern.put("deviceIds", deviceTimeAlerts.keySet());
                        pattern.put("timestamp", windowAlerts.get(0).getTimestamp());
                        patterns.add(pattern);
                    }
                }
            }
        }

        return patterns;
    }

    /**
     * 计算告警预测指标
     * @param deviceId 设备ID
     * @return 告警预测结果
     */
    @Transactional(readOnly = true)
    public Map<String, Object> predictAlertRisk(String deviceId) {
        Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
        if (deviceOpt.isEmpty()) {
            return Collections.emptyMap();
        }

        Device device = deviceOpt.get();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthAgo = now.minusMonths(1);

        // 获取最近一个月的告警
        List<Alert> recentAlerts = alertRepository.findByDeviceIdAndTimestampAfter(deviceId, oneMonthAgo);

        Map<String, Object> prediction = new HashMap<>();
        prediction.put("deviceId", deviceId);
        prediction.put("deviceName", device.getDeviceName());
        prediction.put("deviceStatus", device.getStatus());

        // 计算告警频率
        long alertCount = recentAlerts.size();
        double alertsPerDay = alertCount / 30.0;
        prediction.put("alertFrequency", alertsPerDay);

        // 风险等级评估
        String riskLevel;
        if (alertsPerDay >= 1.0) {
            riskLevel = "高风险";
        } else if (alertsPerDay >= 0.5) {
            riskLevel = "中风险";
        } else if (alertsPerDay >= 0.1) {
            riskLevel = "低风险";
        } else {
            riskLevel = "无风险";
        }
        prediction.put("riskLevel", riskLevel);

        // 最常见的告警类型
        String mostCommonType = recentAlerts.stream()
                .collect(Collectors.groupingBy(Alert::getAlertType, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("无");
        prediction.put("mostCommonAlertType", mostCommonType);

        // 平均解决时间
        double avgResolveHours = recentAlerts.stream()
                .filter(a -> a.getResolvedTime() != null)
                .mapToDouble(a -> ChronoUnit.HOURS.between(a.getTimestamp(), a.getResolvedTime()))
                .average()
                .orElse(0.0);
        prediction.put("avgResolveHours", avgResolveHours);

        return prediction;
    }

    // ========== 私有辅助方法 ==========

    /**
     * 检查是否存在重复的未处理告警
     */
    private boolean hasDuplicatePendingAlert(String deviceId, String alertType) {
        List<Alert> pendingAlerts = alertRepository.findByDeviceIdAndAlertTypeAndStatus(
                deviceId, alertType, Alert.AlertStatus.pending);

        if (!pendingAlerts.isEmpty()) {
            // 检查是否有30分钟内的重复告警
            LocalDateTime thirtyMinutesAgo = LocalDateTime.now().minusMinutes(30);
            return pendingAlerts.stream()
                    .anyMatch(alert -> alert.getTimestamp().isAfter(thirtyMinutesAgo));
        }

        return false;
    }

    /**
     * 判断是否需要创建工单
     */
    private boolean shouldCreateWorkOrder(String alertType, Alert.AlertLevel alertLevel) {
        // 严重级别以上的告警都需要创建工单
        if (alertLevel == Alert.AlertLevel.critical || alertLevel == Alert.AlertLevel.error) {
            return true;
        }

        // 特定类型的告警需要工单
        Set<String> workOrderRequiredTypes = Set.of(
                "WATER_MAKER_FAULT",
                "WATER_SUPPLY_FAULT",
                "DEVICE_FAULT",
                "SAFETY_ALERT"
        );

        return workOrderRequiredTypes.contains(alertType);
    }

    /**
     * 为告警创建工单
     */
    private void createWorkOrderForAlert(Alert alert) {
        try {
            WorkOrder workOrder = new WorkOrder();
            workOrder.setOrderId(generateOrderId());
            workOrder.setAlertId(alert.getAlertId());
            workOrder.setDeviceId(alert.getDeviceId());
            workOrder.setAreaId(alert.getAreaId());
            workOrder.setOrderType(WorkOrder.OrderType.repair);
            workOrder.setDescription("告警处理工单: " + alert.getAlertMessage());
            workOrder.setPriority(convertAlertLevelToPriority(alert.getAlertLevel()));
            workOrder.setStatus(WorkOrder.OrderStatus.pending);
            workOrder.setCreatedTime(LocalDateTime.now());

            workOrderRepository.save(workOrder);
            log.info("为告警创建工单成功: alertId={}, orderId={}",
                    alert.getAlertId(), workOrder.getOrderId());
        } catch (Exception e) {
            log.error("为告警创建工单失败: alertId={}, error={}",
                    alert.getAlertId(), e.getMessage(), e);
        }
    }

    /**
     * 生成工单ID
     */
    private String generateOrderId() {
        return String.format("WO%s%03d",
                System.currentTimeMillis(),
                (int)(Math.random() * 1000));
    }

    /**
     * 将告警级别转换为工单优先级
     */
    private WorkOrder.OrderPriority convertAlertLevelToPriority(Alert.AlertLevel alertLevel) {
        switch (alertLevel) {
            case critical:
                return WorkOrder.OrderPriority.urgent;
            case error:
                return WorkOrder.OrderPriority.high;
            case warning:
                return WorkOrder.OrderPriority.medium;
            default:
                return WorkOrder.OrderPriority.low;
        }
    }

    /**
     * 格式化时间键（用于分组）
     */
    private String formatTimeKey(LocalDateTime time, String period) {
        switch (period) {
            case "day":
                return time.toLocalDate().toString();
            case "week":
                return String.format("%d-W%d",
                        time.getYear(),
                        time.get(java.time.temporal.WeekFields.ISO.weekOfYear()));
            case "month":
                return String.format("%d-%02d",
                        time.getYear(), time.getMonthValue());
            default:
                return time.toLocalDate().toString();
        }
    }

    /**
     * 格式化时间窗口（用于模式分析）
     */
    private String formatTimeWindow(LocalDateTime time) {
        // 按小时窗口分组
        return String.format("%s-%02d",
                time.toLocalDate().toString(),
                time.getHour());
    }
}