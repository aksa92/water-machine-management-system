package com.campus.water.entity.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
public class RegisterRequest {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Pattern(regexp = "^[a-zA-Z0-9]{6,16}$", message = "密码必须为6-16位字母或数字")
    private String password;

    @NotBlank(message = "用户类型不能为空")
    private String userType; // admin/user/repairer

    // 用户特有字段
    private String studentId; // 学生ID（仅user类型需要）
    private String studentName; // 学生姓名（仅user类型需要）
    private String phone; // 新增：学生/用户手机号（仅user类型需要）

    // 管理员特有字段
    private String adminId; // 管理员ID（仅admin类型需要）
    // 已移除管理员角色区分（根据之前需求）

    // 维修人员特有字段
    private String repairmanId; // 维修人员ID（仅repairer类型需要）
}