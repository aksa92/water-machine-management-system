// com/campus/water/mapper/DeviceRepository.java
package com.campus.water.Repository;

import com.campus.water.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DeviceRepository extends JpaRepository<Device, String> {
    // 根据区域名称查询设备
    List<Device> findByAreaId(String areaId);

    // 根据设备类型（枚举）查询
    List<Device> findByDeviceType(Device.DeviceType deviceType);

    // 根据设备状态（枚举）查询
    List<Device> findByStatus(Device.DeviceStatus status);

    // 按区域和设备类型查询
    List<Device> findByAreaIdAndDeviceType(String areaId, Device.DeviceType deviceType);

    // 按安装位置模糊查询
    List<Device> findByInstallLocationContaining(String location);

    // 关键修正：参数类型为枚举（原DevicePO用String，现统一为Device的枚举）
    List<Device> findByStatusAndAreaIdAndDeviceType(
            Device.DeviceStatus status,
            String areaId,
            Device.DeviceType deviceType
    );

    // 统计方法参数修正为枚举
    long countByStatusAndAreaIdAndDeviceType(
            Device.DeviceStatus status,
            String areaId,
            Device.DeviceType deviceType
    );

    List<Device> findByAreaIdAndStatus(String areaId, Device.DeviceStatus deviceStatus);

    // 根据制水机ID查询关联的供水机
    List<Device> findByParentMakerIdAndDeviceType(String parentMakerId, Device.DeviceType deviceType);


    // 按状态加载设备（支持区域筛选）
    List<Device> findByStatusAndAreaId(Device.DeviceStatus status, String areaId);

    // 按设备类型加载加载设备（支持区域筛选）
    List<Device> findByDeviceTypeAndAreaId(Device.DeviceType deviceType, String areaId);


}