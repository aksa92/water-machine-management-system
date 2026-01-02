package main.java.com.campus.water.controller.web;

import main.java.com.campus.water.entity.User;
import main.java.com.campus.water.service.UserService;
import main.java.com.campus.water.util.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*; // 补充Web注解的统一导入

import java.util.List;
import java.util.Optional; // 补充Optional的导入


@RestController
@RequestMapping("/api/web/user")
@RequiredArgsConstructor
@Tag(name = "学生管理接口", description = "Web管理端学生操作接口")
public class UserController {

    private final UserService userService; // 只依赖Service，不直接依赖Repository


    /**
     * 获取学生列表（支持姓名/状态筛选）
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')")
    @Operation(summary = "获取学生列表", description = "支持按姓名模糊搜索、按状态筛选")
    public ResponseEntity<ResultVO<List<User>>> getUserList(
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) User.UserStatus status
    ) {
        try {
            List<User> userList = userService.getUserList(studentName, status);
            return ResponseEntity.ok(ResultVO.success(userList));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "查询失败：" + e.getMessage()));
        }
    }


    /**
     * 获取所有学生状态枚举
     */
    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')")
    @Operation(summary = "获取学生状态列表", description = "返回所有可选状态（active/inactive）")
    public ResponseEntity<ResultVO<User.UserStatus[]>> getAllStatus() {
        try {
            User.UserStatus[] statuses = User.UserStatus.values();
            return ResponseEntity.ok(ResultVO.success(statuses));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "获取状态列表失败：" + e.getMessage()));
        }
    }


    /**
     * 新增/编辑学生（移除直接依赖Repository的逻辑，交给Service处理）
     */
    @PostMapping("/save") // 已补充@PostMapping的导入
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "保存学生信息", description = "新增/编辑学生，支持指定状态")
    public ResponseEntity<ResultVO<User>> saveUser(@RequestBody User user) { // 已补充@RequestBody的导入
        try {
            // 学号唯一性校验移到Service层处理，Controller只调用Service
            User savedUser = userService.saveUser(user);
            return ResponseEntity.ok(ResultVO.success(
                    savedUser,
                    user.getStudentId() == null ? "学生新增成功" : "学生信息更新成功"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "保存失败：" + e.getMessage()));
        }
    }


    /**
     * 删除学生
     */
    @DeleteMapping("/{studentId}") // 已补充@DeleteMapping的导入
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "删除学生", description = "按学号删除学生")
    public ResponseEntity<ResultVO<Void>> deleteUser(@PathVariable String studentId) { // 已补充@PathVariable的导入
        try {
            userService.deleteUser(studentId);
            return ResponseEntity.ok(ResultVO.success(null, "删除成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "删除失败：" + e.getMessage()));
        }
    }


    /**
     * 根据学号查询学生详情
     */
    @GetMapping("/{studentId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')")
    @Operation(summary = "获取学生详情", description = "按学号查询学生详细信息")
    public ResponseEntity<ResultVO<User>> getUserById(@PathVariable String studentId) {
        try {
            Optional<User> user = userService.getUserById(studentId);
            return user.map(value -> ResponseEntity.ok(ResultVO.success(value)))
                    .orElseGet(() -> ResponseEntity.ok(ResultVO.error(404, "学生不存在")));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "查询失败：" + e.getMessage()));
        }
    }
}