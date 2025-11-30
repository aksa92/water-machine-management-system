package com.campus.water.repository;

import com.campus.water.entity.DeviceTerminalMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTerminalMappingRepository extends JpaRepository<DeviceTerminalMapping, Integer> {
    Optional<DeviceTerminalMapping> findByTerminalId(String terminalId);
    List<DeviceTerminalMapping> findByDeviceId(String deviceId);
    List<DeviceTerminalMapping> findByTerminalStatus(DeviceTerminalMapping.TerminalStatus status);
    Optional<DeviceTerminalMapping> findByDeviceIdAndTerminalId(String deviceId, String terminalId);
}