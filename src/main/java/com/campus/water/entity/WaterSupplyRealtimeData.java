package com.campus.water.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "water_supply_realtime_data")
public class WaterSupplyRealtimeData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_id", length = 20)
    private String deviceId;

    @Column(name = "water_flow", precision = 8, scale = 2)
    private Double waterFlow;

    // 根据文档修正：字段名改为 water_press
    @Column(name = "water_press", precision = 8, scale = 2)
    private Double waterPress;

    @Column(name = "water_level", precision = 8, scale = 2)
    private Double waterLevel;

    @Column(name = "temperature", precision = 5, scale = 2)
    private Double temperature;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private DeviceStatus status = DeviceStatus.normal;

    @Column(name = "timestamp")
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "created_time")
    private LocalDateTime createdTime = LocalDateTime.now();

    public enum DeviceStatus {
        normal, warning, error
    }
}