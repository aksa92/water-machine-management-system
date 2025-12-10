package com.campus.water.controller;

import com.campus.water.entity.WorkOrder;
import com.campus.water.service.WorkOrderService;
import com.campus.water.util.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    @Autowired
    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    // 抢单功能 - 维修人员和管理员可访问
    @PostMapping("/grab")
    @PreAuthorize("hasAnyRole('REPAIRMAN', 'SUPER_ADMIN', 'AREA_ADMIN')")
    public ResultVO<Boolean> grabOrder(
            @RequestParam String orderId,
            @RequestParam String repairmanId) {
        try {
            boolean result = workOrderService.grabOrder(orderId, repairmanId);
            return result ? ResultVO.success(true, "抢单成功")
                    : ResultVO.error(400, "抢单失败，工单可能已被抢走或状态异常");
        } catch (Exception e) {
            return ResultVO.error(500, "抢单失败：" + e.getMessage());
        }
    }

    // 拒单功能 - 维修人员和管理员可访问
    @PostMapping("/reject")
    @PreAuthorize("hasAnyRole('REPAIRMAN', 'SUPER_ADMIN', 'AREA_ADMIN')")
    public ResultVO<Boolean> rejectOrder(
            @RequestParam String orderId,
            @RequestParam String repairmanId,
            @RequestParam String reason) {
        try {
            boolean result = workOrderService.rejectOrder(orderId, repairmanId, reason);
            return result ? ResultVO.success(true, "拒单成功")
                    : ResultVO.error(400, "拒单失败，工单状态异常");
        } catch (Exception e) {
            return ResultVO.error(500, "拒单失败：" + e.getMessage());
        }
    }

    // 提交维修结果 - 维修人员和管理员可访问
    @PostMapping("/submit")
    @PreAuthorize("hasAnyRole('REPAIRMAN', 'SUPER_ADMIN', 'AREA_ADMIN')")
    public ResultVO<Boolean> submitRepairResult(
            @RequestParam String orderId,
            @RequestParam String repairmanId,
            @RequestParam String dealNote,
            @RequestParam(required = false) String imgUrl) {
        try {
            boolean result = workOrderService.submitRepairResult(orderId, repairmanId, dealNote, imgUrl);
            return result ? ResultVO.success(true, "维修结果提交成功，等待审核")
                    : ResultVO.error(400, "提交失败，工单状态异常");
        } catch (Exception e) {
            return ResultVO.error(500, "提交失败：" + e.getMessage());
        }
    }

    // 新增：审核工单接口（管理员专用）
    @PostMapping("/review")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')")
    public ResultVO<Boolean> reviewOrder(
            @RequestParam String orderId,
            @RequestParam boolean approved) {
        try {
            boolean result = workOrderService.reviewOrder(orderId, approved);
            return result ? ResultVO.success(true, approved ? "审核通过" : "审核不通过")
                    : ResultVO.error(400, "审核失败，工单状态异常");
        } catch (Exception e) {
            return ResultVO.error(500, "审核失败：" + e.getMessage());
        }
    }

    // 获取可抢工单列表 - 维修人员和管理员可访问
    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('REPAIRMAN','SUPER_ADMIN', 'AREA_ADMIN')")
    public ResultVO<List<WorkOrder>> getAvailableOrders(
            @RequestParam(required = false) String areaId, // 改为非必填
            Authentication authentication) { // 获取当前登录用户的认证信息
        try {
            // 1. 判断当前用户角色
            boolean isRepairman = authentication.getAuthorities().contains(
                    new SimpleGrantedAuthority("ROLE_REPAIRMAN")
            );
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(auth -> auth.getAuthority().equals("ROLE_SUPER_ADMIN")
                            || auth.getAuthority().equals("ROLE_AREA_ADMIN"));

            // 2. 角色逻辑校验
            List<WorkOrder> orders;
            if (isRepairman) {
                // 维修人员：必须传areaId，否则抛异常
                if (areaId == null || areaId.trim().isEmpty()) {
                    return ResultVO.error(400, "维修人员查询可抢工单必须传入区域ID");
                }
                // 维修人员：查指定区域的可抢工单
                orders = workOrderService.getAvailableOrders(areaId);
            } else if (isAdmin) {
                // 管理员：无需传areaId，查所有区域的可抢工单
                // 给service层传null，让service层识别为"查所有"
                orders = workOrderService.getAvailableOrders(null);
            } else {
                // 非授权角色（理论上被@PreAuthorize拦截，不会走到这）
                return ResultVO.error(403, "无权限访问");
            }

            return ResultVO.success(orders);
        } catch (Exception e) {
            return ResultVO.error(500, "获取工单列表失败：" + e.getMessage());
        }
    }

    // 获取维修工自己的工单 - 维修人员和管理员可访问
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('REPAIRMAN', 'SUPER_ADMIN', 'AREA_ADMIN')")
    public ResultVO<List<WorkOrder>> getMyOrders(@RequestParam String repairmanId) {
        try {
            List<WorkOrder> orders = workOrderService.getMyOrders(repairmanId);
            return ResultVO.success(orders);
        } catch (Exception e) {
            return ResultVO.error(500, "获取我的工单失败：" + e.getMessage());
        }
    }

    // 管理员手动派单接口
    @PostMapping("/assign")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')")
    public ResultVO<Boolean> assignOrderByAdmin(
            @RequestParam String orderId,
            @RequestParam String repairmanId) {
        try {
            boolean result = workOrderService.assignOrderByAdmin(orderId, repairmanId);
            return result ? ResultVO.success(true, "派单成功")
                    : ResultVO.error(400, "派单失败，工单或维修人员状态异常");
        } catch (Exception e) {
            return ResultVO.error(500, "派单失败：" + e.getMessage());
        }
    }
}