/**
 * 区域信息实体类
 * 对应表：area
 * 用于管理校园、楼宇、区域等层级结构信息
 */
package com.campus.water.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "area")
public class Area {
    @Id
    @Column(name = "area_id", length = 20)
    private String areaId;

    @Column(name = "area_name", length = 100)
    private String areaName;

    @Enumerated(EnumType.STRING)
    @Column(name = "area_type", length = 50)
    private AreaType areaType;

    @Column(name = "parent_area_id", length = 20)
    private String parentAreaId;

    @Column(length = 200)
    private String address;

    @Column(length = 50)
    private String manager;

    @Column(name = "manager_phone", length = 20)  // 确保这个字段存在
    private String managerPhone;

    @Column(name = "created_time")
    private LocalDateTime createdTime = LocalDateTime.now();

    @Column(name = "updated_time")
    private LocalDateTime updatedTime = LocalDateTime.now();

    public enum AreaType {
        campus, building, zone
    }
}