package main.java.com.campus.water.service;

import main.java.com.campus.water.entity.WorkOrder;
import main.java.com.campus.water.entity.Repairman;
import main.java.com.campus.water.mapper.WorkOrderRepository;
import main.java.com.campus.water.mapper.RepairmanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import main.java.com.campus.water.service.NotificationService;
import main.java.com.campus.water.service.WorkOrderService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * 工单服务实现类
 *
 * @author 豆包编程助手
 * @date 2025/12/25
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkOrderServiceImpl implements WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final RepairmanRepository repairmanRepository;
    private final NotificationService notificationService;

    @Override
    public WorkOrder getOrderDetail(String orderId) {
        return workOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("工单不存在"));
    }

    /**
     * 维修人员抢单功能
     * 业务规则：仅允许抢"待处理"状态的工单，且维修人员需处于"空闲"状态
     *
     * @param orderId       工单ID
     * @param repairmanId   维修人员ID
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
                // 查询维修人员是否存在
                Optional<Repairman> repairman = repairmanRepository.findById(repairmanId);
                if (repairman.isPresent()) {

                    // 更新工单状态：改为"已抢单"，记录抢单时间和维修人员ID
                    order.setStatus(WorkOrder.OrderStatus.processing);
                    order.setAssignedRepairmanId(repairmanId);
                    order.setGrabbedTime(LocalDateTime.now());
                    workOrderRepository.save(order);

                    // 改造点1：不再手动设置维修人员为忙碌，改为动态更新状态
                    updateRepairmanStatusByPendingOrders(repairmanId);

                    return true; // 抢单成功
                }
            }
        }
        return false; // 抢单失败（工单不存在/状态异常/维修人员不满足条件）
    }

    /**
     * 维修人员拒单功能
     * 业务规则：仅允许拒绝自己已抢单的工单，拒单后工单回到"待处理"状态
     *
     * @param orderId       工单ID
     * @param repairmanId   维修人员ID
     * @param reason        拒单原因
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

                // 改造点2：拒单后动态更新维修人员状态（而非直接设为空闲）
                updateRepairmanStatusByPendingOrders(repairmanId);

                return true; // 拒单成功
            }
        }
        return false; // 拒单失败（工单不存在/无权限/状态异常）
    }

    /**
     * 提交维修结果
     * 业务规则：仅允许提交自己负责的"已抢单"或"处理中"状态工单
     *
     * @param orderId       工单ID
     * @param repairmanId   维修人员ID
     * @param dealNote      处理备注
     * @param imgUrl        维修照片URL（可选）
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
                    (order.getStatus() == WorkOrder.OrderStatus.processing)) {

                // 更新工单状态为"待审核"（关键修改）
                order.setStatus(WorkOrder.OrderStatus.reviewing);
                order.setCompletedTime(LocalDateTime.now());
                order.setDealNote(dealNote);
                order.setImgUrl(imgUrl);
                workOrderRepository.save(order);

                // 改造点3：提交结果后动态更新维修人员状态（而非直接设为空闲）
                // 同时保留工作量统计逻辑
                repairmanRepository.findById(repairmanId).ifPresent(rm -> {
                    rm.setWorkCount(rm.getWorkCount() + 1);
                    repairmanRepository.save(rm);
                });
                updateRepairmanStatusByPendingOrders(repairmanId);

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
            if (order.getStatus() == WorkOrder.OrderStatus.reviewing) {
                if (approved) {
                    // 审核通过：改为"已完成"
                    order.setStatus(WorkOrder.OrderStatus.completed);
                    // 改造点4：审核通过后，动态更新维修人员状态
                    if (order.getAssignedRepairmanId() != null) {
                        updateRepairmanStatusByPendingOrders(order.getAssignedRepairmanId());
                    }
                } else {
                    // 审核不通过：退回"处理中"
                    order.setStatus(WorkOrder.OrderStatus.processing);
                    // 改造点5：审核不通过后，动态更新维修人员状态（而非直接设为忙碌）
                    if (order.getAssignedRepairmanId() != null) {
                        updateRepairmanStatusByPendingOrders(order.getAssignedRepairmanId());
                    }
                }
                workOrderRepository.save(order);
                return true;
            }
        }
        return false;
    }

    /**
     * 超时工单检查任务（改为静态内部类，解决@Slf4j报错问题）
     * 注意：需确保Spring开启了定时任务（@EnableScheduling）
     */
    @Slf4j
    @Service // 单独注册为Spring Bean，替代@Component
    @RequiredArgsConstructor
    public static class WorkOrderTimeoutTask {

        private final WorkOrderRepository workOrderRepository;

        // 每小时检查一次超时工单
        @Scheduled(fixedRate = 3600000)
        @Transactional
        public void checkTimeoutOrders() {
            log.info("开始检查超时工单");

            // 查询所有待处理和处理中的工单
            List<WorkOrder> activeOrders = workOrderRepository.findByStatusIn(
                    Arrays.asList(WorkOrder.OrderStatus.pending, WorkOrder.OrderStatus.processing)
            );

            LocalDateTime now = LocalDateTime.now();
            for (WorkOrder order : activeOrders) {
                // 检查是否设置了截止时间且已超时
                if (order.getDeadline() != null && now.isAfter(order.getDeadline())) {
                    order.setStatus(WorkOrder.OrderStatus.timeout);
                    workOrderRepository.save(order);
                    log.info("工单{}已超时，状态已更新为超时", order.getOrderId());
                }
            }

            log.info("超时工单检查完成，共检查{}个工单，其中{}个超时",
                    activeOrders.size(),
                    activeOrders.stream().filter(o -> o.getDeadline() != null && now.isAfter(o.getDeadline())).count());
        }
    }

    /**
     * 获取可抢工单列表
     * 业务规则：只返回指定区域内"待处理"状态的工单
     *
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
     *
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
     *
     * @param orderId       工单ID
     * @param repairmanId   维修人员ID
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

                // 查询维修人员是否存在
                Optional<Repairman> repairmanOpt = repairmanRepository.findById(repairmanId);
                if (repairmanOpt.isPresent()) {
                    // 更新工单状态
                    order.setStatus(WorkOrder.OrderStatus.processing);
                    order.setAssignedRepairmanId(repairmanId);
                    order.setGrabbedTime(LocalDateTime.now());
                    workOrderRepository.save(order);

                    // ===== 新增：派单通知 =====
                    try {
                        String notificationContent = String.format("您有新的维修工单待处理！工单ID：%s", orderId);
                        notificationService.sendOrderAssignedNotification(repairmanId, orderId, notificationContent);
                    } catch (Exception e) {
                        // 捕获异常，不影响原有派单逻辑
                        System.err.println("派单通知发送失败：" + e.getMessage());
                    }
                    // ==============================================

                    // 改造点6：派单后动态更新维修人员状态（而非直接设为忙碌）
                    updateRepairmanStatusByPendingOrders(repairmanId);

                    return true; // 派单成功
                }
            }
        }
        return false; // 派单失败（工单不存在/状态异常/维修人员不满足条件）
    }

    @Override
    public List<WorkOrder> getOrdersByStatus(WorkOrder.OrderStatus status) {
        return workOrderRepository.findByStatus(status);
    }

    @Override
    public List<WorkOrder> getOrdersByAreaAndStatus(String areaId, WorkOrder.OrderStatus status) {
        return workOrderRepository.findByAreaIdAndStatus(areaId, status);
    }

    @Override
    public List<WorkOrder> getOrdersByConditions(String areaId, WorkOrder.OrderStatus status,
                                                 LocalDateTime startTime, LocalDateTime endTime) {
        // 分步组合查询条件，核心复用已有findByCreatedTimeBetween方法
        List<WorkOrder> timeRangeOrders = workOrderRepository.findByCreatedTimeBetween(startTime, endTime);

        // 过滤区域和状态（如果传了这些参数）
        return timeRangeOrders.stream()
                .filter(order -> {
                    // 区域过滤：如果传了areaId则匹配，没传则不过滤
                    boolean areaMatch = (areaId == null || areaId.trim().isEmpty())
                            || order.getAreaId().equals(areaId);
                    // 状态过滤：如果传了status则匹配，没传则不过滤
                    boolean statusMatch = (status == null)
                            || order.getStatus() == status;
                    return areaMatch && statusMatch;
                })
                .toList();
    }

    // ===================== 新增核心辅助方法 =====================
    /**
     * 动态更新维修人员状态：根据待处理工单数量自动判定
     * 规则：无待处理工单（pending/processing/reviewing）= 空闲；有待处理工单=忙碌
     * @param repairmanId 维修人员ID
     */
    @Transactional
    private void updateRepairmanStatusByPendingOrders(String repairmanId) {
        // 1. 定义「待处理工单」状态集合（根据业务调整，此处包含待抢、已抢、待审核）
        List<WorkOrder.OrderStatus> pendingStatuses = Arrays.asList(
                WorkOrder.OrderStatus.pending,
                WorkOrder.OrderStatus.processing,
                WorkOrder.OrderStatus.reviewing
        );

        // 2. 查询该维修人员的待处理工单数量（需在WorkOrderRepository中定义该方法）
        long pendingOrderCount = workOrderRepository.countByAssignedRepairmanIdAndStatusIn(repairmanId, pendingStatuses);

        // 3. 动态更新维修人员状态
        Optional<Repairman> repairmanOpt = repairmanRepository.findById(repairmanId);
        if (repairmanOpt.isPresent()) {
            Repairman repairman = repairmanOpt.get();
            if (pendingOrderCount <= 0) {
                // 无待处理工单 → 空闲状态
                repairman.setStatus(Repairman.RepairmanStatus.idle);
            } else {
                // 有待处理工单 → 忙碌状态
                repairman.setStatus(Repairman.RepairmanStatus.busy);
            }
            repairmanRepository.save(repairman);
            log.debug("维修人员{}状态更新完成，当前待处理工单数量：{}，状态：{}",
                    repairmanId, pendingOrderCount, repairman.getStatus());
        }
    }

}