package com.campus.water.student.controller;

import com.campus.water.student.entity.DrinkRecordVO;
import com.campus.water.student.service.DrinkRecordService;
import com.campus.water.student.service.WaterQualityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 饮水接口（供学生APP前端调用，适配“扫码用水、查看饮水量”需求）
 */
@RestController
@RequestMapping("/api/student/drink")
public class DrinkController {
    @Autowired
    private DrinkRecordService drinkRecordService;
    @Autowired
    private WaterQualityService waterQualityService;

    // 1. 扫码用水（生成饮水记录，需求核心功能）
    @PostMapping("/scan")
    public String scanDrink(
            @RequestParam String studentId,
            @RequestParam String terminalId,
            @RequestParam double volume // 饮水量（终端机流量传感器获取或按时间估算）
    ) {
        // 获取当前终端机水质（用于记录）
        String waterQuality = waterQualityService.getWaterQuality(terminalId);
        // 生成饮水记录（存入MySQL）
        drinkRecordService.createDrinkRecord(studentId, terminalId, volume, waterQuality);
        return "扫码用水成功，饮水量：" + volume + "L，水质：" + waterQuality;
    }

    // 2. 查询今日饮水量（需求“查看每日饮水量”功能）
    @GetMapping("/today/{studentId}")
    public DrinkRecordVO getTodayDrink(@PathVariable String studentId) {
        return drinkRecordService.getTodayDrinkRecord(studentId);
    }

    // 3. 查询历史饮水量（按日/周/月筛选）
    @GetMapping("/history/{studentId}")
    public List<DrinkRecordVO> getHistoryDrink(
            @PathVariable String studentId,
            @RequestParam String type, // type: day/week/month
            @RequestParam String date  // 日期（如2024-05-20）
    ) {
        return drinkRecordService.getHistoryDrinkRecord(studentId, type, date);
    }
}