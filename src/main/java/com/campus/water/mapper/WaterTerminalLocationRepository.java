package com.campus.water.mapper;

import com.campus.water.entity.WaterTerminalLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 终端机位置数据访问层（仅操作坐标相关数据）
 */
@Repository
public interface WaterTerminalLocationRepository extends JpaRepository<WaterTerminalLocation, String> {
    /**
     * 查询可用的终端机位置（地图优先展示可用设备）
     */
    List<WaterTerminalLocation> findByIsAvailable(Boolean isAvailable);
}