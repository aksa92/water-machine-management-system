package com.campus.water.service;
import com.campus.water.config.MqttConfig;
import com.campus.water.entity.Alert;
import com.campus.water.entity.WaterMakerRealtimeData;
import com.campus.water.entity.WaterSupplyRealtimeData;
import com.campus.water.Repository.AlertRepository;
import com.campus.water.Repository.WaterMakerRealtimeDataRepository;
import com.campus.water.Repository.WaterSupplyRealtimeDataRepository;
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
    private final WaterMakerRealtimeDataRepository waterMakerRepo;
    private final WaterSupplyRealtimeDataRepository waterSupplyRepo;
    private final AlertRepository alertRepo;
    private final ObjectMapper objectMapper;
    private final MqttPahoMessageDrivenChannelAdapter mqttAdapter;
    private final AlertTriggerService alertTriggerService;
    private final MqttRedisCacheService redisCacheService;
    @PostConstruct
    public void initMqttSubscription() {
        mqttAdapter.addTopic(MqttConfig.TOPIC_WATER_MAKER_STATE + "+");
        mqttAdapter.addTopic(MqttConfig.TOPIC_WATER_MAKER_WARN + "+");
        mqttAdapter.addTopic(MqttConfig.TOPIC_WATER_SUPPLIER_STATE + "+");
        mqttAdapter.addTopic(MqttConfig.TOPIC_WATER_SUPPLIER_WARN + "+");
        log.info("MQTT订阅初始化完成 | 订阅主题：{}+、{}+、{}+、{}+",
                MqttConfig.TOPIC_WATER_MAKER_STATE,
                MqttConfig.TOPIC_WATER_MAKER_WARN,
                MqttConfig.TOPIC_WATER_SUPPLIER_STATE,
                MqttConfig.TOPIC_WATER_SUPPLIER_WARN);
    }

    @ServiceActivator(inputChannel = "mqttInputChannel")
    public void handleMqttMessage(String payload, @Header(MqttHeaders.RECEIVED_TOPIC) String topic) {
        log.info("MQTT消息接收成功 | 主题：{} | 内容：{}", topic, payload);

        try {
            if (topic.startsWith(MqttConfig.TOPIC_WATER_MAKER_STATE)) {
                handleWaterMakerState(payload);
            } else if (topic.startsWith(MqttConfig.TOPIC_WATER_MAKER_WARN)) {
                handleWaterMakerWarning(payload);
            } else if (topic.startsWith(MqttConfig.TOPIC_WATER_SUPPLIER_STATE)) {
                handleWaterSupplyState(payload);
            } else if (topic.startsWith(MqttConfig.TOPIC_WATER_SUPPLIER_WARN)) {
                handleWaterSupplyWarning(payload);
            } else {
                log.warn("MQTT消息主题未匹配 | 未知主题：{} | 内容：{}", topic, payload);
            }
        } catch (Exception e) {
            log.error("MQTT消息处理失败，缓存到Redis | 主题：{} | 异常：{}", topic, e.getMessage());

            redisCacheService.cacheMessage(topic, payload, MqttConfig.QOS);
        }
    }

    private void handleWaterMakerState(String payload) throws Exception {
        WaterMakerSensorData sensorData = objectMapper.readValue(payload, WaterMakerSensorData.class);

        WaterMakerRealtimeData entity = new WaterMakerRealtimeData();
        entity.setDeviceId(sensorData.getDeviceId());
        entity.setTdsValue1(sensorData.getTdsValue1() != null ? BigDecimal.valueOf(sensorData.getTdsValue1()) : null);
        entity.setTdsValue2(sensorData.getTdsValue2() != null ? BigDecimal.valueOf(sensorData.getTdsValue2()) : null);
        entity.setTdsValue3(sensorData.getTdsValue3() != null ? BigDecimal.valueOf(sensorData.getTdsValue3()) : null);
        entity.setWaterFlow1(sensorData.getWaterFlow1() != null ? BigDecimal.valueOf(sensorData.getWaterFlow1()) : null);
        entity.setWaterFlow2(sensorData.getWaterFlow2() != null ? BigDecimal.valueOf(sensorData.getWaterFlow2()) : null);
        entity.setWaterPress(sensorData.getWaterPress() != null ? BigDecimal.valueOf(sensorData.getWaterPress()) : null);
        entity.setFilterLife(sensorData.getFilterLife());
        entity.setLeakage(sensorData.getLeakage());
        entity.setWaterQuality(sensorData.getWaterQuality());
        entity.setStatus(WaterMakerRealtimeData.DeviceStatus.valueOf(sensorData.getStatus()));
        entity.setRecordTime(sensorData.getRecordTime());
        entity.setCreatedTime(LocalDateTime.now());

        waterMakerRepo.save(entity);
        log.info("制水机状态数据持久化成功 | 设备ID：{}", sensorData.getDeviceId());

        alertTriggerService.checkWaterMakerAbnormal(sensorData);
    }

    private void handleWaterMakerWarning(String payload) throws Exception {
        WaterMakerSensorData sensorData = objectMapper.readValue(payload, WaterMakerSensorData.class);
        // 新增：重复告警判断
        if (alertTriggerService.isDuplicateAlert(sensorData.getDeviceId(), "WATER_MAKER_ABNORMAL")) {
            log.info("制水机存在未处理告警，跳过重复触发 | 设备ID：{}", sensorData.getDeviceId());
            return;
        }
        Alert alert = new Alert();
        alert.setDeviceId(sensorData.getDeviceId());
        alert.setAlertType("WATER_MAKER_ABNORMAL");

        alert.setAlertLevel(Alert.AlertLevel.critical);
        alert.setAreaId(alertTriggerService.getDeviceAreaId(sensorData.getDeviceId()));
        alert.setAlertMessage(String.format(
                "制水机异常 - 设备ID：%s，TDS值：%.2f，滤芯寿命：%d%%，漏水状态：%s",
                sensorData.getDeviceId(),
                sensorData.getTdsValue1(),
                sensorData.getFilterLife(),
                sensorData.getLeakage() ? "是" : "否"
        ));
        alert.setStatus(Alert.AlertStatus.pending);
        alert.setTimestamp(sensorData.getRecordTime() != null ? sensorData.getRecordTime() : LocalDateTime.now());
        LocalDateTime now = LocalDateTime.now();
        alert.setCreatedTime(now);
        alert.setUpdatedTime(now);

        alertRepo.save(alert);
        log.warn("制水机告警记录持久化成功 | 告警ID：{} | 设备ID：{}", alert.getAlertId(), sensorData.getDeviceId());

        handleWaterMakerState(payload);
    }

    private void handleWaterSupplyState(String payload) throws Exception {
        WaterSupplySensorData sensorData = objectMapper.readValue(payload, WaterSupplySensorData.class);

        WaterSupplyRealtimeData entity = new WaterSupplyRealtimeData();
        entity.setDeviceId(sensorData.getDeviceId());
        entity.setWaterFlow(sensorData.getWaterFlow() != null ? BigDecimal.valueOf(sensorData.getWaterFlow()) : null);
        entity.setWaterPress(sensorData.getWaterPress() != null ? BigDecimal.valueOf(sensorData.getWaterPress()) : null);
        entity.setWaterLevel(sensorData.getWaterLevel() != null ? BigDecimal.valueOf(sensorData.getWaterLevel()) : null);
        entity.setTemperature(sensorData.getTemperature() != null ? BigDecimal.valueOf(sensorData.getTemperature()) : null);
        entity.setStatus(WaterSupplyRealtimeData.DeviceStatus.valueOf(sensorData.getStatus()));
        entity.setTimestamp(sensorData.getTimestamp());

        waterSupplyRepo.save(entity);
        log.info("供水机状态数据持久化成功 | 设备ID：{}", sensorData.getDeviceId());

        alertTriggerService.checkWaterSupplyAbnormal(sensorData);
    }

    /**
     * 新增：处理供水机告警数据
     */
    private void handleWaterSupplyWarning(String payload) throws Exception {
        WaterSupplySensorData sensorData = objectMapper.readValue(payload, WaterSupplySensorData.class);
        // 新增：重复告警判断
        if (alertTriggerService.isDuplicateAlert(sensorData.getDeviceId(), "WATER_SUPPLY_ABNORMAL")) {
            log.info("供水机存在未处理告警，跳过重复触发 | 设备ID：{}", sensorData.getDeviceId());
            return;
        }
        Alert alert = new Alert();
        alert.setDeviceId(sensorData.getDeviceId());
        alert.setAlertType("WATER_SUPPLY_ABNORMAL");

        alert.setAlertLevel(Alert.AlertLevel.error);
        alert.setAreaId(alertTriggerService.getDeviceAreaId(sensorData.getDeviceId()));
        alert.setAlertMessage(String.format(
                "供水机异常 - 设备ID：%s，水压：%.2fMPa，水位：%.2f%%，水温：%.2f℃",
                sensorData.getDeviceId(),
                sensorData.getWaterPress(),
                sensorData.getWaterLevel(),
                sensorData.getTemperature()
        ));
        alert.setStatus(Alert.AlertStatus.pending);
        alert.setTimestamp(sensorData.getTimestamp() != null ? sensorData.getTimestamp() : LocalDateTime.now());
        LocalDateTime now = LocalDateTime.now();
        alert.setCreatedTime(now);
        alert.setUpdatedTime(now);

        alertRepo.save(alert);
        log.warn("供水机告警记录持久化成功 | 告警ID：{} | 设备ID：{}", alert.getAlertId(), sensorData.getDeviceId());

        // 同时持久化状态数据
        handleWaterSupplyState(payload);
    }
}