package com.campus.water.controller.web;

import com.campus.water.service.DeviceService;
import com.campus.water.service.RepairmanService;
import com.campus.water.service.DeviceStatusService;
import com.campus.water.util.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import com.campus.water.entity.Device;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/web/device")
@RequiredArgsConstructor
@Tag(name = "设备管理接口", description = "Web管理端设备新增与删除接口")
public class DeviceController {

    private final DeviceService deviceService;
    private final DeviceStatusService deviceStatusService;
    private final RepairmanService repairmanService;

    /**
     * 新增设备
     */
    @PostMapping("/add")
    @Operation(summary = "新增设备", description = "添加新设备信息，包括设备ID、名称、类型等")
    public ResultVO<Device> addDevice(@RequestBody Device device) {
        Device newDevice = deviceService.addDevice(device);
        return ResultVO.success(newDevice, "设备新增成功");
    }

    /**
     * 删除设备
     */
    @DeleteMapping("/delete/{deviceId}")
    @Operation(summary = "删除设备", description = "根据设备ID删除指定设备（需先解除终端绑定）")
    public ResultVO<Boolean> deleteDevice(@PathVariable String deviceId) {
        deviceService.deleteDevice(deviceId);
        return ResultVO.success(true, "设备删除成功");
    }

    @PostMapping("/relate")
    public ResultVO<Void> relateSupplier(@RequestParam String supplierId, @RequestParam String makerId) {
        deviceService.relateSupplierToMaker(supplierId, makerId);
        return ResultVO.success(null, "关联成功");
    }

    @PostMapping("/unrelate")
    public ResultVO<Void> unrelateSupplier(@RequestParam String supplierId) {
        deviceService.unrelateSupplierFromMaker(supplierId);
        return ResultVO.success(null, "解除关联成功");
    }

    @GetMapping("/maker/{makerId}/suppliers")
    public ResultVO<List<Device>> getSuppliers(@PathVariable String makerId) {
        List<Device> suppliers = deviceService.getSuppliersByMaker(makerId);
        return ResultVO.success(suppliers);
    }

    /**
     * 维修人员查询本辖区设备（按类型筛选）
     */
    @GetMapping("/repairman/area-devices-by-type")
    @PreAuthorize("hasRole('REPAIRMAN')")
    @Operation(summary = "维修人员查询辖区设备（按类型）", description = "维修人员查看本辖区内指定类型的设备列表")
    public ResultVO<List<Device>> getAreaDevicesByTypeForRepairman(
            @RequestParam String deviceType,
            Authentication authentication) {
        String username = authentication.getName();
        String areaId = repairmanService.getAreaIdByUsername(username);
        if (areaId == null || areaId.isEmpty()) {
            return ResultVO.error(400, "维修人员未分配辖区");
        }
        Device.DeviceType type = Device.DeviceType.valueOf(deviceType);
        List<Device> devices = deviceService.queryDevices(areaId, type, null);
        return ResultVO.success(devices);
    }

    @GetMapping("/{deviceId}")
    @Operation(summary = "查询设备详情", description = "根据设备ID获取设备的详细信息及实时数据")
    public ResultVO<Map<String, Object>> getDeviceDetail(@PathVariable String deviceId) {
        Map<String, Object> detail = deviceService.getDeviceDetail(deviceId);
        return ResultVO.success(detail, "设备查询成功");
    }

    /**
     * 按状态查询设备列表（支持区域筛选）
     */
    @GetMapping("/areas")
    @Operation(summary = "获取所有片区列表", description = "返回系统中所有已配置的片区ID和相关信息")
    public ResultVO<List<String>> getAllAreas() {
        List<Device> allDevices = deviceService.queryDevices(null, null, null);
        List<String> areaList = allDevices.stream()
                .map(Device::getAreaId)
                .filter(areaId -> areaId != null && !areaId.trim().isEmpty())
                .distinct()
                .toList();
        return ResultVO.success(areaList, "片区列表查询成功");
    }

    @GetMapping("/area/{areaId}/water-supplies")
    @Operation(summary = "查询片区内供水机", description = "根据片区ID获取该片区下所有可用的供水机")
    public ResultVO<List<Device>> getWaterSuppliesByArea(@PathVariable String areaId) {
        List<Device> waterSupplies = deviceService.getWaterSuppliesByArea(areaId);
        return ResultVO.success(waterSupplies, "片区供水机查询成功");
    }

    @PutMapping("/edit")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','AREA_ADMIN')")
    @Operation(summary = "编辑设备基本信息", description = "管理员更新设备名称、类型、安装位置等基本信息")
    public ResultVO<Device> editDevice(@RequestBody Device device) {
        if (device.getDeviceId() == null || device.getDeviceId().trim().isEmpty()) {
            return ResultVO.error(400, "设备ID不能为空");
        }
        Device updatedDevice = deviceService.updateDeviceInfo(device);
        return ResultVO.success(updatedDevice, "设备信息编辑成功");
    }
}
