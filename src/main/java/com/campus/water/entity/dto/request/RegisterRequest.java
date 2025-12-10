package com.campus.water.entity.dto.request;

import com.campus.water.entity.Admin;
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
    private String studentId;
    private String studentName;
    private String phone;

    // 管理员特有字段
    private String adminId;
    private String role; // 新增：管理员角色（super_admin/area_admin/viewer）

    // 维修人员特有字段（新增基本信息字段）
    private String repairmanId;
    private String repairmanName; // 维修人员姓名
    private String areaId; // 负责区域ID
    private String skills; // 技能描述
}