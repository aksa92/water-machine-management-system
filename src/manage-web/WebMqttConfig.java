package com.campus.water.web.config;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebMqttConfig {
    // 修改为本地MQTT
    private String mqttBroker = "tcp://localhost:1883";
    private String clientId = "campus-water-admin-web";

    @Bean
    public MqttClient mqttClient() throws Exception {
        MqttClient client = new MqttClient(mqttBroker, clientId, new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(false);
        options.setKeepAliveInterval(60);
        client.connect(options);
        System.out.println("管理Web已连接MQTT服务器：" + mqttBroker);
        return client;
    }

    @Bean
    public String[] subscribeTopics() {
        return new String[]{
                "forward/web/device/#",
                "forward/web/alert/#"
        };
    }

    @Bean
    public int[] qosLevels() {
        return new int[]{0, 1};
    }
}