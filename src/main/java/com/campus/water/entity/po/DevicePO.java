package com.campus.water.entity.po;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 设备实体类（数据库映射对象）
 * 存储设备基本基本信息及运行状态
 */
@Data
@Entity
@Table(name = "device_po")
public class DevicePO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 设备自增主键

    @Column(unique = true, nullable = false, length = 20) // 确保业务唯一标识
    private String deviceId; // 设备唯一标识（业务ID）

    // 解决area_id映射冲突：明确字段定义，与Device实体类保持长度一致
    @Column(name = "area_id", length = 20) // 显式指定列名和长度，与Device类匹配
    private String areaId; // 所属区域ID

    @Column(length = 50) // 与Device的DeviceStatus枚举存储长度一致
    private String status; // 设备状态：online（在线）、offline（离线）、fault（故障）

    private LocalDateTime lastActiveTime; // 最后活动时间（用于判断在线状态）

    @Column(length = 50) // 与Device的DeviceType枚举存储长度一致
    private String deviceType; // 设备类型：water_maker（制水机）、water_supply（供水机）

    // 解决create_time字段映射冲突：显式指定列名与字段属性，确保与数据库表结构一致
    @Column(name = "create_time", nullable = false, updatable = false)
    private LocalDateTime createTime; // 创建时间

    @Column(name = "update_time")
    private LocalDateTime updateTime; // 更新时间

    // 新增：初始化时间字段的方法，确保与Device实体类行为一致
    @PrePersist
    public void prePersist() {
        if (createTime == null) {
            createTime = LocalDateTime.now();
        }
        if (updateTime == null) {
            updateTime = LocalDateTime.now();
        }
    }

    // 新增：更新时自动刷新updateTime
    @PreUpdate
    public void preUpdate() {
        updateTime = LocalDateTime.now();
    }
}