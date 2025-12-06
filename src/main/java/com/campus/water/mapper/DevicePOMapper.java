package com.campus.water.mapper;

import com.campus.water.entity.po.DevicePO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Repository
// 注意：DevicePO的主键类型是Long（id字段），因此这里泛型第二参数改为Long
public interface DevicePOMapper extends JpaRepository<DevicePO, Long> {

    // 根据设备唯一标识（deviceId）查询（注意区分主键id和业务字段deviceId）
    DevicePO findByDeviceId(String deviceId);

    // 根据状态字符串查询（参数类型为String，匹配DevicePO的status字段）
    List<DevicePO> findByStatus(String status);

    // 更新设备状态（状态参数类型改为String）
    @Modifying
    @Transactional
    @Query("UPDATE DevicePO d SET d.status = ?2, d.lastActiveTime = ?3 WHERE d.deviceId = ?1")
    void updateDeviceStatus(String deviceId, String status, LocalDateTime lastActiveTime);

    // 补充常用查询：按区域ID查询设备
    List<DevicePO> findByAreaId(String areaId);

    // 补充：按区域和状态查询
    List<DevicePO> findByAreaIdAndStatus(String areaId, String status);
}