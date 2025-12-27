package com.campus.water.controller.web;

import com.campus.water.entity.Admin;
import com.campus.water.service.AdminService;
import com.campus.water.util.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
     * 新增：获取指定区域的管理员列表
     */
    @GetMapping("/by-area/{areaId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")  // 只有超级管理员可以查看
    @Operation(summary = "按区域查询管理员", description = "查询指定区域下的所有管理员")
    public ResponseEntity<ResultVO<List<Admin>>> getAdminsByArea(
            @PathVariable String areaId
    ) {
        try {
            List<Admin> admins = adminService.getAdminsByAreaId(areaId);
            return ResponseEntity.ok(ResultVO.success(admins));
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
     * 重写保存接口的注释，明确区域关联说明
     */
    @PostMapping("/save")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "保存管理员", description = "新增/编辑管理员，区域管理员必须指定areaId")
    public ResponseEntity<ResultVO<Admin>> saveAdmin(@RequestBody Admin admin) {
        // 实现保持不变
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

    /**
     * 管理员个人信息修改
     * 允许当前登录用户修改自己的基本信息（不含角色/区域等敏感字段）
     */
    @PostMapping("/profile/update")
    @PreAuthorize("isAuthenticated()") // 只要登录即可访问
    @Operation(summary = "修改个人信息", description = "当前登录管理员修改自己的基本信息（不含角色）")
    public ResponseEntity<ResultVO<Admin>> updateProfile(
            @RequestBody Admin profile,
            Authentication authentication) {
        try {
            // 1. 获取当前登录用户名
            String currentUsername = authentication.getName();

            // 2. 验证身份一致性（当前用户只能修改自己的信息）
            Admin currentAdmin = adminService.getAdminByName(currentUsername)
                    .orElseThrow(() -> new RuntimeException("当前用户信息不存在"));

            if (!currentAdmin.getAdminId().equals(profile.getAdminId())) {
                throw new RuntimeException("无权修改其他管理员信息");
            }

            // 3. 过滤敏感字段（不允许修改角色和区域ID）
            profile.setRole(currentAdmin.getRole());
            profile.setAreaId(currentAdmin.getAreaId());

            // 4. 调用服务层更新
            Admin updatedAdmin = adminService.updateProfile(profile);
            return ResponseEntity.ok(ResultVO.success(updatedAdmin, "个人信息更新成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "更新失败：" + e.getMessage()));
        }
    }

}