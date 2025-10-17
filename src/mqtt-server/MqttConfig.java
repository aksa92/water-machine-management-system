package com.campus.water.mqtt.config;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

/**
 * MQTT服务器配置（适配需求中Air724模块数据上传，对接MQTT Broker）
 */
@Configuration
public class MqttConfig {
    // 从配置文件读取Broker地址（需求中云端Broker，如EMQX）
    private String brokerUrl = "tcp://mqtt-server.campus.com:1883";
    private String clientId = "campus-water-mqtt-server"; // 服务器端客户端ID
    private int keepAlive = 60; // 心跳间隔（秒），适配Air724模块低功耗

    @Bean
    public MqttClient mqttClient() throws Exception {
        // 1. 创建MQTT客户端（内存持久化，避免服务器存储压力）
        MqttClient client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());

        // 2. 配置连接参数（确保数据可靠传输，适配需求中告警不丢失）
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(false); // 保持会话，重连后恢复订阅
        options.setKeepAliveInterval(keepAlive);
        options.setAutomaticReconnect(true); // 自动重连，适配网络波动

        // 3. 连接Broker
        client.connect(options);
        System.out.println("MQTT服务器已连接Broker：" + brokerUrl);
        return client;
    }

    // 订阅的主题（严格按需求中设备类型划分，便于权限隔离）
    @Bean
    public String[] subscribeTopics() {
        return new String[]{
                "sensor/watermaker/#",  // 制水机传感器数据（TDS/水压/流量，需求图3）
                "sensor/watersupply/#", // 供水机传感器数据（水位/漏水，需求图3）
                "alert/#"               // 设备告警报文（滤芯损坏/水位异常，需求告警流程）
        };
    }

    // QoS配置（需求中告警用QoS=1确保送达，普通传感器数据QoS=0）
    @Bean
    public int[] qosLevels() {
        return new int[]{0, 0, 1}; // 告警QoS=1，传感器数据QoS=0
    }
}