package main.java.com.campus.water.mapper;

import main.java.com.campus.water.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * 通知数据访问层
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    /**
     * 查询维修人员未读通知（按创建时间倒序）
     */
    List<Notification> findByRepairmanIdAndIsReadFalseOrderByCreatedTimeDesc(String repairmanId);

    /**
     * 查询维修人员所有通知（按创建时间倒序）
     */
    List<Notification> findByRepairmanIdOrderByCreatedTimeDesc(String repairmanId);
}