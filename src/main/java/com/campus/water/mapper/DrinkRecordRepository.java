package com.campus.water.mapper;

import datebaseclass.business.DrinkRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface DrinkRecordRepository extends JpaRepository<DrinkRecord, Long> {
    // 现有方法...
    List<DrinkRecord> findByStudentId(String studentId);
    List<DrinkRecord> findByTerminalId(String terminalId);
    List<DrinkRecord> findByDeviceId(String deviceId);

    // 新增基于drinkDate的查询方法
    List<DrinkRecord> findByStudentIdAndDrinkDate(String studentId, LocalDate drinkDate);
    List<DrinkRecord> findByStudentIdAndDrinkDateBetween(String studentId, LocalDate start, LocalDate end);

    @Query("SELECT SUM(d.volume) FROM DrinkRecord d WHERE d.studentId = ?1 AND d.drinkDate = ?2")
    Double getTotalVolumeByStudentIdAndDate(String studentId, LocalDate date);

    @Query("SELECT d FROM DrinkRecord d WHERE d.studentId = ?1 AND d.drinkDate BETWEEN ?2 AND ?3 ORDER BY d.drinkDate DESC, d.drinkTime DESC")
    List<DrinkRecord> findByStudentIdAndDrinkDateBetweenOrdered(String studentId, LocalDate start, LocalDate end);
}