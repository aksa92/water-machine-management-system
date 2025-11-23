package com.campus.water.mqtt.core;

import com.alibaba.fastjson2.JSONObject;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class MqttSubscriber {
    @Autowired
    private MqttClient mqttClient;
    @Autowired
    private String[] subscribeTopics;
    @Autowired
    private int[] qosLevels;
    @Autowired
    private DataForwarder dataForwarder;

    @PostConstruct
    public void startSubscribe() throws Exception {
        for (int i = 0; i < subscribeTopics.length; i++) {
            String topic = subscribeTopics[i];
            int qos = qosLevels[i];
            mqttClient.subscribe(topic, qos, messageListener());
            System.out.println("MQTT服务器已订阅主题：" + topic + "（QoS=" + qos + "）");
        }
    }

    private IMqttMessageListener messageListener() {
        return (topic, message) -> {
            String payload = new String(message.getPayload());
            System.out.println("MQTT服务器接收数据：主题=" + topic + "，内容=" + payload);

            try {
                if (topic.startsWith("sensor/watermaker/")) {
                    // 解析制水机数据
                    Map<String, Object> sensorData = parseWaterMakerData(topic, payload);
                    dataForwarder.forwardToWeb(sensorData);
                    dataForwarder.forwardToStudentApp(sensorData);

                } else if (topic.startsWith("sensor/watersupply/")) {
                    // 解析供水机数据
                    Map<String, Object> sensorData = parseWaterSupplyData(topic, payload);
                    dataForwarder.forwardToWeb(sensorData);
                    dataForwarder.forwardToRepairApp(sensorData);

                } else if (topic.startsWith("alert/")) {
                    // 解析告警数据
                    Map<String, Object> alertData = parseAlertMessage(topic, payload);
                    dataForwarder.forwardToWeb(alertData);
                    dataForwarder.forwardToRepairApp(alertData);
                }
            } catch (Exception e) {
                System.err.println("处理MQTT消息异常: " + e.getMessage());
                e.printStackTrace();
            }
        };
    }

    private Map<String, Object> parseWaterMakerData(String topic, String payload) {
        Map<String, Object> data = new HashMap<>();
        parseCommonData(topic, payload, data);
        data.put("deviceType", 1); // 制水机

        JSONObject json = JSONObject.parseObject(payload);
        data.put("tdsValue", json.getDouble("tds"));
        data.put("waterFlow", json.getDouble("flow"));
        data.put("waterPressure", json.getDouble("pressure"));
        data.put("filterLife", json.getInteger("filter_life"));
        data.put("leakage", json.getBoolean("leakage"));

        data.put("status", determineStatus(data));
        return data;
    }

    private Map<String, Object> parseWaterSupplyData(String topic, String payload) {
        Map<String, Object> data = new HashMap<>();
        parseCommonData(topic, payload, data);
        data.put("deviceType", 2); // 供水机

        JSONObject json = JSONObject.parseObject(payload);
        data.put("waterFlow", json.getDouble("flow"));
        data.put("waterPressure", json.getDouble("pressure"));
        data.put("waterLevel", json.getDouble("water_level"));

        data.put("status", determineStatus(data));
        return data;
    }

    private Map<String, Object> parseAlertMessage(String topic, String payload) {
        Map<String, Object> alert = new HashMap<>();

        String[] parts = topic.split("/");
        alert.put("deviceId", parts.length >= 2 ? parts[1] : "unknown");

        JSONObject json = JSONObject.parseObject(payload);
        alert.put("alertType", json.getString("alert_type"));
        alert.put("alertLevel", json.getString("alert_level"));
        alert.put("alertMessage", json.getString("alert_message"));
        alert.put("areaId", json.getString("area_id"));
        alert.put("deviceType", json.getInteger("device_type"));
        alert.put("timestamp", LocalDateTime.now().toString());

        return alert;
    }

    private void parseCommonData(String topic, String payload, Map<String, Object> data) {
        String[] parts = topic.split("/");
        data.put("deviceId", parts.length >= 3 ? parts[2] : "unknown");

        JSONObject json = JSONObject.parseObject(payload);
        data.put("areaId", json.getString("area_id"));
        data.put("temperature", json.getDouble("temperature"));
        data.put("humidity", json.getDouble("humidity"));
        data.put("timestamp", LocalDateTime.now().toString());
    }

    private String determineStatus(Map<String, Object> data) {
        Integer deviceType = (Integer) data.get("deviceType");

        if (deviceType == 1) { // 制水机
            Double tdsValue = (Double) data.get("tdsValue");
            Integer filterLife = (Integer) data.get("filterLife");
            Boolean leakage = (Boolean) data.get("leakage");

            if (tdsValue != null && tdsValue > 600) return "error";
            if (filterLife != null && filterLife < 10) return "error";
            if (leakage != null && leakage) return "error";
            if (tdsValue != null && tdsValue > 300) return "warning";
        } else if (deviceType == 2) { // 供水机
            Double waterLevel = (Double) data.get("waterLevel");
            if (waterLevel != null && waterLevel < 10) return "error";
            if (waterLevel != null && waterLevel < 20) return "warning";
        }
        return "normal";
    }
}