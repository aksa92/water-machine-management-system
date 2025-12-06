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
}