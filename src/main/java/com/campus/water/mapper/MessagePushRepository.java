package com.campus.water.mapper;

import datebaseclass.business.MessagePush;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessagePushRepository extends JpaRepository<MessagePush, Integer> {
    List<MessagePush> findByStudentId(String studentId);
    List<MessagePush> findByAdminId(String adminId);
    List<MessagePush> findByRepairmanId(String repairmanId);
    List<MessagePush> findByUserType(String userType);
    List<MessagePush> findByMessageType(String messageType);
    List<MessagePush> findByIsRead(Boolean isRead);

    @Query("SELECT m FROM MessagePush m WHERE m.userId = ?1 AND m.userType = ?2")
    List<MessagePush> findByUserIdAndUserType(String userId, String userType);

    @Query("SELECT m FROM MessagePush m WHERE m.pushTime BETWEEN ?1 AND ?2")
    List<MessagePush> findByPushTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT m FROM MessagePush m WHERE m.relatedId = ?1")
    List<MessagePush> findByRelatedId(String relatedId);

    @Query("SELECT COUNT(m) FROM MessagePush m WHERE m.userId = ?1 AND m.isRead = false")
    Long countUnreadMessagesByUserId(String userId);
}