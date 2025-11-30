/**
 * 终端使用统计实体类
 * 对应表：terminal_usage_stats
 * 用于记录终端设备的每日使用情况统计，如用水量、使用次数等
 */
package com.campus.water.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "terminal_usage_stats")
public class TerminalUsageStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stat_id")
    private Long statId;

    @Column(name = "terminal_id", length = 20)
    private String terminalId;

    @Column(name = "stat_date")
    private LocalDate statDate;

    @Column(name = "usage_count")
    private Integer usageCount = 0;

    @Column(name = "total_water_output", precision = 10, scale = 2)
    private Double totalWaterOutput = 0.0;

    @Column(name = "avg_water_per_use", precision = 6, scale = 2)
    private Double avgWaterPerUse = 0.0;

    @Column(name = "peak_hour", length = 5)
    private String peakHour;

    @Column(name = "created_time")
    private LocalDateTime createdTime = LocalDateTime.now();

    @Column(name = "updated_time")
    private LocalDateTime updatedTime = LocalDateTime.now();
}