package com.campus.water.repository;

import com.campus.water.entity.DrinkRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DrinkRecommendationRepository extends JpaRepository<DrinkRecommendation, Long> {
    // 根据学生ID查询饮水推荐
    List<DrinkRecommendation> findByStudentId(String studentId);

    // 根据推荐日期查询
    List<DrinkRecommendation> findByRecommendationDate(LocalDate recommendationDate);

    // 根据日期范围查询饮水推荐
    List<DrinkRecommendation> findByRecommendationDateBetween(LocalDate start, LocalDate end);

    // 按学生和日期查找推荐记录
    @Query("SELECT d FROM DrinkRecommendation d WHERE d.studentId = ?1 AND d.recommendationDate = ?2")
    Optional<DrinkRecommendation> findByStudentIdAndRecommendationDate(String studentId, LocalDate date);

    // 查询学生时间段内的饮水推荐记录
    @Query("SELECT d FROM DrinkRecommendation d WHERE d.studentId = ?1 AND d.recommendationDate BETWEEN ?2 AND ?3 ORDER BY d.recommendationDate DESC")
    List<DrinkRecommendation> findByStudentIdAndRecommendationDateBetween(String studentId, LocalDate start, LocalDate end);

    // 统计学生某日总饮水进度
    @Query("SELECT SUM(d.currentProgress) FROM DrinkRecommendation d WHERE d.studentId = ?1 AND d.recommendationDate = ?2")
    Double getTotalProgressByStudentIdAndDate(String studentId, LocalDate date);

    // 计算学生期间平均目标
    @Query("SELECT AVG(d.dailyTarget) FROM DrinkRecommendation d WHERE d.studentId = ?1 AND d.recommendationDate BETWEEN ?2 AND ?3")
    Double getAverageDailyTargetByStudentIdAndPeriod(String studentId, LocalDate start, LocalDate end);
}