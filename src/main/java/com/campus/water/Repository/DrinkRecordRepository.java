package com.campus.water.Repository;

import com.campus.water.entity.DrinkRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DrinkRecordRepository extends JpaRepository<DrinkRecord, Long> {
    // 根据学生ID查询饮水记录
    List<DrinkRecord> findByStudentId(String studentId);

    // 根据终端ID查询饮水记录
    List<DrinkRecord> findByTerminalId(String terminalId);

    // 根据设备ID查询饮水记录
    List<DrinkRecord> findByDeviceId(String deviceId);

    // 查询学生某日的饮水记录
    @Query("SELECT d FROM DrinkRecord d WHERE d.studentId = ?1 AND DATE(d.drinkTime) = ?2")
    List<DrinkRecord> findByStudentIdAndDrinkDate(String studentId, LocalDate drinkDate);

    // 查询学生时间段内的饮水记录
    @Query("SELECT d FROM DrinkRecord d WHERE d.studentId = ?1 AND d.drinkTime BETWEEN ?2 AND ?3")
    List<DrinkRecord> findByStudentIdAndDrinkTimeBetween(String studentId, LocalDateTime start, LocalDateTime end);

    // 统计学生单日总饮水量
    @Query("SELECT SUM(d.waterConsumption) FROM DrinkRecord d WHERE d.studentId = ?1 AND DATE(d.drinkTime) = ?2")
    Double getTotalWaterConsumptionByStudentIdAndDate(String studentId, LocalDate date);

    // 查询学生时间段内的饮水记录（按时间倒序）
    @Query("SELECT d FROM DrinkRecord d WHERE d.studentId = ?1 AND d.drinkTime BETWEEN ?2 AND ?3 ORDER BY d.drinkTime DESC")
    List<DrinkRecord> findByStudentIdAndDrinkTimeBetweenOrdered(String studentId, LocalDateTime start, LocalDateTime end);

    // 查询终端时间段内的饮水记录
    @Query("SELECT d FROM DrinkRecord d WHERE d.terminalId = ?1 AND d.drinkTime BETWEEN ?2 AND ?3")
    List<DrinkRecord> findByTerminalIdAndDrinkTimeBetween(String terminalId, LocalDateTime start, LocalDateTime end);

    // 统计终端单日使用次数
    @Query("SELECT COUNT(d) FROM DrinkRecord d WHERE d.terminalId = ?1 AND DATE(d.drinkTime) = ?2")
    Long countByTerminalIdAndDrinkDate(String terminalId, LocalDate drinkDate);
}