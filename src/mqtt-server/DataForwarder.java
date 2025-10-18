package com.campus.water.mqtt.core;

import com.alibaba.fastjson2.JSONObject;
import com.campus.water.mqtt.entity.SensorData;
import com.campus.water.mqtt.entity.AlertMessage;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 数据转发器（按需求转发到管理Web/学生APP/维修APP，实现多端数据同步）
 */
@Component
public class DataForwarder {
    @Autowired
    private MqttClient mqttClient;

    // 1. 转发传感器数据到管理Web（全量数据，适配管理员监控需求）
    public void forwardToWeb(SensorData sensorData) throws Exception {
        String topic = "forward/web/device/" + sensorData.getDeviceId();
        String payload = JSONObject.toJSONString(sensorData);
        publish(topic, payload, 0); // Web端QoS=0（普通监控数据）
    }

    // 2. 转发告警到管理Web（适配管理员查看告警需求）
    public void forwardToWeb(AlertMessage alert) throws Exception {
        String topic = "forward/web/alert/" + alert.getDeviceId();
        String payload = JSONObject.toJSONString(alert);
        publish(topic, payload, 1); // 告警QoS=1（确保管理员看到）
    }

    // 3. 转发制水机水质数据到学生APP（仅终端机关联的制水机，适配查水质需求）
    public void forwardToStudentApp(SensorData sensorData) throws Exception {
        // 仅制水机数据需转发（学生APP查水质）
        if (sensorData.getDeviceType() != 1) return;

        // 按终端机ID转发（需求中学生扫码查对应终端机的水质）
        String terminalId = getRelatedTerminalId(sensorData.getDeviceId());
        String topic = "forward/student/terminal/" + terminalId;
        String payload = JSONObject.toJSONString(new JSONObject() {{
            put("terminalId", terminalId);
            put("tdsValue", sensorData.getTdsValue());
            put("waterQuality", getWaterQuality(sensorData.getTdsValue())); // 水质等级
            put("timestamp", sensorData.getTimestamp());
        }});
        publish(topic, payload, 0);
    }

    // 4. 转发供水机水位/告警到维修APP（仅本辖区设备，适配维修员权限需求）
    public void forwardToRepairApp(SensorData sensorData) throws Exception {
        String areaId = sensorData.getAreaId(); // 设备所属片区（如AREA01）
        String topic = "forward/repair/area/" + areaId + "/device/" + sensorData.getDeviceId();
        String payload = JSONObject.toJSONString(sensorData);
        publish(topic, payload, 0);
    }

    public void forwardToRepairApp(AlertMessage alert) throws Exception {
        String areaId = alert.getAreaId();
        String topic = "forward/repair/area/" + areaId + "/alert/" + alert.getDeviceId();
        String payload = JSONObject.toJSONString(alert);
        publish(topic, payload, 1); // 告警QoS=1（确保维修员抢单）
    }

    // 通用发布方法
    private void publish(String topic, String payload, int qos) throws Exception {
        MqttMessage message = new MqttMessage(payload.getBytes());
        message.setQos(qos);
        mqttClient.publish(topic, message);
        System.out.println("MQTT服务器转发数据：主题=" + topic + "，内容=" + payload);
    }

    // 工具方法：根据制水机ID获取关联的终端机ID（需求中终端机关联制水机）
    private String getRelatedTerminalId(String deviceId) {
        // 实际需从MySQL设备表查询，此处模拟（如制水机WM001关联终端机TERM001/TERM002）
        return deviceId.equals("WM001") ? "TERM001" : "TERM002";
    }

    // 工具方法：根据TDS值判断水质等级（需求中“优质矿化水”标准）
    private String getWaterQuality(double tds) {
        if (tds < 50) return "纯净水（无矿物质）";
        else if (tds < 300) return "优质矿化水";
        else if (tds < 600) return "合格矿化水";
        else return "水质超标（不建议饮用）";
    }
}