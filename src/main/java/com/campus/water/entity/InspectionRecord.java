/**
 * 巡检记录实体类
 * 对应表：inspection_record
 * 用于记录维修人员的巡检结果，包括状态、异常描述、照片等
 */
package com.campus.water.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "inspection_record")
public class InspectionRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inspection_id")
    private Integer inspectionId;

    @Column(name = "order_id", length = 30)
    private String orderId;

    @Column(name = "repairman_id", length = 50)
    private String repairmanId;

    @Column(name = "device_id", length = 20)
    private String deviceId;

    @Enumerated(EnumType.STRING)
    @Column(name = "inspection_status", length = 50)
    private InspectionStatus inspectionStatus;

    @Column(name = "abnormal_description", columnDefinition = "LONGTEXT")
    private String abnormalDescription;

    @Column(name = "inspection_photo", columnDefinition = "LONGTEXT")
    private String inspectionPhoto;

    @Column(name = "inspection_time")
    private LocalDateTime inspectionTime;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt = LocalDateTime.now();

    public enum InspectionStatus {
        normal, abnormal
    }
}