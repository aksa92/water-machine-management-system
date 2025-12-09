/**
 * 工单实体类
 * 对应表：work_order
 * 用于管理维修、保养、巡检等工单信息，包括状态、优先级、处理记录等
 */
package com.campus.water.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "work_order")
public class WorkOrder {
    @Id
    @Column(name = "order_id", length = 30)
    private String orderId;

    @Column(name = "alert_id")
    private Long alertId;

    @Column(name = "device_id", length = 20)
    private String deviceId;

    @Column(name = "area_id", length = 20)
    private String areaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", length = 50)
    private OrderType orderType;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", length = 50)
    private OrderPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private OrderStatus status = OrderStatus.pending;

    @Column(name = "assigned_repairman_id", length = 50)
    private String assignedRepairmanId;

    @Column(name = "created_time")
    private LocalDateTime createdTime = LocalDateTime.now();

    @Column(name = "grabbed_time")
    private LocalDateTime grabbedTime;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "completed_time")
    private LocalDateTime completedTime;

    @Column(name = "deal_note", columnDefinition = "TEXT")
    private String dealNote;

    @Column(name = "img_url", length = 500)
    private String imgUrl;

    @Column(name = "created_by", length = 50)
    private String createdBy;

    @Column(name = "updated_time")
    private LocalDateTime updatedTime = LocalDateTime.now();

    public enum OrderType {
        repair, maintenance, inspection
    }

    public enum OrderPriority {
        low, medium, high, urgent
    }

    public enum OrderStatus {
        pending,       // 原pending（待抢单）调整为待处理
        processing,     // 合并原grabbed（已抢单）和processing（处理中）
        reviewing,      // 新增状态：维修完成后等待审核
        completed,      // 原completed（已完成）保留
        timeout;          // 原timeout（超时）保留
    }
}