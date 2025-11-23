package com.campus.water.entity;

import lombok.Data;
import javax.persistence.*;
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

    @Column(name = "area_id", length = 20)
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
        info, warning, error, critical
    }

    public enum AlertStatus {
        pending, processing, resolved, closed
    }
}