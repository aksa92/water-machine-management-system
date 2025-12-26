package com.campus.water.controller;

import com.campus.water.entity.vo.TerminalLocationVO;
import com.campus.water.service.WaterTerminalLocationService;
import com.campus.water.util.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 学生端-饮水机位置查询控制器（返回整合后的数据）
 */
@RestController
@RequestMapping("/api/student/terminal/location")
@RequiredArgsConstructor
@Tag(name = "学生端-饮水机位置接口", description = "查询校内矿化水终端机位置信息")
public class StudentTerminalLocationController {

    private final WaterTerminalLocationService locationService;

    /**
     * 获取所有终端机位置（整合安装位置、状态）
     */
    @GetMapping("/all")
    @Operation(summary = "获取所有终端机位置", description = "查询校内所有矿化水终端机的坐标、安装位置、状态")
    public ResultVO<List<TerminalLocationVO>> getAllLocations() {
        try {
            List<TerminalLocationVO> locations = locationService.getAllTerminalLocations();
            return ResultVO.success(locations, "获取所有终端机位置成功");
        } catch (Exception e) {
            return ResultVO.error(500, "获取位置失败：" + e.getMessage());
        }
    }

    /**
     * 获取可用的终端机位置（仅正常运行的设备）
     */
    @GetMapping("/available")
    @Operation(summary = "获取可用终端机位置", description = "仅查询状态正常的矿化水终端机位置")
    public ResultVO<List<TerminalLocationVO>> getAvailableLocations() {
        try {
            List<TerminalLocationVO> locations = locationService.getAvailableTerminalLocations();
            return ResultVO.success(locations, "获取可用终端机位置成功");
        } catch (Exception e) {
            return ResultVO.error(500, "获取可用位置失败：" + e.getMessage());
        }
    }
}