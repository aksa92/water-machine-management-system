package com.campus.water.service;

import com.campus.water.entity.Alert;
import com.campus.water.entity.Device;
import com.campus.water.entity.WorkOrder;
import com.campus.water.mapper.AlertRepository;
import com.campus.water.mapper.DeviceRepository;
import com.campus.water.mapper.WorkOrderRepository;
import com.campus.water.model.WaterMakerSensorData;
import com.campus.water.model.WaterSupplySensorData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.campus.water.service.AlertPushService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 告警触发与工单创建服务
 * 监听传感器数据，满足异常条件时自动创建告警和工单
 * 工单类型映射规则：
 * - repair：故障维修（设备已故障，如漏水、TDS异常、水压/水位异常等）
 * - maintenance：保养（设备未故障但需预防性维护，如滤芯寿命不足）
 * - inspection：巡检（主动发起，不通过告警触发）
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AlertTriggerService {

    // 异常阈值配置（可根据实际需求调整或移至配置文件）

    private static final double WATER_MAKER_TDS_THRESHOLD = 100.0; // 制水机TDS值异常阈值
    private static final double WATER_MAKER_PRESS_MIN = 0.2; // 最小水压
    private static final int FILTER_LIFE_THRESHOLD = 20; // 滤芯寿命阈值(%)
    private static final double WATER_SUPPLY_LEVEL_MIN = 20.0; // 供水机最低水位(%)
    private static final double WATER_SUPPLY_PRESS_MIN = 0.1; // 供水机最小水压
    private static final double WATER_SUPPLY_TEMP_MAX = 25.0; // 供水机最高水温(℃)
    private static final int ALERT_DUPLICATE_INTERVAL = 30; // 重复告警间隔(分钟)

    private final AlertRepository alertRepository;
    private final WorkOrderRepository workOrderRepository;
    private final DeviceRepository deviceRepository;

    /**
     * 检查制水机数据异常并触发告警
     * 区分故障类（repair）和保养类（maintenance）工单
     */
    @Transactional
    public void checkWaterMakerAbnormal(WaterMakerSensorData data) {
        // 1. 拆分故障类和保养类异常
        boolean isFaultAbnormal = false; // 故障类（需repair）
        boolean isMaintainAbnormal = false; // 保养类（需maintenance）
        StringBuilder faultAlertMsg = new StringBuilder("制水机故障：");
        StringBuilder maintainAlertMsg = new StringBuilder("制水机保养提醒：");

        // 故障类异常判断（触发repair工单）
        if (data.getTdsValue1() > WATER_MAKER_TDS_THRESHOLD) {
            faultAlertMsg.append(String.format("原水TDS值过高(%.2f)；", data.getTdsValue1()));
            isFaultAbnormal = true;
        }
        if (data.getTdsValue2() > WATER_MAKER_TDS_THRESHOLD) {
            faultAlertMsg.append(String.format("纯水TDS值过高(%.2f)；", data.getTdsValue2()));
            isFaultAbnormal = true;
        }
        if (data.getWaterPress() < WATER_MAKER_PRESS_MIN) {
            faultAlertMsg.append(String.format("水压过低(%.2fMPa)；", data.getWaterPress()));
            isFaultAbnormal = true;
        }
        if (data.getLeakage()) {
            faultAlertMsg.append("设备漏水；");
            isFaultAbnormal = true;
        }

        // 保养类异常判断（触发maintenance工单）
        if (data.getFilterLife() < FILTER_LIFE_THRESHOLD) {
            maintainAlertMsg.append(String.format("滤芯寿命不足(%d%%)，需更换；", data.getFilterLife()));
            isMaintainAbnormal = true;
        }

        // 2. 处理故障类告警（repair工单）
        if (isFaultAbnormal) {
            if (isDuplicateAlert(data.getDeviceId(), "WATER_MAKER_FAULT")) {
                log.info("制水机存在未处理故障告警，跳过重复触发 | 设备ID：{}", data.getDeviceId());
            } else {
                log.warn("制水机故障条件满足 | 设备ID：{} | 原因：{}", data.getDeviceId(), faultAlertMsg);
                createAlertAndWorkOrder(
                        data.getDeviceId(),
                        "WATER_MAKER_FAULT",
                        Alert.AlertLevel.critical,
                        faultAlertMsg.toString(),
                        WorkOrder.OrderType.repair // 故障→维修工单
                );
            }
        }

        // 3. 处理保养类告警（maintenance工单）
        if (isMaintainAbnormal) {
            if (isDuplicateAlert(data.getDeviceId(), "WATER_MAKER_MAINTENANCE")) {
                log.info("制水机存在未处理保养告警，跳过重复触发 | 设备ID：{}", data.getDeviceId());
            } else {
                log.warn("制水机保养条件满足 | 设备ID：{} | 原因：{}", data.getDeviceId(), maintainAlertMsg);
                createAlertAndWorkOrder(
                        data.getDeviceId(),
                        "WATER_MAKER_MAINTENANCE",
                        Alert.AlertLevel.warning,
                        maintainAlertMsg.toString(),
                        WorkOrder.OrderType.maintenance // 保养→保养工单
                );
            }
        }
    }

    /**
     * 检查供水机数据异常并触发告警
     * 均为故障类异常，触发repair工单
     */
    @Transactional
    public void checkWaterSupplyAbnormal(WaterSupplySensorData data) {
        // 1. 检查重复告警
        if (isDuplicateAlert(data.getDeviceId(), "WATER_SUPPLY_FAULT")) {
            log.info("供水机存在未处理故障告警，跳过重复触发 | 设备ID：{}", data.getDeviceId());
            return;
        }

        // 2. 故障类异常判断（仅repair工单）
        boolean isAbnormal = false;
        StringBuilder alertMsg = new StringBuilder("供水机故障：");

        if (data.getWaterLevel() < WATER_SUPPLY_LEVEL_MIN) {
            alertMsg.append(String.format("水位过低(%.2f%%)；", data.getWaterLevel()));
            isAbnormal = true;
        }
        if (data.getWaterPress() < WATER_SUPPLY_PRESS_MIN) {
            alertMsg.append(String.format("水压过低(%.2fMPa)；", data.getWaterPress()));
            isAbnormal = true;
        }
        if (data.getTemperature() > WATER_SUPPLY_TEMP_MAX) {
            alertMsg.append(String.format("水温过高(%.2f℃)；", data.getTemperature()));
            isAbnormal = true;
        }

        // 3. 触发告警并创建维修工单
        if (isAbnormal) {
            log.warn("供水机故障条件满足 | 设备ID：{} | 原因：{}", data.getDeviceId(), alertMsg);
            createAlertAndWorkOrder(
                    data.getDeviceId(),
                    "WATER_SUPPLY_FAULT",
                    Alert.AlertLevel.error,
                    alertMsg.toString(),
                    WorkOrder.OrderType.repair // 供水机异常均为故障→维修工单
            );
        }
    }

    private final AlertPushService alertPushService;
    /**
     * 创建告警记录和对应的工单（支持repair/maintenance类型）
     * @param orderType 工单类型（repair:维修，maintenance:保养，inspection:巡检<告警不触发>）
     */
    private void createAlertAndWorkOrder(String deviceId, String alertType,
                                         Alert.AlertLevel level, String message,
                                         WorkOrder.OrderType orderType) {
        // 获取设备所在区域（用于工单分配）
        String areaId = getDeviceAreaId(deviceId);

        // 1. 创建告警记录
        Alert alert = new Alert();
        alert.setDeviceId(deviceId);
        alert.setAlertType(alertType);
        alert.setAlertLevel(level);
        alert.setAlertMessage(message);
        alert.setAreaId(areaId);
        alert.setStatus(Alert.AlertStatus.pending);
        alert.setTimestamp(LocalDateTime.now());
        alertRepository.save(alert);
        alertPushService.pushAlertMessage(alert);
        log.info("创建告警记录成功 | 告警ID：{} | 设备ID：{} | 告警类型：{}",
                alert.getAlertId(), deviceId, alertType);

        // 2. 创建对应类型的工单（inspection类型不通过告警触发）
        WorkOrder workOrder = new WorkOrder();
        workOrder.setOrderId(generateOrderId());
        workOrder.setDeviceId(deviceId);
        workOrder.setAreaId(areaId);
        workOrder.setOrderType(orderType); // 动态设置工单类型
        workOrder.setDescription(message);
        workOrder.setStatus(WorkOrder.OrderStatus.pending);
        workOrder.setDeadline(LocalDateTime.now().plusHours(24));// 创建工单时设置截止时间（例如24小时后）
        workOrder.setCreatedTime(LocalDateTime.now());
        workOrderRepository.save(workOrder);
        log.info("创建工单成功 | 工单ID：{} | 设备ID：{} | 工单类型：{}",
                workOrder.getOrderId(), deviceId, orderType.name());
    }

    /**
     * 检查是否存在重复告警（同设备同类型未处理告警，且在间隔时间内）
     * 覆盖pending/processing两种未处理状态
     */
    public boolean isDuplicateAlert(String deviceId, String alertType) {
        LocalDateTime before = LocalDateTime.now().minusMinutes(ALERT_DUPLICATE_INTERVAL);
        // 检查未处理的告警状态（pending/processing）
        List<Alert.AlertStatus> activeStatus = Arrays.asList(
                Alert.AlertStatus.pending,
                Alert.AlertStatus.processing
        );
        return !alertRepository.findByDeviceIdAndAlertTypeAndStatusInAndTimestampAfter(
                deviceId,
                alertType,
                activeStatus,
                before
        ).isEmpty();
    }

    /**
     * 获取设备所在区域ID（新增超时控制，避免查询阻塞）
     */
    public String getDeviceAreaId(String deviceId) {
        try {
            Optional<Device> deviceOpt = deviceRepository.findById(deviceId);
            String areaId = deviceOpt.map(Device::getAreaId).orElse(null);
            // 空值兜底：null/空字符串→unknown
            if (areaId == null || areaId.trim().isEmpty()) {
                areaId = "unknown";
                log.warn("设备{}的area_id为空/设备不存在，兜底为unknown", deviceId);
            }
            return areaId;
        } catch (Exception e) { // 捕获所有数据库异常
            log.error("获取设备{}的area_id失败（数据库异常）", deviceId, e);
            return "unknown"; // 异常时兜底
        }
    }
    /**
     * 生成唯一工单ID（WO+时间戳+随机数）
     */
    private String generateOrderId() {
        return String.format("WO%s%03d",
                System.currentTimeMillis(),
                (int)(Math.random() * 1000));
    }
}