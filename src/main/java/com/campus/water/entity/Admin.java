/**
 * 管理员信息实体类
 * 对应表：admin
 * 用于存储系统管理员信息，包括角色、状态、联系方式等
 */
package com.campus.water.entity;

import lombok.Data;
import jakarta.persistence.Column;

import jakarta.persistence.*;
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

    @Column(name = "created_time")
    private LocalDateTime createdTime = LocalDateTime.now();

    @Column(name = "updated_time")
    private LocalDateTime updatedTime = LocalDateTime.now();


}