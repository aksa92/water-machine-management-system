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
    public static final String BROKER = "ssl://b17be106.ala.cn-hangzhou.emqxsl.cn:8883";
    public static final String USERNAME = "test1";
    public static final String PASSWORD = "123456";
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
        options.setConnectionTimeout(CONNECTION_TIMEOUT / 1000);
        options.setKeepAliveInterval(KEEP_ALIVE_INTERVAL);
        options.setAutomaticReconnect(true);

        // 改进1: 改为持久会话，断线重连后保留未确认消息
        options.setCleanSession(false);

        // 改进2: 设置最大飞行窗口（允许最多100条未确认消息）
        options.setMaxInflight(100);

        // 在 MqttConfig 的 mqttClientFactory() 中增强连接选项
        options.setAutomaticReconnect(true);
        options.setMaxReconnectDelay(5000);

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
        String clientId = "sensor-sender-" + System.currentTimeMillis();
        MqttPahoMessageHandler handler = new MqttPahoMessageHandler(clientId, mqttClientFactory());

        handler.setAsync(true);
        // 改进3: 启用异步事件回调，支持发送确认
        handler.setAsyncEvents(true);
        // 改进4: 设置超时时间（5秒）
        handler.setCompletionTimeout(5000);
        handler.setDefaultQos(QOS);
        handler.setDefaultTopic(TOPIC_WATER_MAKER_STATE);

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