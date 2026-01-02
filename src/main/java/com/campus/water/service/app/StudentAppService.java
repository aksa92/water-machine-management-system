package main.java.com.campus.water.service.app;

import main.java.com.campus.water.controller.WaterUsageController;
import main.java.com.campus.water.util.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class StudentAppService {

    @Autowired
    private WaterUsageController waterUsageController;

    // 扫码获取终端信息 - 学生和管理员可访问
    @PreAuthorize("hasAnyRole('STUDENT', 'SUPER_ADMIN', 'AREA_ADMIN', 'VIEWER')")
    public ResultVO<Map<String, Object>> getTerminalInfo(String terminalId) {
        try {
            // 直接返回控制器的ResultVO
            return waterUsageController.getTerminalInfo(terminalId);
        } catch (Exception e) {
            return ResultVO.error(500, "获取终端信息失败: " + e.getMessage());
        }
    }

    // 扫码用水 - 学生和管理员可访问
    @PreAuthorize("hasAnyRole('STUDENT', 'SUPER_ADMIN', 'AREA_ADMIN', 'VIEWER')")
    public ResultVO<Map<String, Object>> scanToDrink(Map<String, Object> request) {
        try {
            String terminalId = (String) request.get("terminalId");
            String studentId = (String) request.get("studentId");
            Double waterConsumption = Double.valueOf(request.get("waterConsumption").toString());

            // 直接返回控制器的ResultVO
            return waterUsageController.scanToDrink(terminalId, studentId, waterConsumption);
        } catch (Exception e) {
            return ResultVO.error(500, "用水操作失败: " + e.getMessage());
        }
    }

    // 查询水质信息 - 学生和管理员可访问
    @PreAuthorize("hasAnyRole('STUDENT', 'SUPER_ADMIN', 'AREA_ADMIN', 'VIEWER')")
    public ResultVO<Map<String, Object>> getWaterQuality(String deviceId) {
        try {
            // 直接返回控制器的ResultVO
            return waterUsageController.getWaterQualityInfo(deviceId);
        } catch (Exception e) {
            return ResultVO.error(500, "获取水质信息失败: " + e.getMessage());
        }
    }
}