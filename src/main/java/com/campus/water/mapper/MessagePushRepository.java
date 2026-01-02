package com.campus.water.mapper;

import com.campus.water.entity.MessagePush;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessagePushRepository extends JpaRepository<MessagePush, Integer> {
    // 根据学生ID查询消息
    List<MessagePush> findByStudentId(String studentId);

    // 根据管理员ID查询消息
    List<MessagePush> findByAdminId(String adminId);

    // 根据维修人员ID查询消息
    List<MessagePush> findByRepairmanId(String repairmanId);

    // 根据用户类型查询消息
    List<MessagePush> findByUserType(String userType);

    // 根据消息类型查询
    List<MessagePush> findByMessageType(String messageType);

    // 根据阅读状态查询消息
    List<MessagePush> findByIsRead(Boolean isRead);

    // 按用户ID和类型查询消息
    @Query("SELECT m FROM MessagePush m WHERE m.userId = ?1 AND m.userType = ?2")
    List<MessagePush> findByUserIdAndUserType(String userId, String userType);

    // 按推送时间范围查询消息
    @Query("SELECT m FROM MessagePush m WHERE m.pushTime BETWEEN ?1 AND ?2")
    List<MessagePush> findByPushTimeBetween(LocalDateTime start, LocalDateTime end);

    // 根据相关ID查询消息
    @Query("SELECT m FROM MessagePush m WHERE m.relatedId = ?1")
    List<MessagePush> findByRelatedId(String relatedId);

    // 统计用户未读消息数量
    @Query("SELECT COUNT(m) FROM MessagePush m WHERE m.userId = ?1 AND m.isRead = false")
    Long countUnreadMessagesByUserId(String userId);
}