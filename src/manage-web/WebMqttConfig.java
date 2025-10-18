package com.campus.water.web.config;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 管理Web端MQTT配置（订阅MQTT服务器转发的全量数据，适配管理员监控需求）
 */
@Configuration
public class WebMqttConfig {
    // MQTT服务器转发的主题前缀（需求中全量设备数据/告警）
    private String mqttBroker = "tcp://mqtt-server.campus.com:1883";
    private String clientId = "campus-water-admin-web"; // Web端客户端ID

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

    // 订阅的主题（全量设备数据+全量告警，适配管理员权限）
    @Bean
    public String[] subscribeTopics() {
        return new String[]{
                "forward/web/device/#",  // 全量设备实时数据
                "forward/web/alert/#"    // 全量告警报文
        };
    }

    @Bean
    public int[] qosLevels() {
        return new int[]{0, 1}; // 告警QoS=1，设备数据QoS=0
    }
}