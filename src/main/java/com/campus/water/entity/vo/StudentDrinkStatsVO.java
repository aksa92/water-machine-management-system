package main.java.com.campus.water.entity.vo;

import lombok.Data;
import java.util.List;
import main.java.com.campus.water.entity.DrinkRecord;
import main.java.com.campus.water.entity.vo.DailyDrinkVO;
/**
 * 学生饮水量统计结果VO
 */
@Data
public class StudentDrinkStatsVO {
    /** 学生ID */
    private String studentId;
    /** 统计维度（本日/本周/本月） */
    private String timeDimension;
    /** 统计时间范围（如"2025-12-25~2025-12-25"） */
    private String timeRange;
    /** 总饮水量（升） */
    private Double totalConsumption;
    /** 日均饮水量（升） */
    private Double avgDailyConsumption;
    /** 饮水次数 */
    private Integer drinkCount;
    /** 按日期分组的每日饮水量明细 */
    private List<DailyDrinkVO> dailyDetails;
    /** 所有饮水记录明细 */
    private List<DrinkRecord> drinkRecords;
}
