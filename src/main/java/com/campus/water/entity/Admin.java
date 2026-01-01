package main.java.com.campus.water.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import org.hibernate.annotations.GenericGenerator;

@Data
@Entity
@Table(name = "admin")
public class Admin {
    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "admin_id", length = 50, nullable = false)
    private String adminId;

    @Column(name = "admin_name", length = 50)
    private String adminName;

    @Column(name = "password", length = 200)
    private String password;

    @Column(name = "phone", length = 20)
    private String phone;

    // 新增：管理员负责的区域ID（区域管理员专用）
    @Column(name = "area_id", length = 36, nullable = true)
    private String areaId;

    // 恢复三个角色枚举
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 50, nullable = false)
    private AdminRole role;

    @Column(name = "created_time")
    private LocalDateTime createdTime = LocalDateTime.now();

    @Column(name = "updated_time")
    private LocalDateTime updatedTime = LocalDateTime.now();

    // 枚举类：恢复super_admin、area_admin、viewer三个角色
    // java/com/campus/water/entity/Admin.java
    public enum AdminRole {
        ROLE_SUPER_ADMIN,  // 超级管理员（原super_admin）
        ROLE_AREA_ADMIN,   // 区域管理员（原area_admin）
        ROLE_VIEWER        // 查看者（原viewer）
    }
}