package com.campus.water.entity;

import lombok.Data;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "admin")
public class Admin {
    @Id
    @Column(name = "admin_id", length = 50)
    private String adminId;

    @Column(name = "admin_name", length = 50)
    private String adminName;

    @Column(name = "password", length = 200)
    private String password;

    @Column(name = "phone", length = 20)
    private String phone;

    // 恢复三个角色枚举
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 50, nullable = false)
    private AdminRole role;

    @Column(name = "created_time")
    private LocalDateTime createdTime = LocalDateTime.now();

    @Column(name = "updated_time")
    private LocalDateTime updatedTime = LocalDateTime.now();

    // 枚举类：恢复super_admin、area_admin、viewer三个角色
    public enum AdminRole {
        super_admin,  // 超级管理员
        area_admin,   // 区域管理员
        viewer        // 查看者
    }
}