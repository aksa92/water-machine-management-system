package com.campus.water.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "water_quality_history")
public class WaterQualityHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "terminal_id", length = 20)
    private String terminalId;

    @Column(name = "device_id", length = 20)
    private String deviceId;

    @Column(name = "tds_value", precision = 8, scale = 2)
    private Double tdsValue;

    @Column(name = "water_quality", length = 50)
    private String waterQuality;

    @Column(name = "temperature", precision = 5, scale = 2)
    private Double temperature;

    @Column(name = "timestamp")
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "created_time")
    private LocalDateTime createdTime = LocalDateTime.now();
}