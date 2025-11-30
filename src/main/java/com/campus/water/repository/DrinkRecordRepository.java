package com.campus.water.repository;

import com.campus.water.entity.DrinkRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DrinkRecordRepository extends JpaRepository<DrinkRecord, Long> {
    List<DrinkRecord> findByStudentId(String studentId);
    List<DrinkRecord> findByTerminalId(String terminalId);
    List<DrinkRecord> findByDeviceId(String deviceId);

    // 修正：使用 drink_time 字段进行日期范围查询
    @Query("SELECT d FROM DrinkRecord d WHERE d.studentId = ?1 AND DATE(d.drinkTime) = ?2")
    List<DrinkRecord> findByStudentIdAndDrinkDate(String studentId, LocalDate drinkDate);

    @Query("SELECT d FROM DrinkRecord d WHERE d.studentId = ?1 AND d.drinkTime BETWEEN ?2 AND ?3")
    List<DrinkRecord> findByStudentIdAndDrinkTimeBetween(String studentId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(d.waterConsumption) FROM DrinkRecord d WHERE d.studentId = ?1 AND DATE(d.drinkTime) = ?2")
    Double getTotalWaterConsumptionByStudentIdAndDate(String studentId, LocalDate date);

    @Query("SELECT d FROM DrinkRecord d WHERE d.studentId = ?1 AND d.drinkTime BETWEEN ?2 AND ?3 ORDER BY d.drinkTime DESC")
    List<DrinkRecord> findByStudentIdAndDrinkTimeBetweenOrdered(String studentId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT d FROM DrinkRecord d WHERE d.terminalId = ?1 AND d.drinkTime BETWEEN ?2 AND ?3")
    List<DrinkRecord> findByTerminalIdAndDrinkTimeBetween(String terminalId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT COUNT(d) FROM DrinkRecord d WHERE d.terminalId = ?1 AND DATE(d.drinkTime) = ?2")
    Long countByTerminalIdAndDrinkDate(String terminalId, LocalDate drinkDate);
}