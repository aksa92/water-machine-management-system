package com.campus.water.service;

import com.campus.water.config.MqttConfig;
import com.campus.water.entity.Alert;
import com.campus.water.entity.WaterMakerRealtimeData;
import com.campus.water.entity.WaterSupplyRealtimeData;
import com.campus.water.mapper.AlertRepository;
import com.campus.water.mapper.WaterMakerRealtimeDataRepository;
import com.campus.water.mapper.WaterSupplyRealtimeDataRepository;
import com.campus.water.model.WaterMakerSensorData;
import com.campus.water.model.WaterSupplySensorData;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MqttSensorReceiver {
    // JPA Repository（数据持久化接口，Spring自动注入实现）
    private final WaterMakerRealtimeDataRepository waterMakerRepo;
    private final WaterSupplyRealtimeDataRepository waterSupplyRepo;
    private final AlertRepository alertRepo;
    private final ObjectMapper objectMapper;
    private final MqttPahoMessageDrivenChannelAdapter mqttAdapter;
    // 新增告警触发服务依赖
    private final AlertTriggerService alertTriggerService;

    /**
     * 项目启动后初始化：订阅所有需要的MQTT主题
     * 主题后缀用「+」表示通配符（匹配所有设备ID）
     */
    @PostConstruct
    public void initMqttSubscription() {
        mqttAdapter.addTopic(MqttConfig.TOPIC_WATER_MAKER_STATE + "+"); // 制水机状态（所有设备）
        mqttAdapter.addTopic(MqttConfig.TOPIC_WATER_MAKER_WARN + "+"); // 制水机告警（所有设备）
        mqttAdapter.addTopic(MqttConfig.TOPIC_WATER_SUPPLIER_STATE + "+"); // 供水机状态（所有设备）
        mqttAdapter.addTopic(MqttConfig.TOPIC_WATER_SUPPLIER_WARN + "+"); // 供水机告警（所有设备）
        log.info("MQTT订阅初始化完成 | 订阅主题：{}+、{}+、{}+、{}+",
                MqttConfig.TOPIC_WATER_MAKER_STATE,
                MqttConfig.TOPIC_WATER_MAKER_WARN,
                MqttConfig.TOPIC_WATER_SUPPLIER_STATE,
                MqttConfig.TOPIC_WATER_SUPPLIER_WARN);
    }

    /**
     * 监听MQTT接收通道，处理所有收到的消息
     * @param payload 消息内容（JSON字符串）
     * @param topic 接收主题（用于区分消息类型）
     */
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMqttMessage(String payload, @Header(MqttHeaders.RECEIVED_TOPIC) String topic) {
        log.info("MQTT消息接收成功 | 主题：{} | 内容：{}", topic, payload);

        try {
            // 根据主题分类处理
            if (topic.startsWith(MqttConfig.TOPIC_WATER_MAKER_STATE)) {
                handleWaterMakerState(payload); // 制水机状态数据
            } else if (topic.startsWith(MqttConfig.TOPIC_WATER_MAKER_WARN)) {
                handleWaterMakerWarning(payload); // 制水机告警数据
            } else if (topic.startsWith(MqttConfig.TOPIC_WATER_SUPPLIER_STATE)) {
                handleWaterSupplyState(payload); // 供水机状态数据
            } else if (topic.startsWith(MqttConfig.TOPIC_WATER_SUPPLIER_WARN)) {
                handleWaterSupplyWarning(payload); // 供水机告警数据
            } else {
                log.warn("MQTT消息主题未匹配 | 未知主题：{} | 内容：{}", topic, payload);
            }
        } catch (Exception e) {
            log.error("MQTT消息处理失败 | 主题：{} | 内容：{} | 异常：{}", topic, payload, e.getMessage());
        }
    }

    /**
     * 处理制水机状态数据：转换为JPA实体并持久化
     */
    private void handleWaterMakerState(String payload) throws Exception {
        // 1. JSON反序列化为模型对象
        WaterMakerSensorData sensorData = objectMapper.readValue(payload, WaterMakerSensorData.class);

        // 2. 模型对象转换为JPA实体（持久化到数据库）
        WaterMakerRealtimeData entity = new WaterMakerRealtimeData();
        entity.setDeviceId(sensorData.getDeviceId());
        // Double转BigDecimal处理（包含null值判断）
        entity.setTdsValue1(sensorData.getTdsValue1() != null ? BigDecimal.valueOf(sensorData.getTdsValue1()) : null);
        entity.setTdsValue2(sensorData.getTdsValue2() != null ? BigDecimal.valueOf(sensorData.getTdsValue2()) : null);
        entity.setTdsValue3(sensorData.getTdsValue3() != null ? BigDecimal.valueOf(sensorData.getTdsValue3()) : null);
        entity.setWaterFlow1(sensorData.getWaterFlow1() != null ? BigDecimal.valueOf(sensorData.getWaterFlow1()) : null);
        entity.setWaterFlow2(sensorData.getWaterFlow2() != null ? BigDecimal.valueOf(sensorData.getWaterFlow2()) : null);
        entity.setWaterPress(sensorData.getWaterPress() != null ? BigDecimal.valueOf(sensorData.getWaterPress()) : null);
        entity.setFilterLife(sensorData.getFilterLife());
        entity.setLeakage(sensorData.getLeakage() ? true : false); // 数据库存储：true=漏水，false=正常
        entity.setWaterQuality(sensorData.getWaterQuality());
        entity.setStatus(WaterMakerRealtimeData.DeviceStatus.valueOf(sensorData.getStatus()));
        entity.setRecordTime(sensorData.getRecordTime());
        entity.setCreatedTime(LocalDateTime.now());

        // 3. 持久化到数据库（JPA save() 自动实现CRUD）
        waterMakerRepo.save(entity);
        log.info("制水机状态数据持久化成功 | 设备ID：{}", sensorData.getDeviceId());

        // 新增：调用告警检查逻辑
        alertTriggerService.checkWaterMakerAbnormal(sensorData);
    }

    /**
     * 处理制水机告警数据：持久化告警记录+状态数据
     */
    private void handleWaterMakerWarning(String payload) throws Exception {
        WaterMakerSensorData sensorData = objectMapper.readValue(payload, WaterMakerSensorData.class);

        // 1. 持久化告警记录
        Alert alert = new Alert();
        alert.setDeviceId(sensorData.getDeviceId());
        alert.setAlertType("WATER_MAKER_ABNORMAL"); // 告警类型（枚举规范）
        alert.setAlertLevel(Alert.AlertLevel.critical); // 告警级别（严重）
        alert.setAlertMessage(String.format(
                "制水机异常 - 设备ID：%s，TDS值：%.2f，滤芯寿命：%d%%，漏水状态：%s",
                sensorData.getDeviceId(),
                sensorData.getTdsValue1(),
                sensorData.getFilterLife(),
                sensorData.getLeakage() ? "是" : "否"
        ));
        alert.setStatus(Alert.AlertStatus.pending); // 告警状态（未处理）
        alert.setTimestamp(sensorData.getRecordTime());
        alert.setCreatedTime(LocalDateTime.now());

        alertRepo.save(alert);
        log.warn("制水机告警记录持久化成功 | 告警ID：{} | 设备ID：{}", alert.getAlertId(), sensorData.getDeviceId());

        // 2. 同时持久化状态数据（便于后续追溯）
        handleWaterMakerState(payload);
    }

    /**
     * 处理供水机状态数据：转换为JPA实体并持久化
     */
    private void handleWaterSupplyState(String payload) throws Exception {
        WaterSupplySensorData sensorData = objectMapper.readValue(payload, WaterSupplySensorData.class);

        WaterSupplyRealtimeData entity = new WaterSupplyRealtimeData();
        entity.setDeviceId(sensorData.getDeviceId());
        // Double转BigDecimal处理（包含null值判断）
        entity.setWaterFlow(sensorData.getWaterFlow() != null ? BigDecimal.valueOf(sensorData.getWaterFlow()) : null);
        entity.setWaterPress(sensorData.getWaterPress() != null ? BigDecimal.valueOf(sensorData.getWaterPress()) : null);
        entity.setWaterLevel(sensorData.getWaterLevel() != null ? BigDecimal.valueOf(sensorData.getWaterLevel()) : null);
        entity.setTemperature(sensorData.getTemperature() != null ? BigDecimal.valueOf(sensorData.getTemperature()) : null);
        entity.setStatus(WaterSupplyRealtimeData.DeviceStatus.valueOf(sensorData.getStatus()));
        entity.setTimestamp(sensorData.getTimestamp());


        waterSupplyRepo.save(entity);
        log.info("供水机状态数据持久化成功 | 设备ID：{}", sensorData.getDeviceId());

        // 新增：调用告警检查逻辑
        alertTriggerService.checkWaterSupplyAbnormal(sensorData);
    }

    /**
     * 处理供水机告警数据：持久化告警记录+状态数据
     */
    private void handleWaterSupplyWarning(String payload) throws Exception {
        WaterSupplySensorData sensorData = objectMapper.readValue(payload, WaterSupplySensorData.class);

        // 1. 持久化告警记录
        Alert alert = new Alert();
        alert.setDeviceId(sensorData.getDeviceId());
        alert.setAlertType("WATER_SUPPLY_ABNORMAL");
        alert.setAlertLevel(Alert.AlertLevel.error);
        alert.setAlertMessage(String.format(
                "供水机异常 - 设备ID：%s，水压：%.2fMPa，水位：%.2f%%",
                sensorData.getDeviceId(),
                sensorData.getWaterPress(),
                sensorData.getWaterLevel()
        ));
        alert.setStatus(Alert.AlertStatus.pending);
        alert.setTimestamp(sensorData.getTimestamp());
        alert.setCreatedTime(LocalDateTime.now());

        alertRepo.save(alert);
        log.warn("供水机告警记录持久化成功 | 告警ID：{} | 设备ID：{}", alert.getAlertId(), sensorData.getDeviceId());

        // 2. 同时持久化状态数据
        handleWaterSupplyState(payload);
    }
}