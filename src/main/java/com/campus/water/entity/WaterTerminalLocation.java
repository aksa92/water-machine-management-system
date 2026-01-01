package main.java.com.campus.water.entity;

import java.math.BigDecimal;
import lombok.Data;
import jakarta.persistence.*;

/**
 * 矿化水终端机位置实体（仅存储地图坐标，无冗余字段）
 * 专用于学生端地图标记，终端名称/可用状态从映射表关联查询
 */
@Data
@Entity
@Table(name = "water_terminal_location")
public class WaterTerminalLocation {
    /**
     * 终端机ID（主键，与映射表terminal_id关联，统一长度20）
     */
    @Id
    @Column(name = "terminal_id", length = 20, nullable = false)
    private String terminalId;

    /**
     * 经度（高德GCJ-02坐标系）
     */
    @Column(name = "longitude", nullable = false, columnDefinition = "DECIMAL(10,6)")
    private BigDecimal longitude;

    /**
     * 纬度（高德GCJ-02坐标系）
     */
    @Column(name = "latitude", nullable = false, columnDefinition = "DECIMAL(10,6)")
    private BigDecimal latitude;
}