package com.campus.water.web.mqtt;

import com.alibaba.fastjson2.JSONObject;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

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

            // 直接使用JSONObject处理数据
            JSONObject data = JSONObject.parseObject(payload);

            if (topic.startsWith("forward/web/device/")) {
                realTimeService.updateRealTimeData(data);
            } else if (topic.startsWith("forward/web/alert/")) {
                realTimeService.addAlert(data);
            }
        };
    }
}