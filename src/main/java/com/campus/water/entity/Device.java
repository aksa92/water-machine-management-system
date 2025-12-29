// com/campus/water/entity/Device.java
package com.campus.water.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备信息实体类（对应原始device表，不新增字段）
 */
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

    @Column(name = "area_id", length = 20)
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

    // 新增：关联的制水机ID（仅供水机有值）
    @Column(name = "parent_maker_id", length = 36)
    private String parentMakerId;

    // 保留原有的remark方法（若表中有该字段可直接映射，无则忽略）
    private String remark;
    public void setRemark(String remark) {
        this.remark = remark;
    }

    public enum DeviceType {
        water_maker, water_supply
    }

    public enum DeviceStatus {
        online, offline, fault
    }
}