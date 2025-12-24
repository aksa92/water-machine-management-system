/**
 * 区域信息实体类
 * 对应表：area
 * 用于管理校园、楼宇、区域等层级结构信息
 */
package com.campus.water.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.GenericGenerator;

@Data
@Entity
@Table(name = "area")
public class Area {
    @Id
    @GeneratedValue(generator = "uuid")  // 新增：自动生成UUID
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "area_id", length = 36)
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
        campus("校园"),
        building("楼宇"),
        zone("区域");

        private final String desc;

        AreaType(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }
}