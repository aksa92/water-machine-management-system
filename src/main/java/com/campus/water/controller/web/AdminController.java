package com.campus.water.controller.web;

import com.campus.water.entity.Admin;
import com.campus.water.service.AdminService;
import com.campus.water.util.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/web/admin")
@RequiredArgsConstructor
@Tag(name = "管理员管理接口", description = "Web管理端管理员操作接口")
public class AdminController {

    private final AdminService adminService;

    /**
     * 获取管理员列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')")
    @Operation(summary = "获取管理员列表")
    public ResultVO<List<Admin>> getAdminList(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Admin.AdminRole role
    ) {
        List<Admin> adminList = adminService.getAdminList(name, role);
        return ResultVO.success(adminList);
    }

    /**
     * 按ID查询管理员
     */
    @GetMapping("/{adminId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')")
    @Operation(summary = "按ID查询单个管理员")
    public ResultVO<Admin> getAdminById(@PathVariable String adminId) {
        Admin admin = adminService.getAdminById(adminId)
                .orElseThrow(() -> new RuntimeException("管理员不存在，ID：" + adminId));
        return ResultVO.success(admin);
    }

    /**
     * 获取可分配区域管理员
     */
    @GetMapping("/available-area-admins")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')")
    @Operation(summary = "获取可分配校区的区域管理员")
    public ResultVO<List<Admin>> getAvailableAreaAdmins() {
        List<Admin> availableAdmins = adminService.getAvailableAreaAdmins();
        return ResultVO.success(availableAdmins);
    }

    /**
     * 按区域查询管理员
     */
    @GetMapping("/by-area/{areaId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "按区域查询管理员")
    public ResultVO<List<Admin>> getAdminsByArea(@PathVariable String areaId) {
        List<Admin> admins = adminService.getAdminsByAreaId(areaId);
        return ResultVO.success(admins);
    }

    /**
     * 获取所有角色
     */
    @GetMapping("/roles")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')")
    @Operation(summary = "获取管理员角色列表")
    public ResultVO<Admin.AdminRole[]> getAllRoles() {
        return ResultVO.success(adminService.getAllRoles());
    }

    /**
     * 保存管理员
     */
    @PostMapping("/save")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "保存管理员")
    public ResultVO<Admin> saveAdmin(@RequestBody Admin admin) {
        Admin savedAdmin = adminService.saveAdmin(admin);
        return ResultVO.success(savedAdmin);
    }

    /**
     * 删除管理员
     */
    @DeleteMapping("/{adminId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "删除管理员")
    public ResultVO<Void> deleteAdmin(@PathVariable String adminId) {
        adminService.deleteAdmin(adminId);
        return ResultVO.success();
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    @Operation(summary = "管理员登录")
    public ResultVO<Admin> login(
            @RequestParam String adminName,
            @RequestParam String password
    ) {
        Admin admin = adminService.login(adminName, password)
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));
        return ResultVO.success(admin);
    }

    /**
     * 修改个人信息
     */
    @PostMapping("/profile/update")
    @PreAuthorize("isAuthenticated()")
    public ResultVO<Admin> updateProfile(
            @RequestBody Admin profile,
            Authentication authentication
    ) {
        // 拿到当前登录的用户
        String username = authentication.getName();
        Admin currentAdmin = adminService.getAdminByName(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        // 强制把当前登录人的 ID 塞进去 → 只能改自己！
        profile.setAdminId(currentAdmin.getAdminId());

        // 调用 service
        Admin updatedAdmin = adminService.updateProfile(profile);

        return ResultVO.success(updatedAdmin, "更新成功");
    }
    /**
     * 获取当前登录用户
     */
    @GetMapping("/current")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "获取当前登录管理员信息")
    public ResultVO<Admin> getCurrentAdmin(Authentication authentication) {
        String username = authentication.getName();
        Admin admin = adminService.getAdminByName(username)
                .orElseThrow(() -> new RuntimeException("用户信息不存在"));
        return ResultVO.success(admin);
    }

    /**
     * 修改密码
     */
    @PostMapping("/password/update")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "修改密码")
    public ResultVO<Void> updatePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            Authentication authentication
    ) {
        String username = authentication.getName();
        adminService.updatePassword(username, oldPassword, newPassword);
        return ResultVO.success(null, "密码修改成功");
    }
}