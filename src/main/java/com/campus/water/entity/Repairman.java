/**
 * 维修人员信息实体类
 * 对应表：repairman
 * 用于存储维修人员信息，包括技能、状态、工作量、评分等
 */
package com.campus.water.entity;

import lombok.Data;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "repairman")
public class Repairman {
    @Id
    @Column(name = "repairman_id", length = 50)
    private String repairmanId;

    @Column(name = "repairman_name", length = 100)
    private String repairmanName;

    @Column(length = 20)
    private String phone;

    @Column(name = "area_id", length = 36)
    private String areaId;

    @Column(name = "skills", length = 200)
    private String skills;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private RepairmanStatus status = RepairmanStatus.idle;

    @Column(name = "work_count")
    private Integer workCount = 0;

    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating ;

    @Column(name = "created_time")
    private LocalDateTime createdTime = LocalDateTime.now();

    public enum RepairmanStatus {
        idle, busy, vacation
    }
}