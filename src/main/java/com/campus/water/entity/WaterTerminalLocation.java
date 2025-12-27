package com.campus.water.entity;
import java.math.BigDecimal;
import lombok.Data;
import jakarta.persistence.*;

/**
 * 矿化水终端机位置实体（仅存储地图核心数据，复用device表非坐标字段）
 * 专用于学生端查看饮水机位置、地图标记场景
 */
@Data
@Entity
@Table(name = "water_terminal_location")
public class WaterTerminalLocation {
    /**
     * 终端机ID（主键，与device表的device_id完全关联）
     */
    @Id
    @Column(name = "terminal_id", length = 50, nullable = false)
    private String terminalId;

    /**
     * 终端机名称（复用device表的device_name，仅存储冗余字段便于快速查询）
     */
    @Column(name = "terminal_name", length = 100, nullable = false)
    private String terminalName;

    /**
     * 经度（高德GCJ-02坐标系，改用DECIMAL类型支持精度配置）
     */
    @Column(name = "longitude", nullable = false, columnDefinition = "DECIMAL(10,6)")
    private BigDecimal longitude;

    /**
     * 纬度（高德GCJ-02坐标系，改用DECIMAL类型支持精度配置）
     */
    @Column(name = "latitude", nullable = false, columnDefinition = "DECIMAL(10,6)")
    private BigDecimal latitude;

    /**
     * 是否可用（关联device表的status字段：online→true，offline/fault→false）
     */
    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = true;
}