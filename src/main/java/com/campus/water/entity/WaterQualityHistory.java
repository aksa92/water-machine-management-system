/**
 * 水质历史数据实体类
 * 对应表：water_quality_history
 * 用于记录终端设备检测的水质历史数据，包括多个TDS值和水质评级
 */
package com.campus.water.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "water_quality_history")
public class WaterQualityHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId;

    @Column(name = "terminal_id", length = 20)
    private String terminalId;

    @Column(name = "device_id", length = 20)
    private String deviceId;

    // 根据文档修正：三个TDS值
    @Column(name = "tds_value1", precision = 8, scale = 2)
    private Double tdsValue1; // 原水TDS

    @Column(name = "tds_value2", precision = 8, scale = 2)
    private Double tdsValue2; // 纯水TDS

    @Column(name = "tds_value3", precision = 8, scale = 2)
    private Double tdsValue3; // 矿化水TDS

    @Column(name = "water_quality", length = 50)
    private String waterQuality;

    // 根据文档修正：字段名改为 detected_time
    @Column(name = "detected_time")
    private LocalDateTime detectedTime = LocalDateTime.now();

    @Column(name = "created_time")
    private LocalDateTime createdTime = LocalDateTime.now();
}