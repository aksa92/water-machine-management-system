package com.campus.water.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "device")
public class Device {
    @Id
    @Column(name = "device_id", length = 20)
    private String deviceId;

    @Column(name = "device_name", length = 100)
    private String deviceName;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_type", length = 50)
    private DeviceType deviceType;

    @Column(name = "area_id", length = 20)  // 确保这个字段存在
    private String areaId;

    @Column(name = "install_location", length = 200)
    private String installLocation;

    @Column(name = "install_date")
    private LocalDate installDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private DeviceStatus status = DeviceStatus.online;

    @Column(name = "create_time")
    private LocalDateTime createTime = LocalDateTime.now();

    public enum DeviceType {
        water_maker, water_supply
    }

    public enum DeviceStatus {
        online, offline, fault
    }
}