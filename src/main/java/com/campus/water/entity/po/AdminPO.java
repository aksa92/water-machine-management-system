package com.campus.water.entity.po;

import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "admin") // 对应数据库admin表
public class AdminPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 主键自增（根据数据库调整）
    private String adminId;

    @Column(unique = true, nullable = false)
    private String username; // 登录用户名

    @Column(nullable = false)
    private String password; // MD5加密后的密码

    private String phone; // 联系电话

    // 管理员角色枚举
    public enum AdminRole {
        SUPER_ADMIN, NORMAL_ADMIN
    }

    // 管理员状态枚举
    public enum AdminStatus {
        ACTIVE, INACTIVE
    }

    @Enumerated(EnumType.STRING) // 枚举以字符串形式存储
    private AdminRole role;

    @Enumerated(EnumType.STRING)
    private AdminStatus status;
}