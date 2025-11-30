package com.campus.water.mapper;

import com.campus.water.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {
    // 根据区域ID查询设备
    List<Device> findByAreaId(String areaId);

    // 根据设备类型查询
    List<Device> findByDeviceType(Device.DeviceType deviceType);

    // 根据设备状态查询
    List<Device> findByStatus(Device.DeviceStatus status);

    // 按区域和设备类型查询设备
    List<Device> findByAreaIdAndDeviceType(String areaId, Device.DeviceType deviceType);

    // 按安装位置模糊查询设备
    List<Device> findByInstallLocationContaining(String location);
}