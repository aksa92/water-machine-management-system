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
    List<DrinkRecommendation> findByStudentId(String studentId);
    List<DrinkRecommendation> findByRecommendationDate(LocalDate recommendationDate);
    List<DrinkRecommendation> findByRecommendationDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT d FROM DrinkRecommendation d WHERE d.studentId = ?1 AND d.recommendationDate = ?2")
    Optional<DrinkRecommendation> findByStudentIdAndRecommendationDate(String studentId, LocalDate date);

    @Query("SELECT d FROM DrinkRecommendation d WHERE d.studentId = ?1 AND d.recommendationDate BETWEEN ?2 AND ?3 ORDER BY d.recommendationDate DESC")
    List<DrinkRecommendation> findByStudentIdAndRecommendationDateBetween(String studentId, LocalDate start, LocalDate end);

    @Query("SELECT SUM(d.currentProgress) FROM DrinkRecommendation d WHERE d.studentId = ?1 AND d.recommendationDate = ?2")
    Double getTotalProgressByStudentIdAndDate(String studentId, LocalDate date);

    @Query("SELECT AVG(d.dailyTarget) FROM DrinkRecommendation d WHERE d.studentId = ?1 AND d.recommendationDate BETWEEN ?2 AND ?3")
    Double getAverageDailyTargetByStudentIdAndPeriod(String studentId, LocalDate start, LocalDate end);
}