package com.campus.water.entity.po;

import lombok.Data;
import jakarta.persistence.*;

/**
 * 管理员PO类（数据库映射对象）
 * 对应数据库admin表，已与Admin实体类统一映射规则
 */
@Data
@Entity
@Table(name = "admin") // 对应数据库admin表
public class AdminPO {
    @Id
    @Column(name = "admin_id", length = 50) // 明确映射数据库列admin_id，与Admin类保持一致
    private String adminId; // 移除自增注解，因String类型不支持IDENTITY自增

    @Column(name = "admin_name", length = 50, unique = true, nullable = false)
    private String username; // 统一映射到数据库列admin_name，与Admin类的adminName保持一致

    @Column(name = "password", length = 200, nullable = false)
    private String password; // MD5加密后的密码，与Admin类字段长度保持一致

    @Column(name = "phone", length = 20)
    private String phone; // 联系电话，统一长度约束

    // 管理员角色枚举（与Admin类的AdminRole取值和大小写保持一致）
    public enum AdminRole {
        super_admin, area_admin, viewer
    }

    // 管理员状态枚举（与Admin类的AdminStatus取值和大小写保持一致）
    public enum AdminStatus {
        active, inactive
    }

    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 50, nullable = false)
    private AdminRole role; // 默认为空，可在业务层设置默认值

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private AdminStatus status; // 默认为空，可在业务层设置默认值
}