package com.campus.water.service;

import com.campus.water.entity.WorkOrder;
import com.campus.water.entity.Repairman;
import com.campus.water.mapper.WorkOrderRepository;
import com.campus.water.mapper.RepairmanRepository;
import com.campus.water.service.WorkOrderService;
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
    @Transactional
    public boolean grabOrder(String orderId, String repairmanId) {
        // 原抢单逻辑（从Controller迁移）
        Optional<WorkOrder> orderOpt = workOrderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            WorkOrder order = orderOpt.get();
            if (order.getStatus() == WorkOrder.OrderStatus.pending) {
                var repairman = repairmanRepository.findById(repairmanId);
                if (repairman.isPresent() && repairman.get().getStatus() == Repairman.RepairmanStatus.idle) {
                    order.setStatus(WorkOrder.OrderStatus.grabbed);
                    order.setAssignedRepairmanId(repairmanId);
                    order.setGrabbedTime(LocalDateTime.now());
                    workOrderRepository.save(order);

                    var repairmanEntity = repairman.get();
                    repairmanEntity.setStatus(Repairman.RepairmanStatus.busy);
                    repairmanRepository.save(repairmanEntity);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    @Transactional
    public boolean rejectOrder(String orderId, String repairmanId, String reason) {
        // 原拒单逻辑（从Controller迁移）
        Optional<WorkOrder> orderOpt = workOrderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            WorkOrder order = orderOpt.get();
            if (order.getAssignedRepairmanId() != null &&
                    order.getAssignedRepairmanId().equals(repairmanId) &&
                    order.getStatus() == WorkOrder.OrderStatus.grabbed) {

                order.setStatus(WorkOrder.OrderStatus.pending);
                order.setAssignedRepairmanId(null);
                order.setGrabbedTime(null);
                workOrderRepository.save(order);

                var repairman = repairmanRepository.findById(repairmanId);
                repairman.ifPresent(rm -> {
                    rm.setStatus(Repairman.RepairmanStatus.idle);
                    repairmanRepository.save(rm);
                });
                return true;
            }
        }
        return false;
    }

    @Override
    @Transactional
    public boolean submitRepairResult(String orderId, String repairmanId, String dealNote, String imgUrl) {
        // 原提交维修结果逻辑（从Controller迁移）
        Optional<WorkOrder> orderOpt = workOrderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            WorkOrder order = orderOpt.get();
            if (order.getAssignedRepairmanId() != null &&
                    order.getAssignedRepairmanId().equals(repairmanId) &&
                    (order.getStatus() == WorkOrder.OrderStatus.grabbed ||
                            order.getStatus() == WorkOrder.OrderStatus.processing)) {

                order.setStatus(WorkOrder.OrderStatus.completed);
                order.setCompletedTime(LocalDateTime.now());
                order.setDealNote(dealNote);
                order.setImgUrl(imgUrl);
                workOrderRepository.save(order);

                repairmanRepository.findById(repairmanId).ifPresent(rm -> {
                    rm.setStatus(Repairman.RepairmanStatus.idle);
                    rm.setWorkCount(rm.getWorkCount() + 1);
                    repairmanRepository.save(rm);
                });
                return true;
            }
        }
        return false;
    }

    @Override
    public List<WorkOrder> getAvailableOrders(String areaId) {
        return workOrderRepository.findByAreaIdAndStatus(areaId, WorkOrder.OrderStatus.pending);
    }

    @Override
    public List<WorkOrder> getMyOrders(String repairmanId) {
        return workOrderRepository.findByAssignedRepairmanId(repairmanId);
    }

    @Override
    @Transactional
    public boolean assignOrderByAdmin(String orderId, String repairmanId) {
        // 管理员手动派单逻辑（新增实现）
        Optional<WorkOrder> orderOpt = workOrderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            WorkOrder order = orderOpt.get();
            // 校验工单状态（只能分配待处理或超时工单）
            if (order.getStatus() == WorkOrder.OrderStatus.pending ||
                    order.getStatus() == WorkOrder.OrderStatus.timeout) {

                Optional<Repairman> repairmanOpt = repairmanRepository.findById(repairmanId);
                if (repairmanOpt.isPresent() && repairmanOpt.get().getStatus() == Repairman.RepairmanStatus.idle) {
                    // 更新工单状态
                    order.setStatus(WorkOrder.OrderStatus.grabbed);
                    order.setAssignedRepairmanId(repairmanId);
                    order.setGrabbedTime(LocalDateTime.now());
                    workOrderRepository.save(order);

                    // 更新维修人员状态
                    Repairman repairman = repairmanOpt.get();
                    repairman.setStatus(Repairman.RepairmanStatus.busy);
                    repairmanRepository.save(repairman);
                    return true;
                }
            }
        }
        return false;
    }
}