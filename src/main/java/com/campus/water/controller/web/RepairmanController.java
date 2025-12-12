package com.campus.water.controller.web;

import com.campus.water.entity.Repairman;
import com.campus.water.service.RepairmanService;
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
@RequestMapping("/api/web/repairman")
@RequiredArgsConstructor
@Tag(name = "维修人员管理接口", description = "Web管理端维修人员列表查询接口")
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
}