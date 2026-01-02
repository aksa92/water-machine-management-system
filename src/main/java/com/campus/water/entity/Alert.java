/**
 * 告警信息实体类
 * 对应表：alert
 * 用于记录设备告警信息，包括告警级别、状态、处理人等
 */
package com.campus.water.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "alert")
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_id")
    private Long alertId;

    @Column(name = "device_id", length = 20)
    private String deviceId;

    @Column(name = "alert_type", length = 50)
    private String alertType;

    @Enumerated(EnumType.STRING)
    @Column(name = "alert_level", length = 50)
    private AlertLevel alertLevel;

    @Column(name = "alert_message", columnDefinition = "TEXT")
    private String alertMessage;

    @Column(name = "area_id", length = 36)
    private String areaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private AlertStatus status = AlertStatus.pending;

    @Column(name = "timestamp")
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "resolved_time")
    private LocalDateTime resolvedTime;

    @Column(name = "resolved_by", length = 50)
    private String resolvedBy;

    @Column(name = "created_time")
    private LocalDateTime createdTime = LocalDateTime.now();

    @Column(name = "updated_time")
    private LocalDateTime updatedTime = LocalDateTime.now();

    public enum AlertLevel {
        info("一般", 1),         // 信息级（如状态通知，无需处理）
        warning("一般", 2),      // 警告级（需关注，非紧急）
        error("紧急", 3),        // 错误级（需立即处理）
        critical("紧急", 4);     // 严重级（影响服务，最高优先级）

        private final String levelName; // 分级名称（一般/紧急）
        private final int priority;     // 处理优先级（1-4，升序）

        AlertLevel(String levelName, int priority) {
            this.levelName = levelName;
            this.priority = priority;
        }

        // 获取分级名称（用于前端展示）
        public String getLevelName() {
            return levelName;
        }

        // 获取优先级（用于推送排序）
        public int getPriority() {
            return priority;
        }
    }

    public enum AlertStatus {
        pending, processing, resolved, closed
    }
}