package com.campus.water.service;

import com.campus.water.entity.Notification;
import java.util.List;

/**
 * 维修人员通知服务接口
 */
public interface NotificationService {
    /**
     * 发送派单通知
     * @param repairmanId 维修人员ID
     * @param orderId 工单ID
     * @param content 通知内容
     */
    void sendOrderAssignedNotification(String repairmanId, String orderId, String content);

    /**
     * 获取维修人员未读通知
     * @param repairmanId 维修人员ID
     * @return 未读通知列表
     */
    List<Notification> getUnreadNotifications(String repairmanId);

    /**
     * 获取维修人员所有通知
     * @param repairmanId 维修人员ID
     * @return 所有通知列表
     */
    List<Notification> getAllNotifications(String repairmanId);

    /**
     * 标记通知为已读
     * @param notificationId 通知ID
     */
    void markAsRead(Long notificationId);
}