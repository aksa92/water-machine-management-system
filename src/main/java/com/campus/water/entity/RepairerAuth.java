/**
 * 维修人员认证信息实体类
 * 对应表：repairer_auth
 * 用于存储维修人员的登录账号、密码、状态等信息
 */
package com.campus.water.entity;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "repairer_auth")
public class RepairerAuth {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auth_id")
    private Long authId;

    @Column(name = "repairman_id", length = 50)
    private String repairmanId;

    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "password", length = 200)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", length = 50)
    private AccountStatus accountStatus = AccountStatus.active;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "created_time")
    private LocalDateTime createdTime = LocalDateTime.now();

    @Column(name = "updated_time")
    private LocalDateTime updatedTime = LocalDateTime.now();

    public enum AccountStatus {
        active, inactive, locked
    }
}