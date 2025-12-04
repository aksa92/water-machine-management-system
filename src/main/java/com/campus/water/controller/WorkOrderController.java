package com.campus.water.controller;

import com.campus.water.entity.WorkOrder;
import com.campus.water.mapper.WorkOrderRepository;
import com.campus.water.mapper.RepairmanRepository;
import com.campus.water.mapper.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
public class WorkOrderController {

    @Autowired
    private WorkOrderRepository workOrderRepository;

    @Autowired
    private RepairmanRepository repairmanRepository;

    @Autowired
    private AlertRepository alertRepository;

    // 抢单功能 - 维修人员和管理员可访问
    @PreAuthorize("hasAnyRole('REPAIRMAN', 'ADMIN')")
    @Transactional
    public boolean grabOrder(String orderId, String repairmanId) {
        Optional<WorkOrder> orderOpt = workOrderRepository.findById(orderId);
        if (orderOpt.isPresent()) {
            WorkOrder order = orderOpt.get();
            if (order.getStatus() == WorkOrder.OrderStatus.pending) {
                var repairman = repairmanRepository.findById(repairmanId);
                if (repairman.isPresent() && repairman.get().getStatus() == repairman.get().getStatus().idle) {
                    order.setStatus(WorkOrder.OrderStatus.grabbed);
                    order.setAssignedRepairmanId(repairmanId);
                    order.setGrabbedTime(LocalDateTime.now());
                    workOrderRepository.save(order);

                    var repairmanEntity = repairman.get();
                    repairmanEntity.setStatus(repairmanEntity.getStatus().busy);
                    repairmanRepository.save(repairmanEntity);
                    return true;
                }
            }
        }
        return false;
    }

    // 拒单功能 - 维修人员和管理员可访问
    @PreAuthorize("hasAnyRole('REPAIRMAN', 'ADMIN')")
    @Transactional
    public boolean rejectOrder(String orderId, String repairmanId, String reason) {
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
                if (repairman.isPresent()) {
                    var repairmanEntity = repairman.get();
                    repairmanEntity.setStatus(repairmanEntity.getStatus().idle);
                    repairmanRepository.save(repairmanEntity);
                }
                return true;
            }
        }
        return false;
    }

    // 提交维修结果 - 维修人员和管理员可访问
    @PreAuthorize("hasAnyRole('REPAIRMAN', 'ADMIN')")
    @Transactional
    public boolean submitRepairResult(String orderId, String repairmanId,
                                      String dealNote, String imgUrl) {
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

                var repairman = repairmanRepository.findById(repairmanId);
                if (repairman.isPresent()) {
                    var repairmanEntity = repairman.get();
                    repairmanEntity.setStatus(repairmanEntity.getStatus().idle);
                    repairmanEntity.setWorkCount(repairmanEntity.getWorkCount() + 1);
                    repairmanRepository.save(repairmanEntity);
                }
                return true;
            }
        }
        return false;
    }

    // 获取可抢工单列表 - 维修人员和管理员可访问
    @PreAuthorize("hasAnyRole('REPAIRMAN', 'ADMIN')")
    public List<WorkOrder> getAvailableOrders(String areaId) {
        return workOrderRepository.findByAreaIdAndStatus(areaId, WorkOrder.OrderStatus.pending);
    }

    // 获取维修工自己的工单 - 维修人员和管理员可访问
    @PreAuthorize("hasAnyRole('REPAIRMAN', 'ADMIN')")
    public List<WorkOrder> getMyOrders(String repairmanId) {
        return workOrderRepository.findByAssignedRepairmanId(repairmanId);
    }
}