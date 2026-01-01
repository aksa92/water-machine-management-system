package main.java.com.campus.water.controller.web;

import main.java.com.campus.water.entity.Repairman;
import main.java.com.campus.water.service.RepairmanService;
import main.java.com.campus.water.util.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/web/repairman")
@RequiredArgsConstructor
@Tag(name = "维修人员管理接口", description = "Web管理端维修人员列表查询、新增、修改、删除接口")
public class RepairmanController {

    private final RepairmanService repairmanService;

    /**
     * 获取维修人员列表（支持多条件筛选）
     * @param name 姓名模糊查询（可选）
     * @param areaId 区域ID筛选（可选）
     * @param status 状态筛选（可选，值：idle/busy/vacation）
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')") // 仅管理员可访问
    @Operation(summary = "获取维修人员列表", description = "支持按姓名、区域和状态筛选维修人员")
    public ResponseEntity<ResultVO<List<Repairman>>> getRepairmanList(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String status
    ) {
        try {
            // 转换状态参数为枚举类型
            Repairman.RepairmanStatus repairmanStatus = status != null
                    ? Repairman.RepairmanStatus.valueOf(status)
                    : null;

            // 调用服务层查询
            List<Repairman> repairmanList = repairmanService.getRepairmanList(name, areaId, repairmanStatus);
            return ResponseEntity.ok(ResultVO.success(repairmanList));
        } catch (IllegalArgumentException e) {
            // 处理枚举参数错误
            return ResponseEntity.ok(ResultVO.error(400, "无效的状态参数: " + e.getMessage()));
        } catch (Exception e) {
            // 处理其他异常
            return ResponseEntity.ok(ResultVO.error(500, "查询维修人员列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取所有维修人员状态枚举
     */
    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')")
    @Operation(summary = "获取维修人员状态列表", description = "返回所有可选状态（idle/busy/vacation）")
    public ResponseEntity<ResultVO<Repairman.RepairmanStatus[]>> getAllStatus() {
        try {
            Repairman.RepairmanStatus[] statuses = repairmanService.getAllStatus();
            return ResponseEntity.ok(ResultVO.success(statuses));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "获取状态列表失败：" + e.getMessage()));
        }
    }

    /**
     * 新增/编辑维修人员
     */
    @PostMapping("/save")
    @PreAuthorize("hasRole('SUPER_ADMIN')") // 仅超级管理员可操作
    @Operation(summary = "保存维修人员信息", description = "新增或编辑维修人员信息，ID存在则更新，不存在则新增")
    public ResponseEntity<ResultVO<Repairman>> saveRepairman(@Valid @RequestBody Repairman repairman) {
        try {
            Repairman savedRepairman = repairmanService.saveRepairman(repairman);
            return ResponseEntity.ok(ResultVO.success(savedRepairman,
                    repairman.getRepairmanId() == null ? "维修人员新增成功" : "维修人员更新成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "保存维修人员失败: " + e.getMessage()));
        }
    }

    /**
     * 删除维修人员
     */
    @DeleteMapping("/{repairmanId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')") // 仅超级管理员可操作
    @Operation(summary = "删除维修人员", description = "根据维修人员ID删除指定维修人员")
    public ResponseEntity<ResultVO<Void>> deleteRepairman(@PathVariable String repairmanId) {
        try {
            repairmanService.deleteRepairman(repairmanId);
            return ResponseEntity.ok(ResultVO.success(null, "维修人员删除成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "删除维修人员失败: " + e.getMessage()));
        }
    }

    /**
     * 根据ID查询维修人员详情
     */
    @GetMapping("/{repairmanId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')")
    @Operation(summary = "获取维修人员详情", description = "根据ID查询维修人员详细信息")
    public ResponseEntity<ResultVO<Repairman>> getRepairmanById(@PathVariable String repairmanId) {
        try {
            Optional<Repairman> repairman = repairmanService.getRepairmanById(repairmanId);
            return repairman.map(value -> ResponseEntity.ok(ResultVO.success(value)))
                    .orElseGet(() -> ResponseEntity.ok(ResultVO.error(404, "维修人员不存在")));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "查询维修人员详情失败: " + e.getMessage()));
        }
    }

    /**
     * 根据片区ID查询维修人员
     * 专门用于仅按片区筛选维修人员的场景，返回指定片区内的所有维修人员
     */
    @GetMapping("/by-area/{areaId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')") // 保持与其他查询接口一致的权限控制
    @Operation(summary = "根据片区查询维修人员", description = "根据指定的片区ID，查询该片区内的所有维修人员")
    public ResponseEntity<ResultVO<List<Repairman>>> getRepairmenByArea(@PathVariable String areaId) {
        try {
            // 调用现有服务层方法，仅传入areaId，其他参数为null（表示不筛选）
            List<Repairman> repairmen = repairmanService.getRepairmanList(null, areaId, null);
            return ResponseEntity.ok(ResultVO.success(repairmen));
        } catch (Exception e) {
            // 统一异常处理，与其他接口保持一致
            return ResponseEntity.ok(ResultVO.error(500, "根据片区查询维修人员失败: " + e.getMessage()));
        }
    }
}