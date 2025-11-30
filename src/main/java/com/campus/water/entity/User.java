/**
 * 用户信息实体类（学生用户）
 * 对应表：user
 * 用于存储学生用户的基本信息、登录状态等
 */
package com.campus.water.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user")
public class User {
    @Id
    @Column(name = "student_id", length = 50)
    private String studentId;

    @Column(name = "student_name", length = 50)
    private String studentName;

    @Column(name = "password", length = 200)
    private String password;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private UserStatus status = UserStatus.active;

    @Column(name = "create_time")
    private LocalDateTime createTime = LocalDateTime.now();

    @Column(name = "updated_time")
    private LocalDateTime updatedTime = LocalDateTime.now();

    public enum UserStatus {
        active, inactive
    }
}