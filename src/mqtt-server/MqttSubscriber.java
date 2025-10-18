package com.campus.water.mqtt.core;

import com.campus.water.mqtt.service.AlertTriggerService;
import com.campus.water.mqtt.service.SensorDataStorage;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * MQTT数据订阅器（接收制水机/供水机数据，对应需求中“云端接收设备数据”）
 */
@Component
public class MqttSubscriber {
    @Autowired
    private MqttClient mqttClient;
    @Autowired
    private String[] subscribeTopics;
    @Autowired
    private int[] qosLevels;
    @Autowired
    private DataParser dataParser;
    @Autowired
    private DataForwarder dataForwarder;
    @Autowired
    private SensorDataStorage sensorDataStorage;
    @Autowired
    private AlertTriggerService alertTriggerService;

    // 初始化订阅（项目启动后自动执行）
    @PostConstruct
    public void startSubscribe() throws Exception {
        // 为每个主题绑定消息监听器
        for (int i = 0; i < subscribeTopics.length; i++) {
            String topic = subscribeTopics[i];
            int qos = qosLevels[i];
            mqttClient.subscribe(topic, qos, messageListener());
            System.out.println("MQTT服务器已订阅主题：" + topic + "（QoS=" + qos + "）");
        }
    }

    // 消息监听器（接收数据后解析→存储→转发）
    private IMqttMessageListener messageListener() {
        return (topic, message) -> {
            String payload = new String(message.getPayload());
            System.out.println("MQTT服务器接收数据：主题=" + topic + "，内容=" + payload);

            // 按主题类型处理（严格对齐需求中设备数据类型）
            if (topic.startsWith("sensor/watermaker/")) {
                // 1. 处理制水机传感器数据（TDS/水压/流量）
                var sensorData = dataParser.parseWaterMakerData(topic, payload);
                sensorDataStorage.saveToInfluxDB(sensorData); // 存入时序库
                dataForwarder.forwardToWeb(sensorData);       // 转发到管理Web
                dataForwarder.forwardToStudentApp(sensorData); // 转发到学生APP（水质数据）
            } else if (topic.startsWith("sensor/watersupply/")) {
                // 2. 处理供水机传感器数据（水位）
                var sensorData = dataParser.parseWaterSupplyData(topic, payload);
                sensorDataStorage.saveToInfluxDB(sensorData); // 存入时序库
                dataForwarder.forwardToWeb(sensorData);       // 转发到管理Web
                dataForwarder.forwardToRepairApp(sensorData); // 转发到维修APP（辖区水位）
            } else if (topic.startsWith("alert/")) {
                // 3. 处理告警报文（触发工单）
                var alert = dataParser.parseAlertMessage(topic, payload);
                alertTriggerService.createWorkOrder(alert);   // 生成工单（存入MySQL）
                dataForwarder.forwardToWeb(alert);            // 转发到管理Web（告警列表）
                dataForwarder.forwardToRepairApp(alert);      // 转发到维修APP（抢单）
            }
        };
    }
}