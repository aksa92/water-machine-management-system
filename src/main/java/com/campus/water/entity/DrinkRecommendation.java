/**
 * 饮水推荐实体类
 * 对应表：drink_recommendation
 * 用于记录学生的每日饮水目标和当前进度
 */
package com.campus.water.entity;

import lombok.Data;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "drink_recommendation")
public class DrinkRecommendation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recommendation_id")
    private Integer recommendationId;

    @Column(name = "student_id", length = 50)
    private String studentId;

    @Column(name = "daily_target")
    private Double dailyTarget;

    @Column(name = "current_progress", precision = 6, scale = 2)
    private BigDecimal currentProgress;

    @Column(name = "recommendation_date")
    private LocalDate recommendationDate;

    @Column(name = "created_time")
    private LocalDateTime createdTime = LocalDateTime.now();
}