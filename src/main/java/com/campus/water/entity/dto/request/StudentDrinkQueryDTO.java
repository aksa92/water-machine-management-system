package com.campus.water.entity.dto.request;

import lombok.Data;

/**
 * 学生饮水量查询请求DTO
 */
@Data
public class StudentDrinkQueryDTO {
    /** 学生ID */
    private String studentId;
}