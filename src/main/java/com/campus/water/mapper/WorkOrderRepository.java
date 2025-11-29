package com.campus.water.mapper;

import com.campus.water.entity.WorkOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WorkOrderRepository extends JpaRepository<WorkOrder, String> {
    List<WorkOrder> findByAreaId(String areaId);
    List<WorkOrder> findByStatus(WorkOrder.OrderStatus status);
    List<WorkOrder> findByAssignedRepairmanId(String assignedRepairmanId);
    List<WorkOrder> findByAreaIdAndStatus(String areaId, WorkOrder.OrderStatus status);
    List<WorkOrder> findByPriority(WorkOrder.OrderPriority priority);
    List<WorkOrder> findByDeviceId(String deviceId);
    List<WorkOrder> findByOrderType(WorkOrder.OrderType orderType);
    List<WorkOrder> findByCreatedTimeBetween(LocalDateTime start, LocalDateTime end);
    List<WorkOrder> findByDeadlineBeforeAndStatusNot(LocalDateTime deadline, WorkOrder.OrderStatus status);
    List<WorkOrder> findByAssignedRepairmanIdAndStatus(String assignedRepairmanId, WorkOrder.OrderStatus status);
    List<WorkOrder> findByAlertId(Long alertId);
    List<WorkOrder> findByCreatedBy(String createdBy);
}