// com/campus/water/entity/dto/request/StatisticsQueryRequest.java
package main.java.com.campus.water.entity.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data // Lombok注解自动生成getter/setter
public class StatisticsQueryRequest {
    private String statType; // 统计类型（如by_device、by_area等）
    private LocalDate startDate; // 开始日期
    private LocalDate endDate; // 结束日期
    private String areaId; // 区域ID
    private String deviceType; // 设备类型
    private String terminalId; // 补充终端ID字段（解决getTerminalId()错误）
    private Integer limit; // 限制数量
    // 若有其他字段可继续补充
}