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

    // 根据文档修正：三个TDS值
    @Column(name = "tds_value1", precision = 8, scale = 2)
    private Double tdsValue1; // 原水TDS

    @Column(name = "tds_value2", precision = 8, scale = 2)
    private Double tdsValue2; // 纯水TDS

    @Column(name = "tds_value3", precision = 8, scale = 2)
    private Double tdsValue3; // 矿化水TDS

    // 根据文档修正：两个流量计
    @Column(name = "water_flow1", precision = 8, scale = 2)
    private Double waterFlow1;

    @Column(name = "water_flow2", precision = 8, scale = 2)
    private Double waterFlow2;

    // 根据文档修正：字段名改为 water_press
    @Column(name = "water_press", precision = 8, scale = 2)
    private Double waterPress;

    @Column(name = "filter_life")
    private Integer filterLife;

    @Column(name = "leakage")
    private Boolean leakage = false;

    @Column(name = "water_quality", length = 50)
    private String waterQuality;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private DeviceStatus status = DeviceStatus.normal;

    // 根据文档修正：字段名改为 record_time
    @Column(name = "record_time")
    private LocalDateTime recordTime = LocalDateTime.now();

    @Column(name = "created_time")
    private LocalDateTime createdTime = LocalDateTime.now();

    public enum DeviceStatus {
        normal, warning, error
    }
}