package com.campus.water.repair.config;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 维修APP端MQTT配置（仅订阅本辖区设备数据/告警，适配维修员权限需求）
 */
@Configuration
public class RepairMqttConfig {
    private String mqttBroker = "tcp://mqtt-server.campus.com:1883";
    private String clientIdPrefix = "campus-water-repair-"; // 客户端ID前缀（拼接维修员ID）

    // 动态生成客户端ID（按维修员ID区分）
    @Bean
    public String clientId(String repairmanId) {
        return clientIdPrefix + repairmanId;
    }

    @Bean
    public MqttClient mqttClient(String repairmanId) throws Exception {
        String clientId = clientId(repairmanId);
        MqttClient client = new MqttClient(mqttBroker, clientId, new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(false); // 重连后恢复订阅，避免错过工单
        options.setKeepAliveInterval(60);
        client.connect(options);
        System.out.println("维修APP（ID：" + repairmanId + "）已连接MQTT服务器");
        return client;
    }

    // 订阅主题（仅本辖区设备数据+告警，适配“维修员仅查看本辖区”需求）
    public String getDeviceSubscribeTopic(String areaId) {
        return "forward/repair/area/" + areaId + "/device/#";
    }

    public String getAlertSubscribeTopic(String areaId) {
        return "forward/repair/area/" + areaId + "/alert/#";
    }
}