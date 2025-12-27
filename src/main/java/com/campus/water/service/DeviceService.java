package com.campus.water.service;

import com.campus.water.entity.Device;
import com.campus.water.entity.DeviceTerminalMapping;
import com.campus.water.entity.Device.DeviceStatus;
import com.campus.water.entity.Device.DeviceType;
import com.campus.water.entity.DeviceTerminalMapping.TerminalStatus;
import com.campus.water.mapper.DeviceRepository;
import com.campus.water.mapper.DeviceTerminalMappingRepository;
import com.campus.water.util.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 设备管理服务类
 * 处理设备的CRUD、状态更新、终端关联等核心业务逻辑
 */
@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final DeviceTerminalMappingRepository terminalMappingRepository;

    /**
     * 根据设备ID查询设备详情
     */
    public Device getDeviceById(String deviceId) {
        return deviceRepository.findById(deviceId)
                .orElseThrow(() -> new RuntimeException("设备不存在：" + deviceId));
    }



    /**
     * 新增设备
     */
    @Transactional
    public Device addDevice(Device device) {
        // 检查设备ID是否已存在
        if (deviceRepository.existsById(device.getDeviceId())) {
            throw new RuntimeException("设备ID已存在：" + device.getDeviceId());
        }
        device.setCreateTime(LocalDateTime.now());
        device.setStatus(DeviceStatus.online); // 默认为在线状态
        return deviceRepository.save(device);
    }

    /**
     * 更新设备基本信息（不含状态）
     */
    @Transactional
    public Device updateDeviceInfo(Device device) {
        Device existingDevice = getDeviceById(device.getDeviceId());
        // 保留创建时间，更新其他可编辑字段
        existingDevice.setDeviceName(device.getDeviceName());
        existingDevice.setDeviceType(device.getDeviceType());
        existingDevice.setAreaId(device.getAreaId());
        existingDevice.setInstallLocation(device.getInstallLocation());
        existingDevice.setInstallDate(device.getInstallDate());
        return deviceRepository.save(existingDevice);
    }

    /**
     * 更新设备状态（在线/离线/故障）
     */
    @Transactional
    public boolean updateDeviceStatus(String deviceId, DeviceStatus status) {
        Device device = getDeviceById(deviceId);
        device.setStatus(status);
        deviceRepository.save(device);
        return true;
    }

    /**
     * 批量更新设备状态
     */
    @Transactional
    public boolean batchUpdateStatus(List<String> deviceIds, DeviceStatus status) {
        List<Device> devices = deviceRepository.findAllById(deviceIds);
        if (devices.size() != deviceIds.size()) {
            throw new RuntimeException("部分设备ID不存在");
        }
        devices.forEach(device -> device.setStatus(status));
        deviceRepository.saveAll(devices);
        return true;
    }

    /**
     * 根据条件查询设备列表
     */
    public List<Device> queryDevices(String areaId, DeviceType deviceType, DeviceStatus status) {
        if (areaId != null && deviceType != null) {
            return deviceRepository.findByAreaIdAndDeviceType(areaId, deviceType);
        } else if (areaId != null) {
            return deviceRepository.findByAreaId(areaId);
        } else if (deviceType != null) {
            return deviceRepository.findByDeviceType(deviceType);
        } else if (status != null) {
            return deviceRepository.findByStatus(status);
        } else {
            return deviceRepository.findAll();
        }
    }

    /**
     * 关联设备与终端
     */
    @Transactional
    public DeviceTerminalMapping bindTerminal(String deviceId, String terminalId, String terminalName) {
        // 校验设备是否存在
        getDeviceById(deviceId);

        // 检查终端是否已绑定
        Optional<DeviceTerminalMapping> existing = terminalMappingRepository.findByTerminalId(terminalId);
        if (existing.isPresent()) {
            throw new RuntimeException("终端已绑定设备：" + existing.get().getDeviceId());
        }

        DeviceTerminalMapping mapping = new DeviceTerminalMapping();
        mapping.setDeviceId(deviceId);
        mapping.setTerminalId(terminalId);
        mapping.setTerminalName(terminalName);
        mapping.setTerminalStatus(TerminalStatus.active);
        mapping.setInstallDate(java.time.LocalDate.now());
        return terminalMappingRepository.save(mapping);
    }

    /**
     * 查询设备关联的终端列表
     */
    public List<DeviceTerminalMapping> getBoundTerminals(String deviceId) {
        getDeviceById(deviceId); // 校验设备存在性
        return terminalMappingRepository.findByDeviceId(deviceId);
    }

    /**
     * 统计各状态设备数量
     */
    public long countByStatus(DeviceStatus status) {
        return deviceRepository.findByStatus(status).size();
    }
    /**
     * 删除设备（需先校验是否已解绑终端）
     */
    @Transactional
    public void deleteDevice(String deviceId) {
        // 1. 校验设备是否存在
        Device device = getDeviceById(deviceId);

        // 2. 检查设备是否已绑定终端
        List<DeviceTerminalMapping> boundTerminals = terminalMappingRepository.findByDeviceId(deviceId);
        if (!boundTerminals.isEmpty()) {
            // 收集已绑定的终端ID，便于前端提示
            String terminalIds = boundTerminals.stream()
                    .map(DeviceTerminalMapping::getTerminalId)
                    .collect(Collectors.joining(","));
            throw new RuntimeException("设备已绑定终端，无法删除（终端ID：" + terminalIds + "）");
        }

        // 3. 执行删除操作
        deviceRepository.delete(device);
    }

    // 新增：关联供水机到制水机
    @Transactional
    public void relateSupplierToMaker(String supplierId, String makerId) {
        // 校验制水机是否存在且类型正确
        Device maker = deviceRepository.findById(makerId)
                .orElseThrow(() -> new RuntimeException("制水机不存在：" + makerId));
        if (maker.getDeviceType() != DeviceType.water_maker) {
            throw new RuntimeException("目标设备不是制水机：" + makerId);
        }

        // 校验供水机是否存在且类型正确
        Device supplier = deviceRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("供水机不存在：" + supplierId));
        if (supplier.getDeviceType() != DeviceType.water_supply) {
            throw new RuntimeException("目标设备不是供水机：" + supplierId);
        }

        // 检查供水机是否已关联其他制水机
        if (supplier.getParentMakerId() != null) {
            throw new RuntimeException("供水机已关联制水机：" + supplier.getParentMakerId());
        }

        // 建立关联
        supplier.setParentMakerId(makerId);
        deviceRepository.save(supplier);
    }

    // 新增：解除供水机与制水机的关联
    @Transactional
    public void unrelateSupplierFromMaker(String supplierId) {
        Device supplier = deviceRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("供水机不存在：" + supplierId));
        if (supplier.getDeviceType() != DeviceType.water_supply) {
            throw new RuntimeException("目标设备不是供水机：" + supplierId);
        }
        supplier.setParentMakerId(null);
        deviceRepository.save(supplier);
    }

    // 新增：查询制水机关联的所有供水机
    public List<Device> getSuppliersByMaker(String makerId) {
        return deviceRepository.findByParentMakerIdAndDeviceType(makerId, DeviceType.water_supply);
    }

    // 新增：查询供水机所属的制水机
    public Device getMakerBySupplier(String supplierId) {
        Device supplier = deviceRepository.findById(supplierId)
                .orElseThrow(() -> new RuntimeException("供水机不存在：" + supplierId));
        if (supplier.getParentMakerId() == null) {
            return null;
        }
        return deviceRepository.findById(supplier.getParentMakerId()).orElse(null);
    }



}