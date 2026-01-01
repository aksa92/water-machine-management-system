/**
 * 实时供水数据实体类
 * 对应表：water_supply_realtime_data
 * 用于存储供水设备的实时运行数据，如流量、压力、水位、温度等
 */
package main.java.com.campus.water.entity;

import lombok.Data;
import jakarta.persistence.*;

import java.math.BigDecimal;
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
    private BigDecimal waterFlow;

    // 根据文档修正：字段名改为 water_press
    @Column(name = "water_press", precision = 8, scale = 2)
    private BigDecimal  waterPress;

    @Column(name = "water_level", precision = 8, scale = 2)
    private BigDecimal  waterLevel;

    @Column(name = "temperature", precision = 5, scale = 2)
    private BigDecimal  temperature;

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