package com.campus.water.controller.web;

import com.campus.water.entity.Admin;
import com.campus.water.service.AdminService;
import com.campus.water.util.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/web/admin")
@RequiredArgsConstructor
@Tag(name = "管理员管理接口", description = "Web管理端管理员操作接口")
public class AdminController {

    private final AdminService adminService;

    /**
     * 获取管理员列表（支持姓名/角色筛选）
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')") // 超级/区域管理员可查看
    @Operation(summary = "获取管理员列表", description = "支持按姓名模糊搜索、按角色筛选")
    public ResponseEntity<ResultVO<List<Admin>>> getAdminList(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Admin.AdminRole role // 角色筛选参数
    ) {
        try {
            List<Admin> adminList = adminService.getAdminList(name, role);
            return ResponseEntity.ok(ResultVO.success(adminList));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "查询失败：" + e.getMessage()));
        }
    }

    /**
     * 获取所有管理员角色枚举
     */
    @GetMapping("/roles")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')")
    @Operation(summary = "获取管理员角色列表", description = "返回所有可选角色（super_admin/area_admin/viewer）")
    public ResponseEntity<ResultVO<Admin.AdminRole[]>> getAllRoles() {
        try {
            Admin.AdminRole[] roles = adminService.getAllRoles();
            return ResponseEntity.ok(ResultVO.success(roles));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "获取角色列表失败：" + e.getMessage()));
        }
    }

    /**
     * 新增/编辑管理员
     */
    @PostMapping("/save")
    @PreAuthorize("hasRole('SUPER_ADMIN')") // 仅超级管理员可新增/编辑
    @Operation(summary = "保存管理员", description = "新增/编辑管理员，支持指定角色")
    public ResponseEntity<ResultVO<Admin>> saveAdmin(@RequestBody Admin admin) {
        try {
            Admin savedAdmin = adminService.saveAdmin(admin);
            return ResponseEntity.ok(ResultVO.success(savedAdmin));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "保存失败：" + e.getMessage()));
        }
    }

    /**
     * 删除管理员
     */
    @DeleteMapping("/{adminId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')") // 仅超级管理员可删除
    @Operation(summary = "删除管理员", description = "按ID删除管理员")
    public ResponseEntity<ResultVO<Void>> deleteAdmin(@PathVariable String adminId) {
        try {
            adminService.deleteAdmin(adminId);
            return ResponseEntity.ok(ResultVO.success(null));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "删除失败：" + e.getMessage()));
        }
    }

    /**
     * 管理员登录
     */
    @PostMapping("/login")
    @Operation(summary = "管理员登录", description = "用户名+密码验证，返回管理员信息（含角色）")
    public ResponseEntity<ResultVO<Admin>> login(
            @RequestParam String adminName,
            @RequestParam String password
    ) {
        Optional<Admin> admin = adminService.login(adminName, password);
        if (admin.isPresent()) {
            return ResponseEntity.ok(ResultVO.success(admin.get()));
        } else {
            return ResponseEntity.ok(ResultVO.error(401, "用户名或密码错误"));
        }
    }
}