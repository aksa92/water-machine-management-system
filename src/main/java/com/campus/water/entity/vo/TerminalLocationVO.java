package main.java.com.campus.water.entity.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 终端位置VO（截图原命名，字段完全对齐）
 */
@Data
public class TerminalLocationVO {
    private String terminalId;
    private String terminalName;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private Boolean isAvailable;
    private String deviceStatus;
}