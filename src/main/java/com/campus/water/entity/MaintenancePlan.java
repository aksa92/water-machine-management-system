/**
 * 维护计划实体类
 * 对应表：maintenance_plan
 * 用于制定和管理设备的定期维护计划
 */
package com.campus.water.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "maintenance_plan")
public class MaintenancePlan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Integer planId;

    @Column(name = "device_id", length = 20)
    private String deviceId;

    @Column(name = "maintenance_type", length = 50)
    private String maintenanceType;

    @Column(name = "cycle_days")
    private Integer cycleDays;

    @Column(name = "last_maintenance_date")
    private LocalDate lastMaintenanceDate;

    @Column(name = "next_maintenance_date")
    private LocalDate nextMaintenanceDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan_status", length = 50)
    private PlanStatus planStatus;

    @Column(name = "created_time")
    private LocalDateTime createdTime = LocalDateTime.now();

    public enum PlanStatus {
        effective, expired, closed
    }
}