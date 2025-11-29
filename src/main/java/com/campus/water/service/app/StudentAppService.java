package com.campus.water.service.app;

import com.campus.water.controller.WaterUsageController;
import com.campus.water.util.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class StudentAppService {

    @Autowired
    private WaterUsageController waterUsageController;

    // 扫码获取终端信息
    public ResultVO<Map<String, Object>> getTerminalInfo(String terminalId) {
        try {
            Map<String, Object> result = waterUsageController.getTerminalInfo(terminalId);
            return ResultVO.success(result);
        } catch (Exception e) {
            return ResultVO.error(500, "获取终端信息失败: " + e.getMessage());
        }
    }

    // 扫码用水
    public ResultVO<Map<String, Object>> scanToDrink(Map<String, Object> request) {
        try {
            String terminalId = (String) request.get("terminalId");
            String studentId = (String) request.get("studentId");
            Double waterConsumption = Double.valueOf(request.get("waterConsumption").toString());

            Map<String, Object> result = waterUsageController.scanToDrink(terminalId, studentId, waterConsumption);
            return ResultVO.success(result);
        } catch (Exception e) {
            return ResultVO.error(500, "用水操作失败: " + e.getMessage());
        }
    }

    // 查询水质信息
    public ResultVO<Map<String, Object>> getWaterQuality(String deviceId) {
        try {
            Map<String, Object> result = waterUsageController.getWaterQualityInfo(deviceId);
            return ResultVO.success(result);
        } catch (Exception e) {
            return ResultVO.error(500, "获取水质信息失败: " + e.getMessage());
        }
    }
}