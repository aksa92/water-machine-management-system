package com.campus.water.service.impl;

import com.campus.water.entity.Notification;
import com.campus.water.Repository.NotificationRepository;
import com.campus.water.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 通知服务实现类
 */
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * 发送派单通知
     */
    @Override
    public void sendOrderAssignedNotification(String repairmanId, String orderId, String content) {
        Notification notification = new Notification();
        notification.setRepairmanId(repairmanId);
        notification.setOrderId(orderId);
        notification.setContent(content);
        notification.setType(Notification.NotificationType.ORDER_ASSIGNED);
        notificationRepository.save(notification);
    }

    /**
     * 获取未读通知
     */
    @Override
    public List<Notification> getUnreadNotifications(String repairmanId) {
        return notificationRepository.findByRepairmanIdAndIsReadFalseOrderByCreatedTimeDesc(repairmanId);
    }

    /**
     * 获取所有通知
     */
    @Override
    public List<Notification> getAllNotifications(String repairmanId) {
        return notificationRepository.findByRepairmanIdOrderByCreatedTimeDesc(repairmanId);
    }

    /**
     * 标记通知为已读
     */
    @Override
    public void markAsRead(Long notificationId) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            notification.setRead(true);
            notificationRepository.save(notification);
        });
    }
}