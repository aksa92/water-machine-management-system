package main.java.com.campus.water.mapper;

import main.java.com.campus.water.entity.WaterTerminalLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 终端机位置数据访问层（截图原命名，删除isAvailable相关查询）
 */
@Repository
public interface WaterTerminalLocationRepository extends JpaRepository<WaterTerminalLocation, String> {
    // 原findByIsAvailable方法删除（因实体已无该字段，筛选逻辑移至Service层）
}