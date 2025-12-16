package com.campus.water.service;

import com.campus.water.entity.WorkOrder;
import com.campus.water.entity.Repairman;
import com.campus.water.mapper.WorkOrderRepository;
import com.campus.water.mapper.RepairmanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WorkOrderServiceImpl implements WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final RepairmanRepository repairmanRepository;

    @Override
    public WorkOrder getOrderDetail(String orderId) {
        return workOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("工单不存在"));
    }


    /**
     * 维修人员抢单功能
     * 业务规则：仅允许抢"待处理"状态的工单，且维修人员需处于"空闲"状态
     * @param orderId 工单ID
     * @param repairmanId 维修人员ID
     * @return 抢单成功返回true，否则返回false
     */
    @Override
    @Transactional
    public boolean grabOrder(String orderId, String repairmanId) {
        // 查询工单是否存在
        Optional<WorkOrder> orderOpt = workOrderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            WorkOrder order = orderOpt.get();

            // 检查工单状态是否为"待处理"（只有待处理的工单可被抢单）
            if (order.getStatus() == WorkOrder.OrderStatus.pending) {
                // 查询维修人员是否存在且状态为"空闲"
                var repairman = repairmanRepository.findById(repairmanId);
                if (repairman.isPresent() && repairman.get().getStatus() == Repairman.RepairmanStatus.idle) {

                    // 更新工单状态：改为"已抢单"，记录抢单时间和维修人员ID
                    order.setStatus(WorkOrder.OrderStatus.processing);
                    order.setAssignedRepairmanId(repairmanId);
                    order.setGrabbedTime(LocalDateTime.now());
                    workOrderRepository.save(order);

                    // 更新维修人员状态：改为"忙碌"
                    var repairmanEntity = repairman.get();
                    repairmanEntity.setStatus(Repairman.RepairmanStatus.busy);
                    repairmanRepository.save(repairmanEntity);

                    return true; // 抢单成功
                }
            }
        }
        return false; // 抢单失败（工单不存在/状态异常/维修人员不满足条件）
    }

    /**
     * 维修人员拒单功能
     * 业务规则：仅允许拒绝自己已抢单的工单，拒单后工单回到"待处理"状态
     * @param orderId 工单ID
     * @param repairmanId 维修人员ID
     * @param reason 拒单原因
     * @return 拒单成功返回true，否则返回false
     */
    @Override
    @Transactional
    public boolean rejectOrder(String orderId, String repairmanId, String reason) {
        // 查询工单是否存在
        Optional<WorkOrder> orderOpt = workOrderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            WorkOrder order = orderOpt.get();

            // 校验权限：必须是当前工单的已分配维修人员，且工单状态为"已抢单"
            if (order.getAssignedRepairmanId() != null &&
                    order.getAssignedRepairmanId().equals(repairmanId) &&
                    order.getStatus() == WorkOrder.OrderStatus.processing) {

                // 重置工单状态：改为"待处理"，清空维修人员信息和抢单时间
                order.setStatus(WorkOrder.OrderStatus.pending);
                order.setAssignedRepairmanId(null);
                order.setGrabbedTime(null);
                workOrderRepository.save(order);

                // 重置维修人员状态：改为"空闲"
                var repairman = repairmanRepository.findById(repairmanId);
                repairman.ifPresent(rm -> {
                    rm.setStatus(Repairman.RepairmanStatus.idle);
                    repairmanRepository.save(rm);
                });

                return true; // 拒单成功
            }
        }
        return false; // 拒单失败（工单不存在/无权限/状态异常）
    }

    /**
     * 提交维修结果
     * 业务规则：仅允许提交自己负责的"已抢单"或"处理中"状态工单
     * @param orderId 工单ID
     * @param repairmanId 维修人员ID
     * @param dealNote 处理备注
     * @param imgUrl 维修照片URL（可选）
     * @return 提交成功返回true，否则返回false
     */
    @Override
    @Transactional
    public boolean submitRepairResult(String orderId, String repairmanId, String dealNote, String imgUrl) {
        // 查询工单是否存在
        Optional<WorkOrder> orderOpt = workOrderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            WorkOrder order = orderOpt.get();

            // 校验权限：必须是当前工单的已分配维修人员，且工单状态为"已抢单"或"处理中"
            if (order.getAssignedRepairmanId() != null &&
                    order.getAssignedRepairmanId().equals(repairmanId) &&
                    (order.getStatus() == WorkOrder.OrderStatus.processing ||
                            order.getStatus() == WorkOrder.OrderStatus.processing)) {

                // 更新工单状态为"已完成"，记录完成时间和维修信息
                order.setStatus(WorkOrder.OrderStatus.completed);
                order.setCompletedTime(LocalDateTime.now());
                order.setDealNote(dealNote);
                order.setImgUrl(imgUrl); // 允许为空（可选参数）
                workOrderRepository.save(order);

                // 更新维修人员状态为"空闲"，并增加工作量
                repairmanRepository.findById(repairmanId).ifPresent(rm -> {
                    rm.setStatus(Repairman.RepairmanStatus.idle);
                    rm.setWorkCount(rm.getWorkCount() + 1);
                    repairmanRepository.save(rm);
                });

                return true; // 提交成功
            }
        }
        return false; // 提交失败（工单不存在/无权限/状态异常）
    }

    /**
     * 管理员审核工单（新增实现）
     * 业务规则：仅审核"待审核"状态的工单，通过则改为"已完成"，不通过则退回"处理中"
     */
    @Override
    @Transactional
    public boolean reviewOrder(String orderId, boolean approved) {
        Optional<WorkOrder> orderOpt = workOrderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            WorkOrder order = orderOpt.get();
            // 仅处理"待审核"状态的工单
            if (order.getStatus() == WorkOrder.OrderStatus.reviewing) {
                if (approved) {
                    // 审核通过：改为"已完成"，并更新维修人员工作量
                    order.setStatus(WorkOrder.OrderStatus.completed);
                    if (order.getAssignedRepairmanId() != null) {
                        repairmanRepository.findById(order.getAssignedRepairmanId())
                                .ifPresent(rm -> {
                                    rm.setWorkCount(rm.getWorkCount() + 1);
                                    repairmanRepository.save(rm);
                                });
                    }
                } else {
                    // 审核不通过：退回"处理中"
                    order.setStatus(WorkOrder.OrderStatus.processing);
                }
                workOrderRepository.save(order);
                return true;
            }
        }
        return false;
    }

    /**
     * 获取可抢工单列表
     * 业务规则：只返回指定区域内"待处理"状态的工单
     * @param areaId 区域ID
     * @return 符合条件的工单列表
     */
    @Override
    public List<WorkOrder> getAvailableOrders(String areaId) {
        if (areaId == null || areaId.trim().isEmpty()) {
            // 管理员：查所有区域的待处理工单
            return workOrderRepository.findByStatus(WorkOrder.OrderStatus.pending);
        } else {
            // 维修人员：查指定区域的待处理工单
            return workOrderRepository.findByAreaIdAndStatus(areaId, WorkOrder.OrderStatus.pending);
        }
    }

    /**
     * 获取维修人员自己的工单
     * 业务规则：返回所有分配给该维修人员的工单（不限制状态）
     * @param repairmanId 维修人员ID
     * @return 该维修人员负责的工单列表
     */
    @Override
    public List<WorkOrder> getMyOrders(String repairmanId) {
        return workOrderRepository.findByAssignedRepairmanId(repairmanId);
    }

    /**
     * 管理员手动派单功能
     * 业务规则：仅管理员可操作，只能分配"待处理"或"超时"状态的工单给"空闲"的维修人员
     * @param orderId 工单ID
     * @param repairmanId 维修人员ID
     * @return 派单成功返回true，否则返回false
     */
    @Override
    @Transactional
    public boolean assignOrderByAdmin(String orderId, String repairmanId) {
        // 查询工单是否存在
        Optional<WorkOrder> orderOpt = workOrderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            WorkOrder order = orderOpt.get();

            // 校验工单状态（只能分配待处理或超时工单）
            if (order.getStatus() == WorkOrder.OrderStatus.pending ||
                    order.getStatus() == WorkOrder.OrderStatus.timeout) {

                // 查询维修人员是否存在且状态为"空闲"
                Optional<Repairman> repairmanOpt = repairmanRepository.findById(repairmanId);
                if (repairmanOpt.isPresent() && repairmanOpt.get().getStatus() == Repairman.RepairmanStatus.idle) {
                    // 更新工单状态
                    order.setStatus(WorkOrder.OrderStatus.processing);
                    order.setAssignedRepairmanId(repairmanId);
                    order.setGrabbedTime(LocalDateTime.now());
                    workOrderRepository.save(order);

                    // 更新维修人员状态
                    Repairman repairman = repairmanOpt.get();
                    repairman.setStatus(Repairman.RepairmanStatus.busy);
                    repairmanRepository.save(repairman);

                    return true; // 派单成功
                }
            }
        }
        return false; // 派单失败（工单不存在/状态异常/维修人员不满足条件）
    }

    // 按状态查询工单的方法
    @Override
    public List<WorkOrder> getOrdersByStatus(WorkOrder.OrderStatus status) {
        return workOrderRepository.findByStatus(status);
    }

    @Override
    public List<WorkOrder> getOrdersByAreaAndStatus(String areaId, WorkOrder.OrderStatus status) {
        return workOrderRepository.findByAreaIdAndStatus(areaId, status);
    }
}