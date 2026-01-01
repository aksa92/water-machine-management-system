package main.java.com.campus.water.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 维修人员通知实体
 * 存储派单、系统通知等消息
 */
@Data
@Entity
@Table(name = "notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 维修人员ID */
    @Column(name = "repairman_id", nullable = false, length = 50)
    private String repairmanId;

    /** 关联工单ID */
    @Column(name = "order_id", length = 50)
    private String orderId;

    /** 通知内容 */
    @Column(name = "content", nullable = false, length = 500)
    private String content;

    /** 是否已读（默认未读） */
    @Column(name = "is_read")
    private boolean isRead = false;

    /** 创建时间 */
    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime = LocalDateTime.now();

    /** 通知类型 */
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private NotificationType type;

    /** 通知类型枚举 */
    public enum NotificationType {
        ORDER_ASSIGNED, // 派单通知
        ORDER_GRABBED,  // 抢单通知
        ORDER_REJECTED, // 拒单通知
        SYSTEM          // 系统通知
    }
}