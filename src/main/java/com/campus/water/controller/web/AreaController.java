package com.campus.water.controller.web;

import com.campus.water.entity.Area;
import com.campus.water.service.AreaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 区域管理控制器
 * 不使用 ResultVO，直接通过 ResponseEntity 返回响应
 * 适配 Area 实体（areaId 主键、市区-校园层级）
 */
@RestController
@RequestMapping("/api/web/area") // 统一管理端接口前缀：/api/web
@CrossOrigin // 允许跨域（前端调用时需要）
public class AreaController {

    private final AreaService areaService;

    // 构造器注入
    public AreaController(AreaService areaService) {
        this.areaService = areaService;
    }

    /**
     * 构建通用响应体
     * @param code 响应码（200成功，400参数/业务异常，500系统异常）
     * @param msg 响应消息
     * @param data 响应数据
     * @return 封装后的Map响应体
     */
    private Map<String, Object> buildResponse(int code, String msg, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", code);
        response.put("msg", msg);
        response.put("data", data);
        return response;
    }

    /**
     * 新增区域（市区/校园）
     * 权限：超级管理员/区域管理员可操作
     */
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')") // 补充权限注解
    public ResponseEntity<Map<String, Object>> addArea(@RequestBody Area area) {
        try {
            Area savedArea = areaService.addArea(area);
            return ResponseEntity.ok(buildResponse(200, "新增成功", savedArea));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(buildResponse(400, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse(500, "新增区域失败：" + e.getMessage(), null));
        }
    }

    /**
     * 修改区域
     * 权限：超级管理员/区域管理员可操作
     */
    @PutMapping("/update/{areaId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN')") // 补充权限注解
    public ResponseEntity<Map<String, Object>> updateArea(@PathVariable String areaId, @RequestBody Area area) {
        try {
            Area updatedArea = areaService.updateArea(areaId, area);
            return ResponseEntity.ok(buildResponse(200, "修改成功", updatedArea));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(buildResponse(400, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse(500, "修改区域失败：" + e.getMessage(), null));
        }
    }

    /**
     * 删除区域
     * 权限：仅超级管理员可操作
     */
    @DeleteMapping("/delete/{areaId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')") // 补充权限注解（删除权限更严格）
    public ResponseEntity<Map<String, Object>> deleteArea(@PathVariable String areaId) {
        try {
            areaService.deleteArea(areaId);
            return ResponseEntity.ok(buildResponse(200, "删除成功", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(buildResponse(400, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse(500, "删除区域失败：" + e.getMessage(), null));
        }
    }

    /**
     * 查询所有市区（根节点）
     * 权限：超级管理员/区域管理员/维修人员均可查看（根据你项目实际需求调整）
     */
    @GetMapping("/cities")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN', 'REPAIRMAN')") // 补充权限注解
    public ResponseEntity<Map<String, Object>> getAllCities() {
        try {
            List<Area> cities = areaService.getAllCities();
            return ResponseEntity.ok(buildResponse(200, "查询成功", cities));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse(500, "查询市区列表失败：" + e.getMessage(), null));
        }
    }

    /**
     * 根据市区ID查询下属校园
     * 权限：超级管理员/区域管理员/维修人员均可查看
     */
    @GetMapping("/campuses/{cityId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN', 'REPAIRMAN')") // 补充权限注解
    public ResponseEntity<Map<String, Object>> getCampusesByCityId(@PathVariable String cityId) {
        try {
            List<Area> campuses = areaService.getCampusesByCityId(cityId);
            return ResponseEntity.ok(buildResponse(200, "查询成功", campuses));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(buildResponse(400, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse(500, "查询校园列表失败：" + e.getMessage(), null));
        }
    }

    /**
     * 根据区域ID查询单个区域信息
     * 权限：超级管理员/区域管理员/维修人员均可查看
     */
    @GetMapping("/{areaId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'AREA_ADMIN', 'REPAIRMAN')") // 补充权限注解
    public ResponseEntity<Map<String, Object>> getAreaById(@PathVariable String areaId) {
        try {
            Area area = areaService.getAreaById(areaId);
            return ResponseEntity.ok(buildResponse(200, "查询成功", area));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(buildResponse(400, e.getMessage(), null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse(500, "查询区域详情失败：" + e.getMessage(), null));
        }
    }
}