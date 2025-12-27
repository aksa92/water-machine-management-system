package com.campus.water.controller.web;

import com.campus.water.entity.Area;
import com.campus.water.service.AreaService;
import com.campus.water.util.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 区域管理控制器
 * 处理校园/楼宇/区域的增删改查接口请求
 */
@RestController
@RequestMapping("/api/web/area")
@RequiredArgsConstructor
@Tag(name = "区域管理接口", description = "校园、楼宇、区域的层级管理（增删改查）")
public class AreaController {

    private final AreaService areaService;

    /**
     * 新增区域（校园/楼宇/区域）
     * 仅超级管理员可操作
     */
    @PostMapping("/add")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "新增区域", description = "创建校园/楼宇/区域，严格校验层级关联规则")
    public ResponseEntity<ResultVO<Area>> addArea(
            @RequestBody @Parameter(description = "区域信息（名称/类型为必填）") Area area
    ) {
        try {
            Area newArea = areaService.addArea(area);
            return ResponseEntity.ok(ResultVO.success(newArea, "新增区域成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ResultVO.error(500, "新增失败：" + e.getMessage()));
        }
    }

    /**
     * 删除区域
     * 仅超级管理员可操作，需校验无关联管理员和子区域
     */
    @DeleteMapping("/delete/{areaId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "删除区域", description = "删除指定区域，需确保无关联管理员和子区域")
    public ResponseEntity<ResultVO<Void>> deleteArea(
            @PathVariable @Parameter(description = "区域ID") String areaId
    ) {
        try {
            areaService.deleteArea(areaId);
            return ResponseEntity.ok(ResultVO.success(null, "删除区域成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ResultVO.error(500, "删除失败：" + e.getMessage()));
        }
    }

    /**
     * 修改区域信息
     * 仅超级管理员可操作，不允许修改区域类型
     */
    @PutMapping("/update")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "修改区域", description = "更新区域名称/父级/地址/负责人等信息，不允许修改区域类型")
    public ResponseEntity<ResultVO<Area>> updateArea(
            @RequestBody @Parameter(description = "区域信息（areaId为必填）") Area area
    ) {
        try {
            if (area.getAreaId() == null) {
                return ResponseEntity.ok(ResultVO.error(400, "区域ID不能为空"));
            }
            Area updatedArea = areaService.updateArea(area);
            return ResponseEntity.ok(ResultVO.success(updatedArea, "修改区域成功"));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ResultVO.error(500, "修改失败：" + e.getMessage()));
        }
    }

    /**
     * 按ID查询区域详情
     * 超级管理员/区域管理员均可查询
     */
    @GetMapping("/detail/{areaId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')")
    @Operation(summary = "查询区域详情", description = "按ID查询单个区域的完整信息")
    public ResponseEntity<ResultVO<Area>> getAreaDetail(
            @PathVariable @Parameter(description = "区域ID") String areaId
    ) {
        try {
            Area area = areaService.getAreaById(areaId);
            return ResponseEntity.ok(ResultVO.success(area));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ResultVO.error(500, "查询失败：" + e.getMessage()));
        }
    }

    /**
     * 条件查询区域列表
     * 超级管理员/区域管理员均可查询，支持多条件筛选
     */
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')")
    @Operation(summary = "查询区域列表", description = "支持按父级ID/区域类型/名称关键词筛选区域")
    public ResponseEntity<ResultVO<List<Area>>> listAreas(
            @RequestParam(required = false) @Parameter(description = "父级区域ID") String parentAreaId,
            @RequestParam(required = false) @Parameter(description = "区域类型（campus/building/zone）") Area.AreaType areaType,
            @RequestParam(required = false) @Parameter(description = "名称关键词（模糊匹配）") String keyword
    ) {
        try {
            List<Area> areas = areaService.listAreas(parentAreaId, areaType, keyword);
            return ResponseEntity.ok(ResultVO.success(areas));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ResultVO.error(500, "查询失败：" + e.getMessage()));
        }
    }

    /**
     * 查询所有校园（顶级节点）
     * 超级管理员/区域管理员均可查询
     */
    @GetMapping("/list-campus")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')")
    @Operation(summary = "查询所有校园", description = "快速获取所有校园级别的顶级区域")
    public ResponseEntity<ResultVO<List<Area>>> listCampus() {
        try {
            List<Area> campusList = areaService.listAreas(null, Area.AreaType.campus, null);
            return ResponseEntity.ok(ResultVO.success(campusList));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ResultVO.error(500, "查询失败：" + e.getMessage()));
        }
    }

    /**
     * 查询指定校园下的所有楼宇
     * 超级管理员/区域管理员均可查询
     */
    @GetMapping("/list-building/{campusId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')")
    @Operation(summary = "查询校园下的楼宇", description = "按校园ID查询该校园下的所有楼宇")
    public ResponseEntity<ResultVO<List<Area>>> listBuildingByCampus(
            @PathVariable @Parameter(description = "校园ID") String campusId
    ) {
        try {
            List<Area> buildingList = areaService.listAreas(campusId, Area.AreaType.building, null);
            return ResponseEntity.ok(ResultVO.success(buildingList));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ResultVO.error(500, "查询失败：" + e.getMessage()));
        }
    }

    /**
     * 查询指定楼宇下的所有区域
     * 超级管理员/区域管理员均可查询
     */
    @GetMapping("/list-zone/{buildingId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')")
    @Operation(summary = "查询楼宇下的区域", description = "按楼宇ID查询该楼宇下的所有子区域")
    public ResponseEntity<ResultVO<List<Area>>> listZoneByBuilding(
            @PathVariable @Parameter(description = "楼宇ID") String buildingId
    ) {
        try {
            List<Area> zoneList = areaService.listAreas(buildingId, Area.AreaType.zone, null);
            return ResponseEntity.ok(ResultVO.success(zoneList));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(ResultVO.error(500, "查询失败：" + e.getMessage()));
        }
    }
}