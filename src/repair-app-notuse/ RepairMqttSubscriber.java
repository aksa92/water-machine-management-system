package com.campus.water.repair.mqtt;

import com.alibaba.fastjson2.JSONObject;
import com.campus.water.repair.config.RepairMqttConfig;
import com.campus.water.repair.entity.AreaDeviceVO;
import com.campus.water.repair.entity.RepairAlertVO;
import com.campus.water.repair.service.AreaDeviceService;
import com.campus.water.repair.service.WorkOrderService;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 维修APP端MQTT订阅器（仅接收本辖区设备数据/告警，适配“抢单/巡检”需求）
 */
@Component
public class RepairMqttSubscriber {
    @Autowired
    private RepairMqttConfig mqttConfig;
    @Autowired
    private AreaDeviceService areaDeviceService;
    @Autowired
    private WorkOrderService workOrderService;

    // 维修员登录后订阅本辖区数据（先查询维修员所属片区）
    public void subscribeAreaData(String repairmanId, String areaId) throws Exception {
        MqttClient client = mqttConfig.mqttClient(repairmanId);

        // 1. 订阅本辖区设备数据（供水机水位/制水机状态）
        String deviceTopic = mqttConfig.getDeviceSubscribeTopic(areaId);
        client.subscribe(deviceTopic, 0, deviceMessageListener(areaId));

        // 2. 订阅本辖区告警报文（触发抢单）
        String alertTopic = mqttConfig.getAlertSubscribeTopic(areaId);
        client.subscribe(alertTopic, 1, alertMessageListener(areaId));

        System.out.println("维修APP（ID：" + repairmanId + "）已订阅片区" + areaId + "数据");
    }

    // 设备数据监听器（缓存辖区设备状态，供巡检使用）
    private IMqttMessageListener deviceMessageListener(String areaId) {
        return (topic, message) -> {
            String payload = new String(message.getPayload());
            AreaDeviceVO deviceVO = JSONObject.parseObject(payload, AreaDeviceVO.class);
            areaDeviceService.updateAreaDeviceStatus(areaId, deviceVO);
            System.out.println("维修APP接收片区" + areaId + "设备数据：" + payload);
        };
    }

    // 告警监听器（新告警触发工单，推送抢单提醒）
    private IMqttMessageListener alertMessageListener(String areaId) {
        return (topic, message) -> {
            String payload = new String(message.getPayload());
            RepairAlertVO alertVO = JSONObject.parseObject(payload, RepairAlertVO.class);
            // 触发工单生成（存入MySQL）并推送抢单提醒
            workOrderService.createWorkOrderFromAlert(alertVO);
            System.out.println("维修APP接收片区" + areaId + "告警：" + payload + "（已生成工单）");
        };
    }
}