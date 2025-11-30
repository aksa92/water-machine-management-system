package com.campus.water.repository;

import com.campus.water.entity.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, String> {
    // 根据区域ID查询工单
    List<WorkOrder> findByAreaId(String areaId);

    // 根据工单状态查询
    List<WorkOrder> findByStatus(WorkOrder.OrderStatus status);

    // 根据分配的维修人员ID查询工单
    List<WorkOrder> findByAssignedRepairmanId(String assignedRepairmanId);

    // 按区域和状态查询工单
    List<WorkOrder> findByAreaIdAndStatus(String areaId, WorkOrder.OrderStatus status);

    // 根据优先级查询工单
    List<WorkOrder> findByPriority(WorkOrder.OrderPriority priority);

    // 根据设备ID查询工单
    List<WorkOrder> findByDeviceId(String deviceId);

    // 根据工单类型查询
    List<WorkOrder> findByOrderType(WorkOrder.OrderType orderType);

    // 根据创建时间范围查询工单
    List<WorkOrder> findByCreatedTimeBetween(LocalDateTime start, LocalDateTime end);

    // 查询超时未完成工单
    List<WorkOrder> findByDeadlineBeforeAndStatusNot(LocalDateTime deadline, WorkOrder.OrderStatus status);

    // 查询维修人员负责的工单
    List<WorkOrder> findByAssignedRepairmanIdAndStatus(String assignedRepairmanId, WorkOrder.OrderStatus status);

    // 根据告警ID查询工单
    List<WorkOrder> findByAlertId(Long alertId);

    // 根据创建人查询工单
    List<WorkOrder> findByCreatedBy(String createdBy);
}