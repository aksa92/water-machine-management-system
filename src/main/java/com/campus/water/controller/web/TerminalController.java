// java/com/campus/water/controller/web/TerminalController.java
package com.campus.water.controller.web;

import com.campus.water.service.TerminalService;
import com.campus.water.entity.vo.TerminalManageVO;
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
    @Operation(summary = "新增终端", description = "同时保存终端位置和基础映射信息")
    // 泛型改为?，兼容TerminalManageVO和Map
    public ResponseEntity<?> addTerminal(@Valid @RequestBody TerminalManageVO terminalVO) {
        try {
            TerminalManageVO newTerminal = terminalService.addTerminal(terminalVO);
            return new ResponseEntity<>(newTerminal, HttpStatus.CREATED);
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("message", "终端新增失败: " + e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/update")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')")
    @Operation(summary = "更新终端", description = "支持更新终端名称、状态、经纬度等信息")
    // 泛型改为?，兼容TerminalManageVO和Map
    public ResponseEntity<?> updateTerminal(@Valid @RequestBody TerminalManageVO terminalVO) {
        try {
            TerminalManageVO updated = terminalService.updateTerminal(terminalVO);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("message", "终端更新失败: " + e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/delete/{terminalId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')")
    @Operation(summary = "删除终端", description = "先校验设备绑定状态，再级联删除相关数据")
    // 该方法成功/失败均返回Map，泛型可保留Map<String, String>（无冲突）
    public ResponseEntity<Map<String, String>> deleteTerminal(@PathVariable String terminalId) {
        try {
            terminalService.deleteTerminal(terminalId);
            Map<String, String> successMap = new HashMap<>();
            successMap.put("message", "终端删除成功");
            return ResponseEntity.ok(successMap);
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("message", "终端删除失败: " + e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{terminalId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')")
    @Operation(summary = "查询终端详情", description = "根据终端ID获取整合后的完整信息")
    // 泛型改为?，兼容TerminalManageVO和Map
    public ResponseEntity<?> getTerminal(@PathVariable String terminalId) {
        try {
            TerminalManageVO terminal = terminalService.getTerminalById(terminalId);
            return ResponseEntity.ok(terminal);
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("message", "终端查询失败: " + e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')")
    @Operation(summary = "查询终端列表", description = "支持按终端名称模糊筛选")
    // 泛型改为?，兼容List<TerminalManageVO>和Map
    public ResponseEntity<?> getTerminalList(
            @RequestParam(required = false) String terminalName) {
        try {
            List<TerminalManageVO> terminals = terminalService.getTerminalList(terminalName);
            return ResponseEntity.ok(terminals);
        } catch (Exception e) {
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("message", "终端列表查询失败: " + e.getMessage());
            return new ResponseEntity<>(errorMap, HttpStatus.BAD_REQUEST);
        }
    }
}