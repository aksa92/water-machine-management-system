package main.java.com.campus.water.controller;

import main.java.com.campus.water.entity.dto.request.StudentDrinkQueryDTO;
import main.java.com.campus.water.entity.vo.StudentDrinkStatsVO;
import main.java.com.campus.water.service.StudentDrinkStatsService;
import main.java.com.campus.water.util.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 学生饮水量统计接口
 */
@RestController
@RequestMapping("/api/student/drink-stats")
@RequiredArgsConstructor
@Tag(name = "学生端-饮水量统计", description = "学生查看本日/本周/本月饮水量")
public class StudentDrinkStatsController {

    private final StudentDrinkStatsService drinkStatsService;

    @PostMapping("/today")
    @Operation(summary = "查询本日饮水量", description = "获取学生当日的饮水量、次数及明细")
    public ResultVO<StudentDrinkStatsVO> getTodayStats(@RequestBody StudentDrinkQueryDTO request) {
        // 手动校验学生ID非空
        if (request.getStudentId() == null || request.getStudentId().trim().isEmpty()) {
            return ResultVO.badRequest("学生ID不能为空");
        }
        StudentDrinkStatsVO stats = drinkStatsService.getTodayDrinkStats(request.getStudentId());
        return ResultVO.success(stats, "查询本日饮水量成功");
    }

    @PostMapping("/this-week")
    @Operation(summary = "查询本周饮水量", description = "获取学生本周的饮水量、日均量及每日明细")
    public ResultVO<StudentDrinkStatsVO> getThisWeekStats(@RequestBody StudentDrinkQueryDTO request) {
        if (request.getStudentId() == null || request.getStudentId().trim().isEmpty()) {
            return ResultVO.badRequest("学生ID不能为空");
        }
        StudentDrinkStatsVO stats = drinkStatsService.getThisWeekDrinkStats(request.getStudentId());
        return ResultVO.success(stats, "查询本周饮水量成功");
    }

    @PostMapping("/this-month")
    @Operation(summary = "查询本月饮水量", description = "获取学生本月的饮水量、日均量及每日明细")
    public ResultVO<StudentDrinkStatsVO> getThisMonthStats(@RequestBody StudentDrinkQueryDTO request) {
        if (request.getStudentId() == null || request.getStudentId().trim().isEmpty()) {
            return ResultVO.badRequest("学生ID不能为空");
        }
        StudentDrinkStatsVO stats = drinkStatsService.getThisMonthDrinkStats(request.getStudentId());
        return ResultVO.success(stats, "查询本月饮水量成功");
    }
}