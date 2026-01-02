package com.campus.water.service;

import com.campus.water.entity.DeviceTerminalMapping;
import com.campus.water.entity.WaterTerminalLocation;
import com.campus.water.entity.vo.TerminalLocationVO;
import com.campus.water.mapper.DeviceTerminalMappingRepository;
import com.campus.water.mapper.WaterTerminalLocationRepository;
import com.campus.water.service.WaterTerminalLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 终端位置服务实现类（截图原命名）
 */
@Service
@RequiredArgsConstructor
public class WaterTerminalLocationServiceImpl implements WaterTerminalLocationService {

    private final WaterTerminalLocationRepository waterTerminalLocationRepository; // 截图原命名注入
    private final DeviceTerminalMappingRepository deviceTerminalMappingRepository;

    /**
     * 获取所有终端位置（关联映射表补充名称/状态）
     */
    @Override
    public List<TerminalLocationVO> getAllTerminalLocations() {
        List<WaterTerminalLocation> locationList = waterTerminalLocationRepository.findAll();
        return convertToVO(locationList);
    }

    /**
     * 获取可用终端位置（筛选映射表active状态）
     */
    @Override
    public List<TerminalLocationVO> getAvailableTerminalLocations() {
        List<WaterTerminalLocation> locationList = waterTerminalLocationRepository.findAll();
        return convertToVO(locationList).stream()
                .filter(TerminalLocationVO::getIsAvailable)
                .toList();
    }

    /**
     * 转换为VO（截图原逻辑，适配字段调整）
     */
    private List<TerminalLocationVO> convertToVO(List<WaterTerminalLocation> locationList) {
        List<TerminalLocationVO> voList = new ArrayList<>();
        for (WaterTerminalLocation location : locationList) {
            TerminalLocationVO vo = new TerminalLocationVO();
            // 位置表核心字段
            vo.setTerminalId(location.getTerminalId());
            vo.setLongitude(location.getLongitude());
            vo.setLatitude(location.getLatitude());

            // 关联映射表获取名称/状态（替代原isAvailable字段）
            Optional<DeviceTerminalMapping> mappingOpt = deviceTerminalMappingRepository.findByTerminalId(location.getTerminalId());
            if (mappingOpt.isPresent()) {
                DeviceTerminalMapping mapping = mappingOpt.get();
                vo.setTerminalName(mapping.getTerminalName());
                vo.setDeviceStatus(mapping.getTerminalStatus().name());
                vo.setIsAvailable(DeviceTerminalMapping.TerminalStatus.active.equals(mapping.getTerminalStatus()));
            } else {
                // 默认值（截图原逻辑）
                vo.setTerminalName("未配置终端");
                vo.setDeviceStatus("inactive");
                vo.setIsAvailable(false);
            }
            voList.add(vo);
        }
        return voList;
    }
}