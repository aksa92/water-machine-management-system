package com.campus.water.mqtt.config;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqttConfig {
    // 修改为本地MQTT broker
    private String brokerUrl = "tcp://localhost:1883";
    private String clientId = "campus-water-mqtt-server";
    private int keepAlive = 60;

    @Bean
    public MqttClient mqttClient() throws Exception {
        MqttClient client = new MqttClient(brokerUrl, clientId, new MemoryPersistence());

        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(false);
        options.setKeepAliveInterval(keepAlive);
        options.setAutomaticReconnect(true);

        client.connect(options);
        System.out.println("MQTT服务器已连接Broker：" + brokerUrl);
        return client;
    }

    @Bean
    public String[] subscribeTopics() {
        return new String[]{
                "sensor/watermaker/#",
                "sensor/watersupply/#",
                "alert/#"
        };
    }

    @Bean
    public int[] qosLevels() {
        return new int[]{0, 0, 1};
    }
}