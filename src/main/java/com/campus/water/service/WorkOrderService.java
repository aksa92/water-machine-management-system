package com.campus.water.service;

import com.campus.water.entity.WorkOrder;
import java.util.List;

public interface WorkOrderService {
    //按ID获取工单详情
    WorkOrder getOrderDetail(String orderId);

    // 抢单
    boolean grabOrder(String orderId, String repairmanId);

    // 拒单
    boolean rejectOrder(String orderId, String repairmanId, String reason);

    // 提交维修结果
    boolean submitRepairResult(String orderId, String repairmanId, String dealNote, String imgUrl);

    // 获取可抢工单
    List<WorkOrder> getAvailableOrders(String areaId);

    // 获取我的工单
    List<WorkOrder> getMyOrders(String repairmanId);

    // 管理员手动派单（新增方法）
    boolean assignOrderByAdmin(String orderId, String repairmanId);

    // 新增：审核工单方法（与实现类签名一致）
    boolean reviewOrder(String orderId, boolean approved);

    // 按状态查询工单的方法
    List<WorkOrder> getOrdersByStatus(WorkOrder.OrderStatus status);
    List<WorkOrder> getOrdersByAreaAndStatus(String areaId, WorkOrder.OrderStatus status);
}