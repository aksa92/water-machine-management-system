// java/com/campus/water/vo/TerminalManageVO.java
package main.java.com.campus.water.entity.vo;

import main.java.com.campus.water.entity.DeviceTerminalMapping;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 终端管理VO（整合位置表和映射表的核心信息）
 */
@Data
public class TerminalManageVO {
    // 终端核心标识（关联两张表的主键/外键）
    private String terminalId;

    // 终端名称（来自映射表）
    private String terminalName;

    // 终端经纬度（来自位置表）
    private BigDecimal longitude;
    private BigDecimal latitude;

    // 终端状态（来自映射表）
    private DeviceTerminalMapping.TerminalStatus terminalStatus;

    // 安装日期（来自映射表）
    private LocalDate installDate;

    // 设备ID（关联的设备，来自映射表）
    private String deviceId;

    // ========== 新增：片区ID字段（前端传递选中的片区ID） ==========
    private String areaId;
}