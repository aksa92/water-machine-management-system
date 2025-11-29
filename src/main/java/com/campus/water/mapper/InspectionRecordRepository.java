package com.campus.water.mapper;

import com.campus.water.entity.InspectionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InspectionRecordRepository extends JpaRepository<InspectionRecord, Long> {
    List<InspectionRecord> findByOrderId(String orderId);
    List<InspectionRecord> findByRepairmanId(String repairmanId);
    List<InspectionRecord> findByDeviceId(String deviceId);
    List<InspectionRecord> findByInspectionStatus(InspectionRecord.InspectionStatus status);
    List<InspectionRecord> findByInspectionTimeBetween(LocalDateTime start, LocalDateTime end);
    List<InspectionRecord> findByRepairmanIdAndInspectionTimeBetween(String repairmanId, LocalDateTime start, LocalDateTime end);
    List<InspectionRecord> findBySubmittedAtBetween(LocalDateTime start, LocalDateTime end);
}