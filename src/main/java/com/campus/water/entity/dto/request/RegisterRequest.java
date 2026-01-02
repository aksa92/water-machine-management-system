package main.java.com.campus.water.entity.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 注册请求DTO，接收前端传递的注册参数
 */
@Data
public class RegisterRequest {
    // 通用字段
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "用户类型不能为空")
    private String userType; // admin/user/repairman

    private String phone;

    // 维修人员特有字段
    private String repairmanId; // 维修人员ID

    @NotBlank(message = "维修人员姓名不能为空")
    private String repairmanName; // 维修人员姓名

    @NotBlank(message = "负责区域ID不能为空")
    private String areaId; // 负责区域ID

    @NotBlank(message = "技能描述不能为空")
    private String skills; // 技能描述

    // 管理员特有字段
    private String adminId;
    private String role;

    // 用户（学生）特有字段
    private String studentId;
    private String studentName;


}