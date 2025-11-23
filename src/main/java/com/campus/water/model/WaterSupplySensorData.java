package com.campus.water.model;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 供水机传感器数据模型（与MQTT消息格式完全对齐）
 * 用于MQTT消息的序列化/反序列化，不直接持久化
 */
@Data
public class WaterSupplySensorData {
    private String deviceId; // 设备唯一ID（如WS001）
    private Double waterFlow; // 水流量（L/min）
    private Double waterPressure; // 水压（MPa）
    private Double waterLevel; // 水位（%）
    private Double temperature; // 水温（℃）
    private Double humidity; // 环境湿度（%RH）
    private String status; // 设备状态（normal=正常，error=异常）
    private LocalDateTime timestamp; // 数据采集时间戳
}