package com.campus.water.entity.po;

import lombok.Data;
import jakarta.persistence.*;

@Data
@Entity
@Table(name = "repairer_auth_backup") // 对应数据库表
public class RepairerAuthPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 主键自增（Long类型）
    private Long id; // 主键ID（对应Repository的Long主键类型）

    @Column(unique = true, nullable = false)
    private String repairmanId; // 维修人员唯一标识（业务ID）

    @Column(unique = true, nullable = false)
    private String username; // 登录用户名

    @Column(nullable = false)
    private String password; // MD5加密密码

    // 账号状态枚举（解决字段映射冲突）
    public enum AccountStatus {
        active, inactive, locked
    }

    // 明确指定数据库字段名，与实体类字段形成映射，解决冲突
    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", length = 50, nullable = false)
    private AccountStatus accountStatus;

    // 补充其他业务字段
    private String phone; // 联系电话
    private String areaId; // 负责区域ID
    private String name; // 维修人员姓名
}