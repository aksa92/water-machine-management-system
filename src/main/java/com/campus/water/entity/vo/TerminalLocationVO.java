package com.campus.water.entity.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TerminalLocationVO {
    private String terminalId;
    private String terminalName;
    private BigDecimal longitude; // 同步改为BigDecimal
    private BigDecimal latitude;  // 同步改为BigDecimal
    private Boolean isAvailable;
    private String installLocation;
    private String deviceStatus;
}