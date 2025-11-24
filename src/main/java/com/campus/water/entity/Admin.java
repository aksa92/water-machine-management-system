package com.campus.water.entity;

import lombok.Data;
import javax.persistence.*;
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

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 50)
    private AdminRole role = AdminRole.area_admin;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private AdminStatus status = AdminStatus.active;

    @Column(name = "created_time")
    private LocalDateTime createdTime = LocalDateTime.now();

    @Column(name = "updated_time")
    private LocalDateTime updatedTime = LocalDateTime.now();

    public enum AdminRole {
        super_admin, area_admin, viewer
    }

    public enum AdminStatus {
        active, inactive
    }
}