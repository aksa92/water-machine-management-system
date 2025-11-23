package com.campus.water.mqtt.core;

import com.alibaba.fastjson2.JSONObject;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class DataForwarder {
    @Autowired
    private MqttClient mqttClient;

    public void forwardToWeb(Map<String, Object> sensorData) throws Exception {
        String deviceId = (String) sensorData.get("deviceId");
        String topic = "forward/web/device/" + deviceId;
        String payload = JSONObject.toJSONString(sensorData);
        publish(topic, payload, 0);
        System.out.println("转发到管理Web: " + deviceId);
    }

    public void forwardToWeb(Map<String, Object> alertData, boolean isAlert) throws Exception {
        String deviceId = (String) alertData.get("deviceId");
        String topic = "forward/web/alert/" + deviceId;
        String payload = JSONObject.toJSONString(alertData);
        publish(topic, payload, 1);
        System.out.println("转发告警到管理Web: " + deviceId);
    }

    public void forwardToStudentApp(Map<String, Object> sensorData) throws Exception {
        Integer deviceType = (Integer) sensorData.get("deviceType");
        if (deviceType != 1) return;

        String deviceId = (String) sensorData.get("deviceId");
        String terminalId = getRelatedTerminalId(deviceId);
        String topic = "forward/student/terminal/" + terminalId;

        Double tdsValue = (Double) sensorData.get("tdsValue");
        String payload = JSONObject.toJSONString(new JSONObject() {{
            put("terminalId", terminalId);
            put("tdsValue", tdsValue);
            put("waterQuality", getWaterQuality(tdsValue));
            put("timestamp", sensorData.get("timestamp"));
        }});
        publish(topic, payload, 0);
        System.out.println("转发到学生APP: " + terminalId);
    }

    public void forwardToRepairApp(Map<String, Object> sensorData) throws Exception {
        String areaId = (String) sensorData.get("areaId");
        String deviceId = (String) sensorData.get("deviceId");
        String topic = "forward/repair/area/" + areaId + "/device/" + deviceId;
        String payload = JSONObject.toJSONString(sensorData);
        publish(topic, payload, 0);
        System.out.println("转发到维修APP: " + deviceId);
    }

    public void forwardToRepairApp(Map<String, Object> alertData, boolean isAlert) throws Exception {
        String areaId = (String) alertData.get("areaId");
        String deviceId = (String) alertData.get("deviceId");
        String topic = "forward/repair/area/" + areaId + "/alert/" + deviceId;
        String payload = JSONObject.toJSONString(alertData);
        publish(topic, payload, 1);
        System.out.println("转发告警到维修APP: " + deviceId);
    }

    private void publish(String topic, String payload, int qos) throws Exception {
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(qos);
        mqttClient.publish(topic, message);
    }

    private String getRelatedTerminalId(String deviceId) {
        return deviceId.equals("WM001") ? "TERM001" : "TERM002";
    }

    private String getWaterQuality(Double tds) {
        if (tds == null) return "未知";
        if (tds < 50) return "纯净水（无矿物质）";
        else if (tds < 300) return "优质矿化水";
        else if (tds < 600) return "合格矿化水";
        else return "水质超标（不建议饮用）";
    }
}