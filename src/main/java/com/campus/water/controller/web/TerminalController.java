// java/com/campus/water/controller/web/TerminalController.java
package main.java.com.campus.water.controller.web;

import main.java.com.campus.water.service.DeviceService;
import main.java.com.campus.water.service.TerminalService;
import main.java.com.campus.water.entity.vo.TerminalManageVO;
import main.java.com.campus.water.util.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/web/terminal")
@RequiredArgsConstructor
@Tag(name = "终端管理接口", description = "管理员基于设备终端映射表/终端位置表的增删改查操作")
public class TerminalController {

    private final TerminalService terminalService;


    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')")
    @Operation(summary = "新增终端", description = "同时保存终端位置、基础映射信息和片区信息")
    public ResponseEntity<ResultVO<TerminalManageVO>> addTerminal(@Valid @RequestBody TerminalManageVO terminalVO) {
        try {
            TerminalManageVO newTerminal = terminalService.addTerminal(terminalVO);
            return ResponseEntity.ok(ResultVO.success(newTerminal, "终端新增成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "终端新增失败: " + e.getMessage()));
        }
    }

    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')")
    @Operation(summary = "更新终端", description = "支持更新终端名称、状态、经纬度、片区等信息")
    public ResponseEntity<ResultVO<TerminalManageVO>> updateTerminal(@Valid @RequestBody TerminalManageVO terminalVO) {
        try {
            TerminalManageVO updated = terminalService.updateTerminal(terminalVO);
            return ResponseEntity.ok(ResultVO.success(updated, "终端更新成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "终端更新失败: " + e.getMessage()));
        }
    }

    /**
     * 删除终端
     * 先校验设备绑定状态，再级联删除终端位置表和映射表的相关数据
     */
    @DeleteMapping("/delete/{terminalId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')")
    @Operation(summary = "删除终端", description = "先校验设备绑定状态，再级联删除相关数据")
    public ResponseEntity<ResultVO<String>> deleteTerminal(@PathVariable String terminalId) {
        try {
            // 调用服务层删除终端
            terminalService.deleteTerminal(terminalId);
            // 用ResultVO封装成功结果，返回提示信息
            return ResponseEntity.ok(ResultVO.success("终端删除成功"));
        } catch (Exception e) {
            // 用ResultVO封装错误结果，携带异常信息
            return ResponseEntity.ok(ResultVO.error(500, "终端删除失败: " + e.getMessage()));
        }
    }

    /**
     * 查询终端详情
     * 根据终端ID获取整合后的完整信息（包含位置、映射、片区areaId信息）
     */
    @GetMapping("/{terminalId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')")
    @Operation(summary = "查询终端详情", description = "根据终端ID获取整合后的完整信息")
    public ResponseEntity<ResultVO<TerminalManageVO>> getTerminal(@PathVariable String terminalId) {
        try {
            // 调用服务层查询终端详情，返回包含areaId的VO
            TerminalManageVO terminal = terminalService.getTerminalById(terminalId);
            // 用ResultVO封装成功结果，自定义提示信息
            return ResponseEntity.ok(ResultVO.success(terminal, "终端查询成功"));
        } catch (Exception e) {
            // 用ResultVO封装404错误结果，携带异常信息
            return ResponseEntity.ok(ResultVO.notFound("终端查询失败: " + e.getMessage()));
        }
    }


    /**
     * 查询终端列表
     * 支持按终端名称模糊筛选，返回整合后的完整列表（每条数据均包含areaId）
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')")
    @Operation(summary = "查询终端列表", description = "支持按终端名称模糊筛选")
    public ResponseEntity<ResultVO<List<TerminalManageVO>>> getTerminalList(
            @RequestParam(required = false) String terminalName) {
        try {
            // 调用服务层查询终端列表，返回包含areaId的VO列表
            List<TerminalManageVO> terminalList = terminalService.getTerminalList(terminalName);
            // 用ResultVO封装成功结果，自定义提示信息
            return ResponseEntity.ok(ResultVO.success(terminalList, "终端列表查询成功"));
        } catch (Exception e) {
            // 用ResultVO封装错误结果，携带异常信息
            return ResponseEntity.ok(ResultVO.error(500, "终端列表查询失败: " + e.getMessage()));
        }
    }


}