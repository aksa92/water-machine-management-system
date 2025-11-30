/**
 * 消息推送记录实体类
 * 对应表：message_push
 * 用于存储系统向用户、管理员、维修人员推送的消息记录
 */
package com.campus.water.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "message_push")
public class MessagePush {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Integer messageId;

    @Column(name = "student_id", length = 50)
    private String studentId;

    @Column(name = "admin_id", length = 50)
    private String adminId;

    @Column(name = "repairman_id", length = 50)
    private String repairmanId;

    @Column(name = "user_id", length = 50)
    private String userId;

    @Column(name = "user_type", length = 50)
    private String userType;

    @Column(name = "message_type", length = 50)
    private String messageType;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "is_read")
    private Boolean isRead = false;

    @Column(name = "push_time")
    private LocalDateTime pushTime = LocalDateTime.now();

    @Column(name = "related_id", length = 50)
    private String relatedId;
}