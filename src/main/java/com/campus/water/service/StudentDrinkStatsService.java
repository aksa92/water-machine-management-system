package com.campus.water.service;

import com.campus.water.entity.DrinkRecord;
import com.campus.water.entity.vo.DailyDrinkVO;
import com.campus.water.entity.vo.StudentDrinkStatsVO;
import com.campus.water.mapper.DrinkRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学生饮水量统计服务
 */
@Service
@RequiredArgsConstructor
public class StudentDrinkStatsService {

    private final DrinkRecordRepository drinkRecordRepository;

    /**
     * 查询学生本日饮水量统计
     */
    public StudentDrinkStatsVO getTodayDrinkStats(String studentId) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = LocalDateTime.now();
        return calculateStats(studentId, start, end, "本日", "today");
    }

    /**
     * 查询学生本周饮水量统计
     */
    public StudentDrinkStatsVO getThisWeekDrinkStats(String studentId) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        LocalDateTime end = LocalDateTime.now();
        return calculateStats(studentId, start, end, "本周", "thisWeek");
    }

    /**
     * 查询学生本月饮水量统计
     */
    public StudentDrinkStatsVO getThisMonthDrinkStats(String studentId) {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
        LocalDateTime end = LocalDateTime.now();
        return calculateStats(studentId, start, end, "本月", "thisMonth");
    }

    /**
     * 核心统计逻辑
     */
    private StudentDrinkStatsVO calculateStats(String studentId, LocalDateTime start, LocalDateTime end,
                                               String timeRangeDesc, String timeDimension) {
        // 1. 查询时间范围内的饮水记录
        List<DrinkRecord> records = drinkRecordRepository
                .findByStudentIdAndDrinkTimeBetweenOrdered(studentId, start, end);

        // 2. 按日期分组统计
        Map<LocalDate, List<DrinkRecord>> dailyGroup = records.stream()
                .collect(Collectors.groupingBy(record -> record.getDrinkTime().toLocalDate()));

        // 3. 构建每日明细
        List<DailyDrinkVO> dailyDetails = new ArrayList<>();
        dailyGroup.forEach((date, dailyRecords) -> {
            DailyDrinkVO dailyVO = new DailyDrinkVO();
            dailyVO.setDate(date.toString());
            // 当日总饮水量
            double dailyTotal = dailyRecords.stream()
                    .map(DrinkRecord::getWaterConsumption)
                    .filter(Objects::nonNull)
                    .mapToDouble(BigDecimal::doubleValue)
                    .sum();
            dailyVO.setConsumption(dailyTotal);
            dailyVO.setCount(dailyRecords.size());
            dailyDetails.add(dailyVO);
        });
        // 按日期排序
        dailyDetails.sort(Comparator.comparing(DailyDrinkVO::getDate));

        // 4. 计算总饮水量、总次数、日均饮水量
        double totalConsumption = dailyDetails.stream()
                .mapToDouble(DailyDrinkVO::getConsumption)
                .sum();
        int totalCount = records.size();
        double avgDaily = dailyDetails.isEmpty() ? 0 : totalConsumption / dailyDetails.size();

        // 5. 封装结果VO
        StudentDrinkStatsVO statsVO = new StudentDrinkStatsVO();
        statsVO.setStudentId(studentId);
        statsVO.setTimeDimension(timeDimension);
        statsVO.setTimeRange(timeRangeDesc + "(" + start.toLocalDate() + "~" + end.toLocalDate() + ")");
        statsVO.setTotalConsumption(totalConsumption);
        statsVO.setDrinkCount(totalCount);
        statsVO.setAvgDailyConsumption(avgDaily);
        statsVO.setDailyDetails(dailyDetails);
        statsVO.setDrinkRecords(records);

        return statsVO;
    }
}