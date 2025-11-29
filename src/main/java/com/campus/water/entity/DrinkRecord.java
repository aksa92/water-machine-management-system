package com.campus.water.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "drink_record")
public class DrinkRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long recordId;

    @Column(name = "student_id", length = 50)
    private String studentId;

    @Column(name = "terminal_id", length = 20)
    private String terminalId;

    @Column(name = "device_id", length = 20)
    private String deviceId;

    // 根据文档修正：字段名改为 water_consumption
    @Column(name = "water_consumption", precision = 6, scale = 2)
    private Double waterConsumption;

    @Column(name = "water_quality", length = 50)
    private String waterQuality;

    @Column(name = "tds_value", precision = 8, scale = 2)
    private Double tdsValue;

    // 根据文档修正：字段名改为 drink_time
    @Column(name = "drink_time")
    private LocalDateTime drinkTime;

    @Column(name = "location", length = 200)
    private String location;

    @Column(name = "created_time")
    private LocalDateTime createdTime = LocalDateTime.now();
}