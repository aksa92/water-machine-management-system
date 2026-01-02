package main.java.com.campus.water.mapper;

import main.java.com.campus.water.entity.InspectionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InspectionRecordRepository extends JpaRepository<InspectionRecord, Long> {
    // 根据工单ID查询巡检记录
    List<InspectionRecord> findByOrderId(String orderId);

    // 根据维修人员ID查询巡检记录
    List<InspectionRecord> findByRepairmanId(String repairmanId);

    // 根据设备ID查询巡检记录
    List<InspectionRecord> findByDeviceId(String deviceId);

    // 根据巡检状态查询记录
    List<InspectionRecord> findByInspectionStatus(InspectionRecord.InspectionStatus status);

    // 根据巡检时间范围查询记录
    List<InspectionRecord> findByInspectionTimeBetween(LocalDateTime start, LocalDateTime end);

    // 查询维修人员期间巡检记录
    List<InspectionRecord> findByRepairmanIdAndInspectionTimeBetween(String repairmanId, LocalDateTime start, LocalDateTime end);

    // 根据提交时间范围查询记录
    List<InspectionRecord> findBySubmittedAtBetween(LocalDateTime start, LocalDateTime end);
}