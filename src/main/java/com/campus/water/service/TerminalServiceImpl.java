// java/com/campus/water/service/impl/TerminalServiceImpl.java
package com.campus.water.service;

import com.campus.water.entity.DeviceTerminalMapping;
import com.campus.water.entity.WaterTerminalLocation;
import com.campus.water.mapper.DeviceTerminalMappingRepository;
import com.campus.water.mapper.WaterTerminalLocationRepository;
import com.campus.water.service.TerminalService;
import com.campus.water.entity.vo.TerminalManageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TerminalServiceImpl implements TerminalService {

    private final WaterTerminalLocationRepository locationRepository;
    private final DeviceTerminalMappingRepository mappingRepository;
    // ========== 新增：注入DeviceService，用于片区和供水机校验 ==========
    private final DeviceService deviceService;

    @Override
    @Transactional
    public TerminalManageVO addTerminal(TerminalManageVO terminalVO) {
        // 1. 校验终端ID是否已存在（通过位置表主键判断，避免重复新增）
        if (locationRepository.existsById(terminalVO.getTerminalId())) {
            throw new RuntimeException("终端ID已存在，无法重复新增：" + terminalVO.getTerminalId());
        }

        // 2. 保存终端位置信息（经纬度为必填项，校验非空）
        if (terminalVO.getLongitude() == null || terminalVO.getLatitude() == null) {
            throw new RuntimeException("终端经度和纬度为必填项，不可为空");
        }
        // ========== 新增：校验片区ID非空 ==========
        if (terminalVO.getAreaId() == null || terminalVO.getAreaId().trim().isEmpty()) {
            throw new RuntimeException("片区ID不能为空，请先选择片区");
        }
        // ========== 新增：若传递了deviceId，校验供水机是否属于该片区 ==========
        if (terminalVO.getDeviceId() != null && !terminalVO.getDeviceId().trim().isEmpty()) {
            deviceService.validateDeviceBelongsToArea(terminalVO.getDeviceId(), terminalVO.getAreaId());
        }
        WaterTerminalLocation location = new WaterTerminalLocation();
        location.setTerminalId(terminalVO.getTerminalId());
        location.setLongitude(terminalVO.getLongitude());
        location.setLatitude(terminalVO.getLatitude());
        locationRepository.save(location);

        // 3. 保存终端映射信息（状态默认active，复用原有Repository的save方法）
        DeviceTerminalMapping mapping = new DeviceTerminalMapping();
        mapping.setTerminalId(terminalVO.getTerminalId());
        mapping.setTerminalName(terminalVO.getTerminalName());
        mapping.setTerminalStatus(terminalVO.getTerminalStatus() == null
                ? DeviceTerminalMapping.TerminalStatus.active
                : terminalVO.getTerminalStatus());
        mapping.setDeviceId(terminalVO.getDeviceId());
        mapping.setInstallDate(terminalVO.getInstallDate());
        mapping.setAreaId(terminalVO.getAreaId()); // 保存片区ID
        mappingRepository.save(mapping);

        // 4. 封装返回结果
        return assembleTerminalVO(location, mapping);
    }

    @Override
    @Transactional
    public TerminalManageVO updateTerminal(TerminalManageVO terminalVO) {
        // 1. 校验终端是否存在（通过位置表查询，复用原有findById方法）
        WaterTerminalLocation existingLocation = locationRepository.findById(terminalVO.getTerminalId())
                .orElseThrow(() -> new RuntimeException("终端不存在，无法更新：" + terminalVO.getTerminalId()));
        // ========== 新增：若更新片区/设备，校验供水机与片区的关联 ==========
        if (terminalVO.getAreaId() != null && !terminalVO.getAreaId().trim().isEmpty()) {
            if (terminalVO.getDeviceId() != null && !terminalVO.getDeviceId().trim().isEmpty()) {
                deviceService.validateDeviceBelongsToArea(terminalVO.getDeviceId(), terminalVO.getAreaId());
            }
        }
        // 2. 更新终端位置信息（仅更新有值字段，复用原有save方法）
        if (terminalVO.getLongitude() != null) {
            existingLocation.setLongitude(terminalVO.getLongitude());
        }
        if (terminalVO.getLatitude() != null) {
            existingLocation.setLatitude(terminalVO.getLatitude());
        }
        locationRepository.save(existingLocation);

        // 3. 更新终端映射信息（复用原有findByTerminalId方法查询映射记录）
        DeviceTerminalMapping existingMapping = mappingRepository.findByTerminalId(terminalVO.getTerminalId())
                .orElseThrow(() -> new RuntimeException("终端无关联映射信息，无法更新终端名称/状态"));

        if (terminalVO.getTerminalName() != null && !terminalVO.getTerminalName().isEmpty()) {
            existingMapping.setTerminalName(terminalVO.getTerminalName());
        }
        if (terminalVO.getTerminalStatus() != null) {
            existingMapping.setTerminalStatus(terminalVO.getTerminalStatus());
        }
        if (terminalVO.getDeviceId() != null) {
            existingMapping.setDeviceId(terminalVO.getDeviceId());
        }
        if (terminalVO.getInstallDate() != null) {
            existingMapping.setInstallDate(terminalVO.getInstallDate());
        }
        if (terminalVO.getAreaId() != null && !terminalVO.getAreaId().trim().isEmpty()) {
            existingMapping.setAreaId(terminalVO.getAreaId()); // 更新片区ID
        }
        mappingRepository.save(existingMapping);

        // 4. 封装返回结果
        return assembleTerminalVO(existingLocation, existingMapping);
    }

    @Override
@Transactional
public void deleteTerminal(String terminalId) {
    // 1. 检查终端映射记录是否存在
    Optional<DeviceTerminalMapping> mappingOpt = mappingRepository.findByTerminalId(terminalId);

    if (mappingOpt.isPresent()) {
        // 检查是否实际绑定了设备（deviceId不为null）
        DeviceTerminalMapping mapping = mappingOpt.get();
        if (mapping.getDeviceId() != null && !mapping.getDeviceId().isEmpty()) {
            throw new RuntimeException("终端已绑定设备，无法删除，请先解除设备关联");
        }
    }

    // 2. 校验终端是否存在
    if (!locationRepository.existsById(terminalId)) {
        throw new RuntimeException("终端不存在，无需删除：" + terminalId);
    }

    // 3. 级联删除数据
    mappingRepository.deleteByTerminalId(terminalId);
    locationRepository.deleteById(terminalId);
}


    @Override
    public TerminalManageVO getTerminalById(String terminalId) {
        // 1. 查询位置信息（复用原有findById方法）
        WaterTerminalLocation location = locationRepository.findById(terminalId)
                .orElseThrow(() -> new RuntimeException("终端不存在：" + terminalId));

        // 2. 查询映射信息（复用原有findByTerminalId方法）
        DeviceTerminalMapping mapping = mappingRepository.findByTerminalId(terminalId)
                .orElseThrow(() -> new RuntimeException("终端无关联基础信息，请补充映射记录"));

        // 3. 封装返回结果
        return assembleTerminalVO(location, mapping);
    }

    @Override
    public List<TerminalManageVO> getTerminalList(String terminalName) {
        // 1. 查询映射表数据（支持名称模糊筛选，使用新增的findByTerminalNameContaining方法）
        List<DeviceTerminalMapping> mappings;
        if (terminalName != null && !terminalName.isEmpty()) {
            mappings = mappingRepository.findByTerminalNameContaining(terminalName);
        } else {
            mappings = mappingRepository.findAll(); // 复用原有查询所有方法
        }

        // 2. 遍历映射记录，关联位置表数据，封装VO列表
        List<TerminalManageVO> terminalVOList = new ArrayList<>();
        for (DeviceTerminalMapping mapping : mappings) {
            Optional<WaterTerminalLocation> locationOpt = locationRepository.findById(mapping.getTerminalId());
            locationOpt.ifPresent(location -> {
                TerminalManageVO vo = assembleTerminalVO(location, mapping);
                terminalVOList.add(vo);
            });
        }
        return terminalVOList;
    }

    /**
     * 辅助方法：整合位置表和映射表数据，封装为TerminalManageVO
     */
    private TerminalManageVO assembleTerminalVO(WaterTerminalLocation location, DeviceTerminalMapping mapping) {
        TerminalManageVO vo = new TerminalManageVO();
        vo.setTerminalId(location.getTerminalId());
        vo.setLongitude(location.getLongitude());
        vo.setLatitude(location.getLatitude());
        vo.setTerminalName(mapping.getTerminalName());
        vo.setTerminalStatus(mapping.getTerminalStatus());
        vo.setDeviceId(mapping.getDeviceId());
        vo.setInstallDate(mapping.getInstallDate());
        vo.setAreaId(mapping.getAreaId()); // 封装片区ID返回给前端
        return vo;
    }
}