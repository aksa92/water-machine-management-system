package com.campus.water.entity.vo;

import lombok.Data;

/**
 * 终端机位置VO（整合坐标表+设备表数据，前端地图展示专用）
 */
@Data
public class TerminalLocationVO {
    /** 终端机ID */
    private String terminalId;

    /** 终端机名称 */
    private String terminalName;

    /** 经度 */
    private Double longitude;

    /** 纬度 */
    private Double latitude;

    /** 是否可用 */
    private Boolean isAvailable;

    /** 安装位置（复用device表的install_location） */
    private String installLocation;

    /** 设备状态（online/offline/fault） */
    private String deviceStatus;
}