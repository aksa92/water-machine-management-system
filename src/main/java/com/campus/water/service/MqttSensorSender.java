package com.campus.water.service;

import com.campus.water.config.MqttConfig;
import com.campus.water.model.WaterMakerSensorData;
import com.campus.water.model.WaterSupplySensorData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j // 日志注解（替代System.out）
public class MqttSensorSender {
    private final MqttPahoMessageHandler mqttMessageHandler;
    private final ObjectMapper objectMapper; // JSON序列化工具（Spring默认注入）
    private final Random random = new Random(); // 生成模拟数据

    /**
     * 模拟制水机发送「正常状态数据」
     * @param deviceId 设备ID（如WM001）
     */
    public void sendWaterMakerState(String deviceId) {
        try {
            // 1. 构建模拟数据（符合正常业务范围）
            WaterMakerSensorData data = new WaterMakerSensorData();
            data.setDeviceId(deviceId);
            data.setTdsValue(50 + random.nextDouble() * 30); // 50-80（正常范围）
            data.setWaterFlow(random.nextDouble() * 2); // 0-2 L/min
            data.setWaterPressure(0.2 + random.nextDouble() * 0.3); // 0.2-0.5 MPa
            data.setFilterLife(30 + random.nextInt(70)); // 30-100%
            data.setLeakage(random.nextBoolean() && random.nextBoolean()); // 漏水概率较低（25%）
            data.setTemperature(20 + random.nextDouble() * 5); // 20-25℃
            data.setHumidity(40 + random.nextDouble() * 20); // 40-60%RH
            data.setWaterQuality("合格");
            data.setStatus("normal");
            data.setTimestamp(LocalDateTime.now());

            // 2. 序列化JSON（MQTT消息 payload 为JSON字符串）
            String payload = objectMapper.writeValueAsString(data);
            // 3. 构建主题（设备ID作为主题后缀，精准订阅）
            String topic = MqttConfig.TOPIC_WATER_MAKER_STATE + deviceId;

            // 4. 发送MQTT消息
            sendMessage(topic, payload);
            log.info("制水机状态消息发送成功 | 设备ID：{} | 主题：{} | 数据：{}", deviceId, topic, payload);
        } catch (JsonProcessingException e) {
            log.error("制水机状态消息发送失败 | 设备ID：{} | 异常：{}", deviceId, e.getMessage());
        }
    }

    /**
     * 模拟制水机发送「告警数据」
     * @param deviceId 设备ID（如WM001）
     */
    public void sendWaterMakerWarning(String deviceId) {
        try {
            // 1. 构建异常数据（超出正常范围）
            WaterMakerSensorData data = new WaterMakerSensorData();
            data.setDeviceId(deviceId);
            data.setTdsValue(150 + random.nextDouble() * 50); // 150-200（异常范围）
            data.setWaterFlow(0.1 + random.nextDouble() * 0.3); // 流量极低
            data.setWaterPressure(0.1 + random.nextDouble() * 0.1); // 水压过低（0.1-0.2 MPa）
            data.setFilterLife(5 + random.nextInt(10)); // 滤芯寿命低（5-15%）
            data.setLeakage(true); // 强制漏水
            data.setTemperature(28 + random.nextDouble() * 3); // 水温过高（28-31℃）
            data.setStatus("error");
            data.setTimestamp(LocalDateTime.now());

            // 2. 序列化+发送
            String payload = objectMapper.writeValueAsString(data);
            String topic = MqttConfig.TOPIC_WATER_MAKER_WARN + deviceId;
            sendMessage(topic, payload);
            log.warn("制水机告警消息发送成功 | 设备ID：{} | 主题：{} | 数据：{}", deviceId, topic, payload);
        } catch (JsonProcessingException e) {
            log.error("制水机告警消息发送失败 | 设备ID：{} | 异常：{}", deviceId, e.getMessage());
        }
    }

    /**
     * 模拟供水机发送「正常状态数据」
     * @param deviceId 设备ID（如WS001）
     */
    public void sendWaterSupplyData(String deviceId) {
        try {
            // 1. 构建模拟数据
            WaterSupplySensorData data = new WaterSupplySensorData();
            data.setDeviceId(deviceId);
            data.setWaterFlow(random.nextDouble() * 3); // 0-3 L/min
            data.setWaterPressure(0.1 + random.nextDouble() * 0.2); // 0.1-0.3 MPa
            data.setWaterLevel(30 + random.nextDouble() * 50); // 30-80%
            data.setTemperature(18 + random.nextDouble() * 4); // 18-22℃
            data.setHumidity(35 + random.nextDouble() * 15); // 35-50%RH
            data.setStatus("normal");
            data.setTimestamp(LocalDateTime.now());

            // 2. 序列化+发送
            String payload = objectMapper.writeValueAsString(data);
            String topic = MqttConfig.TOPIC_WATER_SUPPLIER_STATE + deviceId;
            sendMessage(topic, payload);
            log.info("供水机状态消息发送成功 | 设备ID：{} | 主题：{} | 数据：{}", deviceId, topic, payload);
        } catch (JsonProcessingException e) {
            log.error("供水机状态消息发送失败 | 设备ID：{} | 异常：{}", deviceId, e.getMessage());
        }
    }

    /**
     * 通用发送方法（封装MQTT消息构建逻辑）
     * @param topic 主题
     * @param payload 消息内容（JSON字符串）
     */
    private void sendMessage(String topic, String payload) {
        // 构建Spring Messaging消息（指定JSON格式、主题、QOS）
        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                .setHeader("mqtt_topic", topic)
                .setHeader("mqtt_qos", MqttConfig.QOS)
                .build();

        // 调用处理器发送消息
        mqttMessageHandler.handleMessage(message);
    }
}