package com.campus.water.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 制水机传感器数据模型（与MQTT消息格式完全对齐）
 * 用于MQTT消息的序列化/反序列化，不直接持久化
 */
@Data
public class WaterMakerSensorData {
    private String deviceId; // 设备唯一ID（如WM001）
    private Double tdsValue1; // TDS值（水质指标）
    private Double tdsValue2;
    private Double tdsValue3;
    private Double waterFlow1; // 水流量（L/min）
    private Double waterFlow2;
    private Double waterPress; // 水压（MPa）
    private Integer filterLife; // 滤芯寿命（%）
    private Boolean leakage; // 是否漏水（true=漏水，false=正常）
    private String waterQuality; // 水质等级（合格/不合格）
    private String status; // 设备状态（normal=正常，error=异常）
    private LocalDateTime recordTime; // 数据采集时间戳
}