package com.campus.water.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class MqttRedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ListOperations<String, Object> listOperations;
    private final ObjectMapper objectMapper;

    @Value("${mqtt.offline-cache.max-queue-size:10000}")
    private int maxQueueSize;

    @Value("${mqtt.offline-cache.dedup-expire-minutes:5}")
    private int dedupExpireMinutes;

    @Value("${mqtt.offline-cache.replay-delay-ms:100}")
    private long replayDelayMs;

    private final MqttPahoMessageHandler mqttMessageHandler;

    private static final String OFFLINE_QUEUE_KEY = "mqtt:offline:queue";
    private static final String DEDUP_CACHE_KEY_PREFIX = "mqtt:dedup:";
    private static final String STATS_KEY = "mqtt:cache:stats";

    @Data
    public static class OfflineMessage {
        private String topic;
        private String payload;
        private int qos;
        private long timestamp;
        private String messageId;

        public OfflineMessage() {}

        public OfflineMessage(String topic, String payload, int qos) {
            this.topic = topic;
            this.payload = payload;
            this.qos = qos;
            this.timestamp = System.currentTimeMillis();
            this.messageId = UUID.randomUUID().toString();
        }
    }

    @PostConstruct
    public void init() {
        Long size = redisTemplate.opsForList().size(OFFLINE_QUEUE_KEY);
        log.info("MQTT Redis离线缓存服务初始化完成 | 当前队列消息数：{}", size != null ? size : 0);
    }

    /**
     * 缓存离线消息到Redis
     */
    public boolean cacheMessage(String topic, String payload, int qos) {
        try {
            Long currentSize = redisTemplate.opsForList().size(OFFLINE_QUEUE_KEY);
            if (currentSize != null && currentSize >= maxQueueSize) {
                log.error("Redis离线队列已满（{}条），丢弃消息 | 主题：{}", currentSize, topic);
                incrementStat("dropped");
                return false;
            }

            OfflineMessage message = new OfflineMessage(topic, payload, qos);

            if (isDuplicateMessage(message.getMessageId(), topic)) {
                log.debug("检测到重复消息，跳过缓存 | 主题：{}", topic);
                return false;
            }

            listOperations.rightPush(OFFLINE_QUEUE_KEY, message);

            redisTemplate.expire(OFFLINE_QUEUE_KEY, Duration.ofHours(24));

            incrementStat("cached");
            log.info("消息已缓存到Redis | 主题：{} | 队列大小：{}", topic, currentSize != null ? currentSize + 1 : 1);

            return true;

        } catch (Exception e) {
            log.error("缓存消息到Redis失败 | 主题：{} | 错误：{}", topic, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 批量重放离线消息
     */
    public int replayMessages(MqttSensorSender sender) {
        int replayCount = 0;

        try {
            while (true) {
                Object obj = listOperations.leftPop(OFFLINE_QUEUE_KEY);
                if (obj == null) {
                    break;
                }

                OfflineMessage message = convertToOfflineMessage(obj);
                if (message == null) {
                    continue;
                }

                try {
                    resendMessage(message);
                    replayCount++;
                    incrementStat("replayed");

                    Thread.sleep(replayDelayMs);

                } catch (Exception e) {
                    log.error("重放消息失败 | 消息ID：{} | 错误：{}", message.getMessageId(), e.getMessage());

                    listOperations.rightPush(OFFLINE_QUEUE_KEY, message);
                }
            }

            log.info("离线消息重放完成 | 重放数量：{}", replayCount);

        } catch (Exception e) {
            log.error("重放过程发生异常", e);
        }

        return replayCount;
    }

    /**
     * 重放消息：将缓存的原始消息重新发送到 MQTT
     */
    private void resendMessage(OfflineMessage message) {
        try {
            Message<String> mqttMessage = MessageBuilder
                    .withPayload(message.getPayload())
                    .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                    .setHeader("mqtt_topic", message.getTopic())
                    .setHeader("mqtt_qos", message.getQos())
                    .setHeader("message_id", message.getMessageId())
                    .build();

            mqttMessageHandler.handleMessage(mqttMessage);
            log.info("离线消息重放成功 | 消息ID：{} | 主题：{}", message.getMessageId(), message.getTopic());
        } catch (Exception e) {
            log.error("离线消息重放失败 | 消息ID：{} | 主题：{} | 错误：{}",
                    message.getMessageId(), message.getTopic(), e.getMessage());
            throw e;
        }
    }

    /**
     * 检查是否为重复消息（基于Redis）
     */
    private boolean isDuplicateMessage(String messageId, String topic) {
        String dedupKey = DEDUP_CACHE_KEY_PREFIX + Math.abs(topic.hashCode()) % 1000;

        Boolean exists = redisTemplate.opsForSet().isMember(dedupKey, messageId);
        if (Boolean.TRUE.equals(exists)) {
            return true;
        }

        redisTemplate.opsForSet().add(dedupKey, messageId);
        redisTemplate.expire(dedupKey, dedupExpireMinutes, TimeUnit.MINUTES);

        Long setSize = redisTemplate.opsForSet().size(dedupKey);
        if (setSize != null && setSize > 5000) {
            redisTemplate.delete(dedupKey);
        }

        return false;
    }

    /**
     * 更新统计信息
     */
    private void incrementStat(String statType) {
        try {
            String key = STATS_KEY + ":" + statType;
            redisTemplate.opsForValue().increment(key, 1);
            redisTemplate.expire(key, Duration.ofDays(7));
        } catch (Exception e) {
            log.warn("更新统计信息失败", e);
        }
    }

    /**
     * 获取缓存统计信息
     */
    public CacheStats getStats() {
        CacheStats stats = new CacheStats();

        try {
            Long queueSize = redisTemplate.opsForList().size(OFFLINE_QUEUE_KEY);
            stats.setCurrentQueueSize(queueSize != null ? queueSize.intValue() : 0);

            Long cached = (Long) redisTemplate.opsForValue().get(STATS_KEY + ":cached");
            stats.setTotalCached(cached != null ? cached : 0);

            Long replayed = (Long) redisTemplate.opsForValue().get(STATS_KEY + ":replayed");
            stats.setTotalReplayed(replayed != null ? replayed : 0);

            Long dropped = (Long) redisTemplate.opsForValue().get(STATS_KEY + ":dropped");
            stats.setTotalDropped(dropped != null ? dropped : 0);

        } catch (Exception e) {
            log.error("获取统计信息失败", e);
        }

        return stats;
    }

    /**
     * 清空离线队列（慎用）
     */
    public void clearQueue() {
        redisTemplate.delete(OFFLINE_QUEUE_KEY);
        log.warn("离线消息队列已被手动清空");
    }

    /**
     * 获取队列中指定范围的消息（用于调试）
     */
    public List<Object> peekMessages(int start, int end) {
        return listOperations.range(OFFLINE_QUEUE_KEY, start, end);
    }

    @Data
    public static class CacheStats {
        private long totalCached;
        private long totalReplayed;
        private long totalDropped;
        private int currentQueueSize;
    }

    @SuppressWarnings("unchecked")
    private OfflineMessage convertToOfflineMessage(Object obj) {
        if (obj instanceof OfflineMessage) {
            return (OfflineMessage) obj;
        }

        try {
            String json = objectMapper.writeValueAsString(obj);
            return objectMapper.readValue(json, OfflineMessage.class);
        } catch (Exception e) {
            log.error("转换离线消息对象失败", e);
            return null;
        }
    }
}
