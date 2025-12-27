package com.campus.water.controller.web;

import com.campus.water.entity.Area;
import com.campus.water.service.AreaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/area")
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
     * @param area 区域信息（JSON格式）
     * @return 新增后的区域对象
     */
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addArea(@RequestBody Area area) {
        try {
            Area savedArea = areaService.addArea(area);
            return ResponseEntity.ok(buildResponse(200, "新增成功", savedArea));
        } catch (RuntimeException e) {
            // 业务异常：400状态码 + 具体提示
            return ResponseEntity.badRequest().body(buildResponse(400, e.getMessage(), null));
        } catch (Exception e) {
            // 系统异常：500状态码 + 通用提示
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(buildResponse(500, "新增区域失败：" + e.getMessage(), null));
        }
    }

    /**
     * 修改区域
     * @param areaId 区域ID
     * @param area 待修改的区域信息
     * @return 修改后的区域对象
     */
    @PutMapping("/update/{areaId}")
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
     * @param areaId 区域ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{areaId}")
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
     * @return 市区列表
     */
    @GetMapping("/cities")
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
     * @param cityId 市区ID（areaId）
     * @return 该市区下的校园列表
     */
    @GetMapping("/campuses/{cityId}")
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
     * （扩展接口：方便前端回显详情）
     * @param areaId 区域ID
     * @return 区域详情
     */
    @GetMapping("/{areaId}")
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