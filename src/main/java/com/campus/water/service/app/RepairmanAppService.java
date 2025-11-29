package com.campus.water.service.app;

import com.campus.water.entity.WorkOrder;
import com.campus.water.controller.WorkOrderController;
import com.campus.water.util.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class RepairmanAppService {

    @Autowired
    private WorkOrderController workOrderController;

    // 获取可抢工单列表
    public ResultVO<List<WorkOrder>> getAvailableOrders(String areaId) {
        try {
            List<WorkOrder> orders = workOrderController.getAvailableOrders(areaId);
            return ResultVO.success(orders);
        } catch (Exception e) {
            return ResultVO.error(500, "获取工单列表失败: " + e.getMessage());
        }
    }

    // 抢单
    public ResultVO<Boolean> grabOrder(Map<String, String> request) {
        try {
            String orderId = request.get("orderId");
            String repairmanId = request.get("repairmanId");
            boolean result = workOrderController.grabOrder(orderId, repairmanId);
            if (result) {
                return ResultVO.success(true, "抢单成功");
            } else {
                return ResultVO.error(400, "抢单失败，工单可能已被其他人抢走");
            }
        } catch (Exception e) {
            return ResultVO.error(500, "抢单失败: " + e.getMessage());
        }
    }

    // 拒单
    public ResultVO<Boolean> rejectOrder(Map<String, String> request) {
        try {
            String orderId = request.get("orderId");
            String repairmanId = request.get("repairmanId");
            String reason = request.get("reason");
            boolean result = workOrderController.rejectOrder(orderId, repairmanId, reason);
            if (result) {
                return ResultVO.success(true, "拒单成功");
            } else {
                return ResultVO.error(400, "拒单失败");
            }
        } catch (Exception e) {
            return ResultVO.error(500, "拒单失败: " + e.getMessage());
        }
    }

    // 提交维修结果
    public ResultVO<Boolean> submitRepairResult(Map<String, String> request) {
        try {
            String orderId = request.get("orderId");
            String repairmanId = request.get("repairmanId");
            String dealNote = request.get("dealNote");
            String imgUrl = request.get("imgUrl");

            boolean result = workOrderController.submitRepairResult(orderId, repairmanId, dealNote, imgUrl);
            if (result) {
                return ResultVO.success(true, "维修结果提交成功");
            } else {
                return ResultVO.error(400, "维修结果提交失败");
            }
        } catch (Exception e) {
            return ResultVO.error(500, "提交失败: " + e.getMessage());
        }
    }

    // 获取我的工单
    public ResultVO<List<WorkOrder>> getMyOrders(String repairmanId) {
        try {
            List<WorkOrder> orders = workOrderController.getMyOrders(repairmanId);
            return ResultVO.success(orders);
        } catch (Exception e) {
            return ResultVO.error(500, "获取工单失败: " + e.getMessage());
        }
    }
}