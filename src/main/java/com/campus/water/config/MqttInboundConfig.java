package com.campus.water.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;

@Configuration
public class MqttInboundConfig {

    /**
     * MQTT 接收适配器（监听MQTT主题，接收消息并转发到通道）
     */
    @Bean
    public MqttPahoMessageDrivenChannelAdapter mqttInbound(MqttConfig mqttConfig) {
        // 接收端客户端ID（与发送端区分）
        String clientId = "sensor-receiver-" + System.currentTimeMillis();

        // 创建适配器：指定客户端ID、工厂、默认订阅主题（可后续动态添加）
        MqttPahoMessageDrivenChannelAdapter adapter = new MqttPahoMessageDrivenChannelAdapter(
                clientId,
                mqttConfig.mqttClientFactory()
        );

        // 配置消息转换器（默认UTF-8编码，支持JSON格式）
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(mqttConfig.QOS); // 订阅QOS等级与发送端一致
        adapter.setOutputChannel(mqttConfig.mqttInputChannel()); // 消息转发到接收通道

        // 开启异常重试（避免网络波动导致消息丢失）
        adapter.setRecoveryInterval(5000); // 重试间隔5秒

        return adapter;
    }
}