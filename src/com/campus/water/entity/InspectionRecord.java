package com.campus.water.entity;

import lombok.Data;
import javax.persistence.*;
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