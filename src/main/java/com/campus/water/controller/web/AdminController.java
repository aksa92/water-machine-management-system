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
     * 获取管理员列表（支持按姓名搜索）
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')") // 仅管理员可访问
    @Operation(summary = "获取管理员列表", description = "支持按姓名搜索管理员")
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
            return ResponseEntity.ok(ResultVO.error(500, "查询管理员列表失败: " + e.getMessage()));
        }
    }
}