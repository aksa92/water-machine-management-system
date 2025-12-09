package com.campus.water.controller.web;

import com.campus.water.entity.Admin;
import com.campus.water.service.AdminService;
import com.campus.water.util.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/web/admin")
@RequiredArgsConstructor
@Tag(name = "管理员管理接口", description = "Web管理端管理员列表查询接口")
public class AdminController {

    private final AdminService adminService;

    /**
     * 加载管理员列表（支持筛选）
     * @param role 角色筛选（可选，值：super_admin/area_admin/viewer）
     * @param status 状态筛选（可选，值：active/inactive）
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')") // 仅管理员可访问
    @Operation(summary = "获取管理员列表", description = "支持按角色和状态筛选管理员")
    public ResponseEntity<ResultVO<List<Admin>>> getAdminList(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status
    ) {
        try {
            // 转换参数为枚举类型
            Admin.AdminRole adminRole = role != null ? Admin.AdminRole.valueOf(role) : null;
            Admin.AdminStatus adminStatus = status != null ? Admin.AdminStatus.valueOf(status) : null;

            // 调用服务层查询
            List<Admin> adminList = adminService.getAdminList(adminRole, adminStatus);
            return ResponseEntity.ok(ResultVO.success(adminList));
        } catch (IllegalArgumentException e) {
            // 处理枚举参数错误
            return ResponseEntity.ok(ResultVO.error(400, "无效的角色或状态参数: " + e.getMessage()));
        } catch (Exception e) {
            // 处理其他异常
            return ResponseEntity.ok(ResultVO.error(500, "查询管理员列表失败: " + e.getMessage()));
        }
    }
}