package com.campus.water.controller.web;

import com.campus.water.entity.Device;
import com.campus.water.service.DeviceService;
import com.campus.water.util.ResultVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;  // 新增List的导入语句

@RestController
@RequestMapping("/api/web/device")
@RequiredArgsConstructor
@Tag(name = "设备管理接口", description = "Web管理端设备新增与删除接口")
public class DeviceController {

    private final DeviceService deviceService;

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
}