package com.campus.water.entity.po;

import lombok.Data;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user") // 对应数据库user表（学生用户）
public class UserPO {
    @Id
    @Column(name = "student_id", length = 50) // 与User类保持字段名一致
    private String studentId; // 学生ID（主键，学号）

    @Column(name = "student_name", unique = true, nullable = false, length = 50)
    // 修正：与User类的studentName字段对应，同时保留唯一约束
    private String username; // 登录名（对应User类的studentName）

    @Column(name = "password", nullable = false, length = 200)
    private String password; // MD5加密后的密码（与User类字段长度保持一致）

    @Column(name = "phone", length = 20)
    private String phone; // 联系电话

    // 移除冗余的studentNo字段（与studentId重复）
    private String college; // 学院

    @Column(name = "email", unique = true, length = 100)
    private String email; // 邮箱（与User类字段长度保持一致）

    // 统一状态枚举命名风格与User类保持一致（小写开头）
    public enum UserStatus {
        active,    // 活跃
        inactive,  // 未激活
        locked     // 锁定
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private UserStatus status = UserStatus.active; // 增加默认值，与User类保持一致

    // 补充时间字段，与User类保持表结构一致
    @Column(name = "create_time")
    private LocalDateTime createTime = LocalDateTime.now();

    @Column(name = "updated_time")
    private LocalDateTime updatedTime = LocalDateTime.now();
}