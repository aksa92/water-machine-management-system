package com.campus.water.repository;

import com.campus.water.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {
    List<Device> findByAreaId(String areaId);
    List<Device> findByDeviceType(Device.DeviceType deviceType);
    List<Device> findByStatus(Device.DeviceStatus status);

    // 修复：确保areaId字段存在
    List<Device> findByAreaIdAndDeviceType(String areaId, Device.DeviceType deviceType);

    List<Device> findByInstallLocationContaining(String location);
}