// com/campus/water/entity/vo/AlarmStatisticsVO.java
package com.campus.water.entity.vo;

import lombok.Data;
import java.util.Map;

@Data
public class AlarmStatisticsVO {
    private Map<String, Long> levelCount; // 告警级别统计（解决setLevelCount()错误）
    private Map<String, Long> statusCount; // 告警状态统计（解决setStatusCount()错误）
    private double handleRate; // 处理率（解决setHandleRate()错误）
    private long totalAlarms; // 总告警数
}