package com.campus.water.web.mqtt;

import com.alibaba.fastjson2.JSONObject;
import com.campus.water.web.entity.SensorDataWebVO;
import com.campus.water.web.entity.AlertWebVO;
import com.campus.water.web.service.RealTimeService;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 管理Web端MQTT订阅器（接收实时设备数据/告警，适配管理员监控需求）
 */
@Component
public class WebMqttSubscriber {
    @Autowired
    private MqttClient mqttClient;
    @Autowired
    private String[] subscribeTopics;
    @Autowired
    private int[] qosLevels;
    @Autowired
    private RealTimeService realTimeService;

    @PostConstruct
    public void startSubscribe() throws Exception {
        for (int i = 0; i < subscribeTopics.length; i++) {
            String topic = subscribeTopics[i];
            int qos = qosLevels[i];
            mqttClient.subscribe(topic, qos, messageListener());
            System.out.println("管理Web已订阅主题：" + topic);
        }
    }

    private IMqttMessageListener messageListener() {
        return (topic, message) -> {
            String payload = new String(message.getPayload());
            System.out.println("管理Web接收数据：主题=" + topic + "，内容=" + payload);

            // 解析数据并缓存到内存（供前端轮询获取实时数据）
            if (topic.startsWith("forward/web/device/")) {
                SensorDataWebVO sensorVO = JSONObject.parseObject(payload, SensorDataWebVO.class);
                realTimeService.updateRealTimeData(sensorVO); // 缓存实时数据
            } else if (topic.startsWith("forward/web/alert/")) {
                AlertWebVO alertVO = JSONObject.parseObject(payload, AlertWebVO.class);
                realTimeService.addAlert(alertVO); // 缓存告警（前端告警列表实时更新）
            }
        };
    }
}