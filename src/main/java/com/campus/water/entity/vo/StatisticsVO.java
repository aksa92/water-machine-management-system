// com/campus/water/entity/vo/StatisticsVO.java
package main.java.com.campus.water.entity.vo;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data // Lombok注解自动生成getter/setter
public class StatisticsVO {
    private String type; // 统计类型（解决setType()错误）
    private String areaId; // 区域ID（解决setAreaId()错误）
    private List<String> dates; // 日期列表（解决setDates()错误）
    private List<Double> waterUsage; // 用水量列表（解决setWaterUsage()错误）
    private double totalUsage; // 总用水量（解决setTotalUsage()错误）
    private double avgDailyUsage; // 日均用水量（解决setAvgDailyUsage()错误）
    private List<Map<String, Object>> deviceStats; // 设备统计详情
    // 其他需要的字段
}