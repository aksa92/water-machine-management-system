package main.java.com.campus.water.controller;

import main.java.com.campus.water.entity.vo.TerminalLocationVO;
import main.java.com.campus.water.service.WaterTerminalLocationService;
import main.java.com.campus.water.util.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学生端终端位置控制器（截图原命名，修正Terminallocation拼写错误）
 */
@RestController
@RequestMapping("/api/student/terminal/location")
@RequiredArgsConstructor
@Tag(name = "学生端-终端位置接口", description = "饮水机地图位置查询")
public class StudentTerminalLocationController {

    private final WaterTerminalLocationService waterTerminalLocationService; // 截图原命名注入

    @GetMapping("/all")
    @Operation(summary = "获取所有终端位置")
    public ResultVO<List<TerminalLocationVO>> getAllLocations() {
        List<TerminalLocationVO> list = waterTerminalLocationService.getAllTerminalLocations();
        return ResultVO.success(list, "获取所有终端位置成功");
    }

    @GetMapping("/available")
    @Operation(summary = "获取可用终端位置")
    public ResultVO<List<TerminalLocationVO>> getAvailableLocations() {
        List<TerminalLocationVO> list = waterTerminalLocationService.getAvailableTerminalLocations();
        return ResultVO.success(list, "获取可用终端位置成功");
    }
}