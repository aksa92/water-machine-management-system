package main.java.com.campus.water.controller.web;

import main.java.com.campus.water.mapper.RepairerAuthRepository;
import main.java.com.campus.water.mapper.WaterMakerRealtimeDataRepository;
import main.java.com.campus.water.mapper.WaterSupplyRealtimeDataRepository;
import main.java.com.campus.water.service.DeviceService;
import main.java.com.campus.water.mapper.RepairmanRepository;
import main.java.com.campus.water.service.DeviceStatusService;
import main.java.com.campus.water.util.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import main.java.com.campus.water.entity.Device;
import main.java.com.campus.water.entity.RepairerAuth;
import main.java.com.campus.water.entity.Repairman;
import main.java.com.campus.water.entity.WaterMakerRealtimeData;
import main.java.com.campus.water.entity.WaterSupplyRealtimeData;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/web/device")
@RequiredArgsConstructor
@Tag(name = "设备管理接口", description = "Web管理端设备新增与删除接口")
public class DeviceController {

    private final DeviceService deviceService;
    private final DeviceStatusService deviceStatusService;
    private final RepairmanRepository repairmanRepository;
    private final RepairerAuthRepository repairerAuthRepository;
    @Autowired
    private WaterMakerRealtimeDataRepository waterMakerRealtimeDataRepository;

    @Autowired
    private WaterSupplyRealtimeDataRepository waterSupplyRealtimeDataRepository;

    /**
     * 新增设备
     */
    @PostMapping("/add")
    @Operation(summary = "新增设备", description = "添加新设备信息，包括设备ID、名称、类型等")
    public ResponseEntity<ResultVO<Device>> addDevice(@Valid @RequestBody Device device) {
        try {
            Device newDevice = deviceService.addDevice(device);
            return ResponseEntity.ok(ResultVO.success(newDevice, "设备新增成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "设备新增失败: " + e.getMessage()));
        }
    }

    /**
     * 删除设备
     */
    @DeleteMapping("/delete/{deviceId}")
    @Operation(summary = "删除设备", description = "根据设备ID删除指定设备（需先解除终端绑定）")
    public ResponseEntity<ResultVO<Boolean>> deleteDevice(@PathVariable String deviceId) {
        try {
            deviceService.deleteDevice(deviceId);
            return ResponseEntity.ok(ResultVO.success(true, "设备删除成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "设备删除失败: " + e.getMessage()));
        }
    }

    @PostMapping("/relate")
    public ResponseEntity<ResultVO<Void>> relateSupplier(@RequestParam String supplierId, @RequestParam String makerId) {
        try {
            deviceService.relateSupplierToMaker(supplierId, makerId);
            return ResponseEntity.ok(ResultVO.success(null, "关联成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, e.getMessage()));
        }
    }

    // 解除关联
    @PostMapping("/unrelate")
    public ResponseEntity<ResultVO<Void>> unrelateSupplier(@RequestParam String supplierId) {
        try {
            deviceService.unrelateSupplierFromMaker(supplierId);
            return ResponseEntity.ok(ResultVO.success(null, "解除关联成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, e.getMessage()));
        }
    }

    // 查询制水机关联的供水机
    @GetMapping("/maker/{makerId}/suppliers")
    public ResponseEntity<ResultVO<List<Device>>> getSuppliers(@PathVariable String makerId) {
        List<Device> suppliers = deviceService.getSuppliersByMaker(makerId);
        return ResponseEntity.ok(ResultVO.success(suppliers));
    }

    /**
     * 维修人员查询本辖区设备（按类型筛选）
     */
    @GetMapping("/repairman/area-devices-by-type")
    @PreAuthorize("hasRole('REPAIRMAN')")
    @Operation(summary = "维修人员查询辖区设备（按类型）", description = "维修人员查看本辖区内指定类型的设备列表")
    public ResponseEntity<ResultVO<List<Device>>> getAreaDevicesByTypeForRepairman(
            @RequestParam String deviceType,
            Authentication authentication) {
        try {
            // 1. 获取当前登录用户名（username）
            String username = authentication.getName();

            // 2. 通过用户名查询RepairerAuth获取维修人员ID
            RepairerAuth repairerAuth = repairerAuthRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("维修人员认证信息不存在"));
            String repairmanId = repairerAuth.getRepairmanId();

            // 3. 通过维修人员ID查询所属区域
            Repairman repairman = repairmanRepository.findById(repairmanId)
                    .orElseThrow(() -> new RuntimeException("维修人员信息不存在"));
            String areaId = repairman.getAreaId();
            if (areaId == null || areaId.isEmpty()) {
                return ResponseEntity.ok(ResultVO.error(400, "维修人员未分配辖区"));
            }

            // 4. 转换设备类型并查询
            Device.DeviceType type = Device.DeviceType.valueOf(deviceType);
            List<Device> devices = deviceService.queryDevices(areaId, type, null);

            return ResponseEntity.ok(ResultVO.success(devices));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ResultVO.error(400, "无效的设备类型: " + deviceType));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "查询设备失败: " + e.getMessage()));
        }
    }

    @GetMapping("/{deviceId}")
    @Operation(summary = "查询设备详情", description = "根据设备ID获取设备的详细信息及实时数据")
    public ResponseEntity<ResultVO<Map<String, Object>>> getDeviceDetail(@PathVariable String deviceId) {
        try {
            // 1. 获取设备基本信息
            Device device = deviceService.getDeviceById(deviceId);

            // 2. 构建返回结果Map
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("deviceInfo", device);

            // 3. 根据设备类型查询对应实时数据
            if (Device.DeviceType.water_maker.equals(device.getDeviceType())) {
                // 制水机实时数据
                Optional<WaterMakerRealtimeData> realtimeData = waterMakerRealtimeDataRepository.findLatestByDeviceId(deviceId);
                realtimeData.ifPresent(data -> resultMap.put("realtimeData", data));
            } else if (Device.DeviceType.water_supply.equals(device.getDeviceType())) {
                // 供水机实时数据
                Optional<WaterSupplyRealtimeData> realtimeData = waterSupplyRealtimeDataRepository.findLatestByDeviceId(deviceId);
                realtimeData.ifPresent(data -> resultMap.put("realtimeData", data));
            }

            return ResponseEntity.ok(ResultVO.success(resultMap, "设备查询成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "设备查询失败: " + e.getMessage()));
        }
    }

    public DeviceStatusService getDeviceStatusService() {
        return deviceStatusService;
    }

    /**
     * 按状态查询设备列表（支持区域筛选）
     * 管理员/运维人员通用接口
     */
   /* @GetMapping("/by-status")
    @Operation(summary = "按状态查询设备", description = "根据设备状态筛选设备列表，可选区域筛选")
    public ResponseEntity<ResultVO<List<Device>>> getDevicesByStatus(
            @RequestParam String status,
            @RequestParam(required = false) String areaId) {
        try {
            List<Device> devices = deviceStatusService.getDevicesByStatusWithArea(status, areaId);
            return ResponseEntity.ok(ResultVO.success(devices, "按状态查询设备成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ResultVO.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "按状态查询设备失败: " + e.getMessage()));
        }
    }*/

    /**
     * 按类型查询设备列表（支持区域筛选）
     * 管理员/运维人员通用接口
     */
   /* @GetMapping("/by-type")
    @Operation(summary = "按类型查询设备", description = "根据设备类型筛选设备列表，可选区域筛选")
    public ResponseEntity<ResultVO<List<Device>>> getDevicesByType(
            @RequestParam String deviceType,
            @RequestParam(required = false) String areaId) {
        try {
            List<Device> devices = deviceStatusService.getDevicesByTypeWithArea(deviceType, areaId);
            return ResponseEntity.ok(ResultVO.success(devices, "按类型查询设备成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.ok(ResultVO.error(400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "按类型查询设备失败: " + e.getMessage()));
        }
    }*/

    // ========== 新增1：获取所有片区列表（假设从Device表中提取唯一片区ID，若有Area实体可直接查询） ==========
    @GetMapping("/areas")
    @Operation(summary = "获取所有片区列表", description = "返回系统中所有已配置的片区ID和相关信息")
    public ResponseEntity<ResultVO<List<String>>> getAllAreas() {
        try {
            // 从设备表中提取唯一的片区ID（若有独立Area表，可替换为AreaRepository查询）
            List<Device> allDevices = deviceService.queryDevices(null, null, null);
            List<String> areaList = allDevices.stream()
                    .map(Device::getAreaId)
                    .filter(areaId -> areaId != null && !areaId.trim().isEmpty())
                    .distinct()
                    .toList();
            return ResponseEntity.ok(ResultVO.success(areaList, "片区列表查询成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "片区列表查询失败: " + e.getMessage()));
        }
    }

    // ========== 新增2：根据片区ID查询该片区的供水机列表 ==========
    @GetMapping("/area/{areaId}/water-supplies")
    @Operation(summary = "查询片区内供水机", description = "根据片区ID获取该片区下所有可用的供水机")
    public ResponseEntity<ResultVO<List<Device>>> getWaterSuppliesByArea(@PathVariable String areaId) {
        try {
            List<Device> waterSupplies = deviceService.getWaterSuppliesByArea(areaId);
            return ResponseEntity.ok(ResultVO.success(waterSupplies, "片区供水机查询成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "片区供水机查询失败: " + e.getMessage()));
        }
    }

    // ========== 新增：管理员编辑设备基本信息接口 ==========
    @PutMapping("/edit")
    @PreAuthorize("hasAnyRole( 'SUPER_ADMIN','AREA_ADMIN')") // 限制仅管理员/超级管理员可访问
    @Operation(summary = "编辑设备基本信息", description = "管理员更新设备名称、类型、安装位置等基本信息（不含设备状态、创建时间）")
    public ResponseEntity<ResultVO<Device>> editDevice(@Valid @RequestBody Device device) {
        try {
            // 校验设备ID不能为空（编辑必须指定设备ID）
            if (device.getDeviceId() == null || device.getDeviceId().trim().isEmpty()) {
                return ResponseEntity.ok(ResultVO.error(400, "设备ID不能为空"));
            }
            Device updatedDevice = deviceService.updateDeviceInfo(device);
            return ResponseEntity.ok(ResultVO.success(updatedDevice, "设备信息编辑成功"));
        } catch (Exception e) {
            return ResponseEntity.ok(ResultVO.error(500, "设备信息编辑失败: " + e.getMessage()));
        }
    }

}