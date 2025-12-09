package com.campus.water.controller.web;

import com.campus.water.entity.User;
import com.campus.water.service.UserService;
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
@RequestMapping("/api/web/user")
@RequiredArgsConstructor
@Tag(name = "学生管理接口", description = "Web管理端学生列表查询接口")
public class UserController {

    private final UserService userService;

    /**
     * 加载学生用户列表（支持筛选）
     * @param studentName 学生姓名模糊查询（可选）
     * @param status 状态筛选（可选，值：active/inactive）
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('STUDENT', 'SUPER_ADMIN', 'AREA_ADMIN', 'VIEWER')")// 仅管理员可访问
    @Operation(summary = "获取学生用户列表", description = "支持按姓名和状态筛选学生")
    public ResponseEntity<ResultVO<List<User>>> getUserList(
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) String status
    ) {
        try {
            // 转换状态参数为枚举类型
            User.UserStatus userStatus = status != null ? User.UserStatus.valueOf(status) : null;

            // 调用服务层查询
            List<User> userList = userService.getUserList(studentName, userStatus);
            return ResponseEntity.ok(ResultVO.success(userList));
        } catch (IllegalArgumentException e) {
            // 处理枚举参数错误
            return ResponseEntity.ok(ResultVO.error(400, "无效的状态参数: " + e.getMessage()));
        } catch (Exception e) {
            // 处理其他异常
            return ResponseEntity.ok(ResultVO.error(500, "查询学生列表失败: " + e.getMessage()));
        }
    }
}