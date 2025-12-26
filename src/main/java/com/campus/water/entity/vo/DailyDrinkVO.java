package com.campus.water.entity.vo;

import lombok.Data;

/**
 * 每日饮水量明细VO
 */
@Data
public class DailyDrinkVO {
    /** 日期（yyyy-MM-dd） */
    private String date;
    /** 当日饮水量（升） */
    private Double consumption;
    /** 当日饮水次数 */
    private Integer count;
}