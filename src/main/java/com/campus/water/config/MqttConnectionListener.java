package com.campus.water.config;

import com.campus.water.service.MqttRedisCacheService;
import com.campus.water.service.MqttSensorSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MqttConnectionListener {

    private final MqttRedisCacheService cacheService;
    private final MqttSensorSender sensorSender;

    @Bean
    public MqttCallbackExtended mqttCallback() {
        return new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    log.info("MQTT重连成功，开始重放离线消息 | Broker：{}", serverURI);

                    int replayCount = cacheService.replayMessages(sensorSender);

                    log.info("离线消息重放完成 | 重放数量：{}", replayCount);
                } else {
                    log.info("MQTT首次连接成功 | Broker：{}", serverURI);
                }
            }

            @Override
            public void connectionLost(Throwable cause) {
                log.error("MQTT连接断开 | 原因：{}", cause.getMessage());
            }

            @Override
            public void messageArrived(String topic, org.eclipse.paho.client.mqttv3.MqttMessage message) {
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                log.debug("MQTT消息发送完成 | Message ID: {}", token.getMessageId());
            }
        };
    }
}
