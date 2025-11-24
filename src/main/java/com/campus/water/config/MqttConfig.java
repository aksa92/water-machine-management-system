package com.campus.water.config;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.MessageChannel;

@Configuration
public class MqttConfig {
    // MQTT 基础配置（可迁移到 application.yml 中，用 @ConfigurationProperties 绑定）
    public static final String BROKER = "tcp://b17be106.ala.cn-hangzhou.emqxsl.cn:8883";
    public static final String USERNAME = "admin";
    public static final String PASSWORD = "12345678";
    public static final int QOS = 1; // 消息质量等级（1=确保送达，不重复）
    public static final int CONNECTION_TIMEOUT = 30000; // 连接超时（毫秒）
    public static final int KEEP_ALIVE_INTERVAL = 60; // 心跳间隔（秒）

    // MQTT 主题定义（按设备类型+功能分层）
    public static final String TOPIC_WATER_MAKER_STATE = "/device/state/water_maker/";
    public static final String TOPIC_WATER_MAKER_WARN = "/device/warn/water_maker/";
    public static final String TOPIC_WATER_SUPPLIER_STATE = "/device/state/water_supply/";
    public static final String TOPIC_WATER_SUPPLIER_WARN = "/device/warn/water_supply/";

    /**
     * MQTT 客户端工厂（Spring 管理，统一创建客户端）
     */
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();

        // 配置连接参数
        options.setServerURIs(new String[]{BROKER});
        options.setUserName(USERNAME);
        options.setPassword(PASSWORD.toCharArray());
        options.setConnectionTimeout(CONNECTION_TIMEOUT / 1000); // 转换为秒
        options.setKeepAliveInterval(KEEP_ALIVE_INTERVAL);
        options.setAutomaticReconnect(true); // 断线自动重连
        options.setCleanSession(true); // 断开后清除会话

        factory.setConnectionOptions(options);
        return factory;
    }

    /**
     * 发送消息通道（DirectChannel：同步发送，确保消息顺序）
     */
    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }

    /**
     * MQTT 消息发送处理器（封装发送逻辑）
     */
    @Bean
    public MqttPahoMessageHandler mqttOutbound() {
        // 客户端ID：前缀+时间戳，避免重复
        String clientId = "sensor-sender-" + System.currentTimeMillis();
        MqttPahoMessageHandler handler = new MqttPahoMessageHandler(clientId, mqttClientFactory());

        handler.setAsync(true); // 异步发送（不阻塞主线程）
        handler.setDefaultQos(QOS); // 默认QOS等级
        handler.setDefaultTopic(TOPIC_WATER_MAKER_STATE); // 默认主题（可在发送时覆盖）

        return handler;
    }

    /**
     * 接收消息通道（用于接收端转发消息）
     */
    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }
}