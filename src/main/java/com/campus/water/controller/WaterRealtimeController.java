package main.java.com.campus.water.controller;

import main.java.com.campus.water.service.StudentWaterDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 实时数据查询控制器
 * 负责终端关联的制水机/供水机实时数据查询接口
 */
@RestController
@RequestMapping("/api/water/realtime")
public class WaterRealtimeController {

    @Autowired
    private StudentWaterDataService waterDataService;

    /**
     * 实时查询终端关联的制水机+供水机数据
     * 调用示例：GET http://localhost:8080/api/water/realtime/TERM001
     * @param terminalId 终端ID（路径参数）
     * @return 制水机+供水机实时数据
     */
    @GetMapping("/{terminalId}")
    public Map<String, Object> getRealtimeData(@PathVariable String terminalId) {
        return waterDataService.queryRealtimeData(terminalId);
    }
}