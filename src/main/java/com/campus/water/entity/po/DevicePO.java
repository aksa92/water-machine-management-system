package com.campus.water.entity.po;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 设备实体类（数据库映射对象）
 * 存储设备基本信息及运行状态
 */
@Data
@Entity
@Table(name = "device")
public class DevicePO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 设备ID
    private String deviceId; // 设备唯一标识
    private String areaId; // 所属区域ID
    private String status; // 设备状态：online（在线）、offline（离线）
    private LocalDateTime lastActiveTime; // 最后活动时间（用于判断在线状态）
    private String deviceType; // 设备类型
    private LocalDateTime createTime; // 创建时间
    private LocalDateTime updateTime; // 更新时间
}