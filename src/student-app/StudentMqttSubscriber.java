package com.campus.water.student.mqtt;

import com.alibaba.fastjson2.JSONObject;
import com.campus.water.student.config.StudentMqttConfig;
import com.campus.water.student.entity.WaterQualityVO;
import com.campus.water.student.service.WaterQualityService;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 学生APP端MQTT订阅器（仅接收终端机水质数据，适配“扫码查水质”需求）
 */
@Component
public class StudentMqttSubscriber {
    @Autowired
    private StudentMqttConfig mqttConfig;
    @Autowired
    private WaterQualityService waterQualityService;

    // 学生扫码后订阅对应终端机的水质数据
    public void subscribeTerminalWaterQuality(String studentId, String terminalId) throws Exception {
        // 1. 创建MQTT客户端（按学生ID区分）
        MqttClient client = mqttConfig.mqttClient(studentId);

        // 2. 订阅该终端机的水质主题
        String topic = mqttConfig.getSubscribeTopic(terminalId);
        client.subscribe(topic, 0, messageListener(terminalId));
        System.out.println("学生APP（ID：" + studentId + "）已订阅终端机" + terminalId + "水质数据");
    }

    // 消息监听器（缓存水质数据，供APP前端展示）
    private IMqttMessageListener messageListener(String terminalId) {
        return (topic, message) -> {
            String payload = new String(message.getPayload());
            System.out.println("学生APP接收终端机" + terminalId + "水质数据：" + payload);

            // 解析水质数据并缓存
            WaterQualityVO qualityVO = JSONObject.parseObject(payload, WaterQualityVO.class);
            waterQualityService.updateWaterQuality(terminalId, qualityVO);
        };
    }
}