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
     * 获取管理员列表（支持姓名搜索）
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取管理员列表", description = "支持按姓名模糊搜索，仅返回Admin角色管理员")
    public ResponseEntity<ResultVO<List<Admin>>> getAdminList(
            @RequestParam(required = false) String name
    ) {
        try {
            List<Admin> adminList;
            if (name != null && !name.isEmpty()) {
                adminList = adminService.searchAdminsByName(name);
            } else {
                adminList = adminService.getAdminList();
            }
            return ResponseEntity.ok(ResultVO.success(adminList));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "查询失败：" + e.getMessage()));
        }
    }

    /**
     * 新增/编辑管理员
     */
    @PostMapping("/save")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "保存管理员", description = "新增/编辑管理员，角色强制为Admin")
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @Operation(summary = "管理员登录", description = "用户名+密码验证")
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