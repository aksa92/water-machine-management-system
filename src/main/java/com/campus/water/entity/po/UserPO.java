package com.campus.water.entity.po;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "user") // 对应数据库user表（学生用户）
public class UserPO {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String studentId; // 学生ID（主键）

    @Column(unique = true, nullable = false)
    private String username; // 登录用户名

    @Column(nullable = false)
    private String password; // MD5加密后的密码

    private String phone; // 联系电话
    private String studentNo; // 学号
    private String college; // 学院
}