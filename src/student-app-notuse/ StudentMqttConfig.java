package com.campus.water.student.config;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 学生APP端MQTT配置（仅订阅终端机水质数据，适配学生权限需求）
 */
@Configuration
public class StudentMqttConfig {
    private String mqttBroker = "tcp://mqtt-server.campus.com:1883";
    private String clientIdPrefix = "campus-water-student-"; // 客户端ID前缀（拼接学生ID）

    // 动态生成客户端ID（按学生ID区分，避免冲突）
    @Bean
    public String clientId(String studentId) {
        return clientIdPrefix + studentId;
    }

    @Bean
    public MqttClient mqttClient(String studentId) throws Exception {
        String clientId = clientId(studentId);
        MqttClient client = new MqttClient(mqttBroker, clientId, new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true); // 学生APP登录后重新订阅，无需保留会话
        options.setKeepAliveInterval(120); // 移动端心跳间隔 longer，适配电池续航
        client.connect(options);
        System.out.println("学生APP（ID：" + studentId + "）已连接MQTT服务器");
        return client;
    }

    // 订阅主题（仅终端机水质数据，适配学生“扫码查水质”需求）
    public String getSubscribeTopic(String terminalId) {
        return "forward/student/terminal/" + terminalId;
    }
}