package com.campus.water.entity.po;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "user") // 对应数据库user表（学生用户）
public class UserPO {
    @Id
    // 修正：String类型主键不支持IDENTITY自增（MySQL自增主键为Long），改为手动赋值/UUID
    // 若需自增，建议将studentId改为Long类型，此处保留String并移除IDENTITY
    private String studentId; // 学生ID（主键，学号）

    @Column(unique = true, nullable = false)
    private String username; // 登录用户名/学生姓名

    @Column(nullable = false)
    private String password; // MD5加密后的密码

    private String phone; // 联系电话
    private String studentNo; // 学号（若studentId已用学号，可删除此字段，避免冗余）
    private String college; // 学院

    // ========== 补充缺失字段 ==========
    @Column(unique = true)
    private String email; // 邮箱（适配Repository的findByEmail/existsByEmail）

    // ========== 补充缺失枚举 ==========
    // 用户状态枚举（适配Repository的findByStatus/findByUsernameContainingAndStatus）
    public enum UserStatus {
        ACTIVE,    // 活跃
        INACTIVE,  // 未激活
        LOCKED     // 锁定
    }

    // 状态字段（映射为字符串存储，适配枚举）
    @Enumerated(EnumType.STRING)
    private UserStatus status;
}