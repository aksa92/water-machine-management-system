package com.campus.water.repository;

import com.campus.water.entity.DeviceTerminalMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTerminalMappingRepository extends JpaRepository<DeviceTerminalMapping, Integer> {
    // 根据终端ID查找映射关系
    Optional<DeviceTerminalMapping> findByTerminalId(String terminalId);

    // 根据设备ID查找所有关联终端
    List<DeviceTerminalMapping> findByDeviceId(String deviceId);

    // 根据终端状态查找映射关系
    List<DeviceTerminalMapping> findByTerminalStatus(DeviceTerminalMapping.TerminalStatus status);

    // 根据设备和终端ID精确查找映射
    Optional<DeviceTerminalMapping> findByDeviceIdAndTerminalId(String deviceId, String terminalId);
}