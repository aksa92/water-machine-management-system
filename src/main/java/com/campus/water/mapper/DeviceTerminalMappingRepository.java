package com.campus.water.mapper;

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

    // ========== 新增必要方法（支撑终端增删改查业务） ==========
    // 1. 判断终端是否已绑定设备（删除终端时的核心校验）
    boolean existsByTerminalId(String terminalId);

    // 2. 按终端名称模糊查询（终端列表筛选）
    List<DeviceTerminalMapping> findByTerminalNameContaining(String terminalName);

    // 3. 按终端ID删除所有关联映射（删除终端时级联清理映射数据）
    void deleteByTerminalId(String terminalId);



}