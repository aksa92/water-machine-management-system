package com.campus.water.service.impl;

import com.campus.water.entity.Device;
import com.campus.water.entity.WaterTerminalLocation;
import com.campus.water.entity.vo.TerminalLocationVO;
import com.campus.water.mapper.DeviceRepository;
import com.campus.water.mapper.WaterTerminalLocationRepository;
import com.campus.water.service.WaterTerminalLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 终端机位置服务实现（关联查询device表，避免数据冗余）
 */
@Service
@RequiredArgsConstructor
public class WaterTerminalLocationServiceImpl implements WaterTerminalLocationService {

    private final WaterTerminalLocationRepository locationRepository;
    private final DeviceRepository deviceRepository; // 注入设备表Mapper

    /**
     * 获取所有终端机位置（整合device表数据）
     */
    @Override
    public List<TerminalLocationVO> getAllTerminalLocations() {
        List<WaterTerminalLocation> locationList = locationRepository.findAll();
        return convertToVO(locationList);
    }

    /**
     * 获取可用的终端机位置
     */
    @Override
    public List<TerminalLocationVO> getAvailableTerminalLocations() {
        List<WaterTerminalLocation> locationList = locationRepository.findByIsAvailable(true);
        return convertToVO(locationList);
    }

    /**
     * 核心转换方法：坐标表+设备表 → VO（前端展示专用）
     */
    private List<TerminalLocationVO> convertToVO(List<WaterTerminalLocation> locationList) {
        List<TerminalLocationVO> voList = new ArrayList<>();
        for (WaterTerminalLocation location : locationList) {
            TerminalLocationVO vo = new TerminalLocationVO();
            // 1. 赋值坐标表核心字段
            vo.setTerminalId(location.getTerminalId());
            vo.setTerminalName(location.getTerminalName());
            vo.setLongitude(location.getLongitude());
            vo.setLatitude(location.getLatitude());
            vo.setIsAvailable(location.getIsAvailable());

            // 2. 关联查询device表，补充安装位置、设备状态
            Optional<Device> deviceOptional = deviceRepository.findById(location.getTerminalId());
            if (deviceOptional.isPresent()) {
                Device device = deviceOptional.get();
                vo.setInstallLocation(device.getInstallLocation()); // 复用install_location
                vo.setDeviceStatus(device.getStatus().name());
            } else {
                vo.setInstallLocation("未配置安装位置");
                vo.setDeviceStatus("unknown");
            }
            voList.add(vo);
        }
        return voList;
    }
}