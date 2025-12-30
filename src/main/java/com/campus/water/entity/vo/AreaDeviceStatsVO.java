package com.campus.water.entity.vo;

import lombok.Data;

/**
 * 片区设备统计VO（适配现有实体，无新增依赖）
 */
@Data
public class AreaDeviceStatsVO {
    private String areaId; // 对应Area.areaId
    private String areaName; // 对应Area.areaName
    private String areaTypeDesc; // 对应Area.AreaType.desc（市区/校园）
    private int waterMakerCount; // 制水机数量
    private int waterSupplyCount; // 供水机数量
    private int terminalCount; // 终端数量（基于DeviceTerminalMapping）
}