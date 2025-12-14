package com.campus.water.controller.web;

import com.campus.water.entity.Device;
import com.campus.water.entity.RepairerAuth;
import com.campus.water.mapper.RepairerAuthRepository;
import com.campus.water.service.DeviceService;
import com.campus.water.entity.Repairman;
import com.campus.water.mapper.RepairmanRepository;
import com.campus.water.util.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;  // 新增List的导入语句

@RestController
@RequestMapping("/api/web/device")
@RequiredArgsConstructor
@Tag(name = "设备管理接口", description = "Web管理端设备新增与删除接口")
public class DeviceController {

    private final DeviceService deviceService;
    private final RepairmanRepository repairmanRepository;
    private final RepairerAuthRepository repairerAuthRepository;

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
    // 在DeviceController.java中修改getAreaDevicesByTypeForRepairman方法
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
}