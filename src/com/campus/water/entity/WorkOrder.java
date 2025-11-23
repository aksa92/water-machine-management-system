package com.campus.water.entity;

import lombok.Data;
import javax.persistence.*;
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
        pending, grabbed, processing, completed, closed, timeout
    }
}