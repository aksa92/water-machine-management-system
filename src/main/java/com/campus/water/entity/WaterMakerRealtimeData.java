package com.campus.water.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "water_maker_realtime_data")
public class WaterMakerRealtimeData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", length = 20)
    private String deviceId;

    @Column(name = "tds_value", precision = 8, scale = 2)
    private Double tdsValue;

    @Column(name = "water_flow", precision = 8, scale = 2)
    private Double waterFlow;

    @Column(name = "water_pressure", precision = 8, scale = 2)
    private Double waterPressure;

    @Column(name = "filter_life")
    private Integer filterLife;

    @Column(name = "leakage")
    private Boolean leakage = false;

    @Column(name = "temperature", precision = 5, scale = 2)
    private Double temperature;

    @Column(name = "humidity", precision = 5, scale = 2)
    private Double humidity;

    @Column(name = "water_quality", length = 50)
    private String waterQuality;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private DeviceStatus status = DeviceStatus.normal;

    @Column(name = "timestamp")
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "created_time")
    private LocalDateTime createdTime = LocalDateTime.now();

    public enum DeviceStatus {
        normal, warning, error
    }
}