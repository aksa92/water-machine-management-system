package com.campus.water.repair.controller;

import com.campus.water.repair.entity.WorkOrderVO;
import com.campus.water.repair.service.WorkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工单接口（供维修APP前端调用，适配“抢单/处理工单、跟踪状态”需求）
 */
@RestController
@RequestMapping("/api/repair/workOrder")
public class WorkOrderController {
    @Autowired
    private WorkOrderService workOrderService;

    // 1. 获取本辖区待抢单工单（需求“抢单”功能）
    @GetMapping("/pending/{repairmanId}")
    public List<WorkOrderVO> getPendingWorkOrder(@PathVariable String repairmanId) {
        String areaId = workOrderService.getRepairmanArea(repairmanId); // 获取维修员所属片区
        return workOrderService.getPendingWorkOrderByArea(areaId);
    }

    // 2. 抢单（需求核心功能，需校验辖区权限）
    @PostMapping("/grab")
    public String grabWorkOrder(
            @RequestParam String repairmanId,
            @RequestParam String orderId
    ) {
        return workOrderService.grabWorkOrder(repairmanId, orderId) ?
                "抢单成功，请及时处理" : "工单已被抢，或您无权限抢此工单";
    }

    // 3. 提交工单处理结果（需求“处理工单并提交”功能）
    @PostMapping("/complete")
    public String completeWorkOrder(
            @RequestParam String orderId,
            @RequestParam String repairmanId,
            @RequestParam String dealNote, // 处理备注（如“已更换滤芯”）
            @RequestParam String imgUrl     // 现场照片URL（可选）
    ) {
        workOrderService.completeWorkOrder(orderId, repairmanId, dealNote, imgUrl);
        return "工单处理完成，已提交审核";
    }

    // 4. 查看个人处理中的工单（需求“跟踪工单状态”功能）
    @GetMapping("/processing/{repairmanId}")
    public List<WorkOrderVO> getProcessingWorkOrder(@PathVariable String repairmanId) {
        return workOrderService.getProcessingWorkOrderByRepairman(repairmanId);
    }
}