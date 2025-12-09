package com.campus.water.service.app;

import com.campus.water.entity.WorkOrder;
import com.campus.water.service.WorkOrderService;
import com.campus.water.util.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 维修人员APP端服务层
 * 处理维修人员APP端的工单相关业务逻辑
 */
@Service
public class RepairmanAppService {

    // 核心修改：注入WorkOrderService而非WorkOrderController
    @Autowired
    private WorkOrderService workOrderService;

    /**
     * 获取可抢工单列表
     * @param areaId 区域ID
     * @return 可抢工单列表
     */
    @PreAuthorize("hasAnyRole('REPAIRMAN', 'ADMIN')")
    public ResultVO<List<WorkOrder>> getAvailableOrders(String areaId) {
        try {
            // 参数校验
            if (areaId == null || areaId.trim().isEmpty()) {
                return ResultVO.error(400, "区域ID不能为空");
            }
            List<WorkOrder> orders = workOrderService.getAvailableOrders(areaId);
            return ResultVO.success(orders);
        } catch (Exception e) {
            return ResultVO.error(500, "获取工单列表失败: " + e.getMessage());
        }
    }

    /**
     * 抢单功能
     * @param request 包含orderId和repairmanId的请求参数
     * @return 抢单结果
     */
    @PreAuthorize("hasAnyRole('REPAIRMAN', 'ADMIN')")
    public ResultVO<Boolean> grabOrder(Map<String, String> request) {
        try {
            // 参数校验
            String orderId = request.get("orderId");
            String repairmanId = request.get("repairmanId");
            if (orderId == null || orderId.trim().isEmpty()) {
                return ResultVO.error(400, "工单ID不能为空");
            }
            if (repairmanId == null || repairmanId.trim().isEmpty()) {
                return ResultVO.error(400, "维修人员ID不能为空");
            }

            boolean result = workOrderService.grabOrder(orderId, repairmanId);
            if (result) {
                return ResultVO.success(true, "抢单成功");
            } else {
                return ResultVO.error(400, "抢单失败，工单可能已被其他人抢走或状态异常");
            }
        } catch (Exception e) {
            return ResultVO.error(500, "抢单失败: " + e.getMessage());
        }
    }

    /**
     * 拒单功能
     * @param request 包含orderId、repairmanId、reason的请求参数
     * @return 拒单结果
     */
    @PreAuthorize("hasAnyRole('REPAIRMAN', 'ADMIN')")
    public ResultVO<Boolean> rejectOrder(Map<String, String> request) {
        try {
            // 参数校验
            String orderId = request.get("orderId");
            String repairmanId = request.get("repairmanId");
            String reason = request.get("reason");

            if (orderId == null || orderId.trim().isEmpty()) {
                return ResultVO.error(400, "工单ID不能为空");
            }
            if (repairmanId == null || repairmanId.trim().isEmpty()) {
                return ResultVO.error(400, "维修人员ID不能为空");
            }
            if (reason == null || reason.trim().isEmpty()) {
                return ResultVO.error(400, "拒单原因不能为空");
            }

            boolean result = workOrderService.rejectOrder(orderId, repairmanId, reason);
            if (result) {
                return ResultVO.success(true, "拒单成功");
            } else {
                return ResultVO.error(400, "拒单失败，工单状态异常或无操作权限");
            }
        } catch (Exception e) {
            return ResultVO.error(500, "拒单失败: " + e.getMessage());
        }
    }

    /**
     * 提交维修结果
     * @param request 包含orderId、repairmanId、dealNote、imgUrl的请求参数
     * @return 提交结果
     */
    @PreAuthorize("hasAnyRole('REPAIRMAN', 'ADMIN')")
    public ResultVO<Boolean> submitRepairResult(Map<String, String> request) {
        try {
            // 参数校验
            String orderId = request.get("orderId");
            String repairmanId = request.get("repairmanId");
            String dealNote = request.get("dealNote");
            String imgUrl = request.get("imgUrl"); // 图片URL可选

            if (orderId == null || orderId.trim().isEmpty()) {
                return ResultVO.error(400, "工单ID不能为空");
            }
            if (repairmanId == null || repairmanId.trim().isEmpty()) {
                return ResultVO.error(400, "维修人员ID不能为空");
            }
            if (dealNote == null || dealNote.trim().isEmpty()) {
                return ResultVO.error(400, "维修处理备注不能为空");
            }

            boolean result = workOrderService.submitRepairResult(orderId, repairmanId, dealNote, imgUrl);
            if (result) {
                return ResultVO.success(true, "维修结果提交成功");
            } else {
                return ResultVO.error(400, "维修结果提交失败，工单状态异常或无操作权限");
            }
        } catch (Exception e) {
            return ResultVO.error(500, "提交失败: " + e.getMessage());
        }
    }

    /**
     * 获取我的工单
     * @param repairmanId 维修人员ID
     * @return 维修人员名下的工单列表
     */
    @PreAuthorize("hasAnyRole('REPAIRMAN', 'ADMIN')")
    public ResultVO<List<WorkOrder>> getMyOrders(String repairmanId) {
        try {
            // 参数校验
            if (repairmanId == null || repairmanId.trim().isEmpty()) {
                return ResultVO.error(400, "维修人员ID不能为空");
            }

            List<WorkOrder> orders = workOrderService.getMyOrders(repairmanId);
            return ResultVO.success(orders);
        } catch (Exception e) {
            return ResultVO.error(500, "获取工单失败: " + e.getMessage());
        }
    }
}