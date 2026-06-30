package com.campus.water.controller;

import com.campus.water.entity.WorkOrder;
import com.campus.water.service.WorkOrderService;
import com.campus.water.util.ResultVO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/work-orders")
public class WorkOrderController {

    private final WorkOrderService workOrderService;

    public WorkOrderController(WorkOrderService workOrderService) {
        this.workOrderService = workOrderService;
    }

    @PostMapping("/grab")
    @PreAuthorize("hasAnyRole('REPAIRMAN', 'SUPER_ADMIN', 'AREA_ADMIN')")
    public ResultVO<Boolean> grabOrder(
            @RequestParam String orderId,
            @RequestParam String repairmanId) {
        boolean result = workOrderService.grabOrder(orderId, repairmanId);
        return result ? ResultVO.success(true, "抢单成功")
                : ResultVO.error(400, "抢单失败，工单可能已被抢走或状态异常");
    }

    @PostMapping("/reject")
    @PreAuthorize("hasAnyRole('REPAIRMAN', 'SUPER_ADMIN', 'AREA_ADMIN')")
    public ResultVO<Boolean> rejectOrder(
            @RequestParam String orderId,
            @RequestParam String repairmanId,
            @RequestParam String reason) {
        boolean result = workOrderService.rejectOrder(orderId, repairmanId, reason);
        return result ? ResultVO.success(true, "拒单成功")
                : ResultVO.error(400, "拒单失败，工单状态异常");
    }

    @PostMapping("/submit")
    @PreAuthorize("hasAnyRole('REPAIRMAN', 'SUPER_ADMIN', 'AREA_ADMIN')")
    public ResultVO<Boolean> submitRepairResult(
            @RequestParam String orderId,
            @RequestParam String repairmanId,
            @RequestParam String dealNote,
            @RequestParam(required = false) String imgUrl) {
        boolean result = workOrderService.submitRepairResult(orderId, repairmanId, dealNote, imgUrl);
        return result ? ResultVO.success(true, "维修结果提交成功，等待审核")
                : ResultVO.error(400, "提交失败，工单状态异常");
    }

    @Data
    public static class ReviewOrderRequest {
        private String orderId;
        private boolean approved;
    }

    @PostMapping("/review")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')")
    public ResultVO<Boolean> reviewOrder(@RequestBody ReviewOrderRequest request) {
        boolean result = workOrderService.reviewOrder(request.getOrderId(), request.isApproved());
        return result ? ResultVO.success(true, request.isApproved() ? "审核通过" : "审核不通过")
                : ResultVO.error(400, "审核失败，工单状态异常");
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('REPAIRMAN','SUPER_ADMIN', 'AREA_ADMIN')")
    public ResultVO<List<WorkOrder>> getAvailableOrders(
            @RequestParam(required = false) String areaId,
            Authentication authentication) {
        boolean isRepairman = authentication.getAuthorities().contains(
                new SimpleGrantedAuthority("ROLE_REPAIRMAN"));
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_SUPER_ADMIN")
                        || auth.getAuthority().equals("ROLE_AREA_ADMIN"));

        List<WorkOrder> orders;
        if (isRepairman) {
            if (areaId == null || areaId.trim().isEmpty()) {
                return ResultVO.error(400, "维修人员查询可抢工单必须传入区域ID");
            }
            orders = workOrderService.getAvailableOrders(areaId);
        } else if (isAdmin) {
            orders = workOrderService.getAvailableOrders(null);
        } else {
            return ResultVO.error(403, "无权限访问");
        }
        return ResultVO.success(orders);
    }

    @GetMapping("/by-status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')")
    public ResultVO<List<WorkOrder>> getOrdersByStatus(
            @RequestParam WorkOrder.OrderStatus status,
            @RequestParam(required = false) String areaId) {
        List<WorkOrder> orders;
        if (areaId == null || areaId.trim().isEmpty()) {
            orders = workOrderService.getOrdersByStatus(status);
        } else {
            orders = workOrderService.getOrdersByAreaAndStatus(areaId, status);
        }
        return ResultVO.success(orders);
    }

    @GetMapping("/by-time-range")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')")
    public ResultVO<List<WorkOrder>> getOrdersByTimeRange(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) WorkOrder.OrderStatus status,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        List<WorkOrder> orders = workOrderService.getOrdersByConditions(areaId, status, startTime, endTime);
        return ResultVO.success(orders);
    }

    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('REPAIRMAN', 'SUPER_ADMIN', 'AREA_ADMIN')")
    public ResultVO<List<WorkOrder>> getMyOrders(@RequestParam String repairmanId) {
        List<WorkOrder> orders = workOrderService.getMyOrders(repairmanId);
        return ResultVO.success(orders);
    }

    @Data
    public static class AssignOrderRequest {
        private String orderId;
        private String repairmanId;
    }

    @PostMapping("/assign")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')")
    public ResultVO<Boolean> assignOrderByAdmin(@RequestBody AssignOrderRequest request) {
        if (request.getOrderId() == null || request.getOrderId().trim().isEmpty()) {
            return ResultVO.error(400, "工单ID不能为空");
        }
        if (request.getRepairmanId() == null || request.getRepairmanId().trim().isEmpty()) {
            return ResultVO.error(400, "维修人员ID不能为空");
        }
        boolean result = workOrderService.assignOrderByAdmin(request.getOrderId(), request.getRepairmanId());
        return result ? ResultVO.success(true, "派单成功")
                : ResultVO.error(400, "派单失败，工单或维修人员状态异常");
    }

    @GetMapping("/{orderId}")
    @PreAuthorize("hasAnyRole('REPAIRMAN', 'SUPER_ADMIN', 'AREA_ADMIN')")
    public ResultVO<WorkOrder> getOrderDetail(@PathVariable String orderId) {
        WorkOrder order = workOrderService.getOrderDetail(orderId);
        return ResultVO.success(order);
    }
}
