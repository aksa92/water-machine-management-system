package com.campus.water.mapper;

import com.campus.water.entity.DrinkRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

/**
 * 用水量统计数据访问接口
 * 提供基于JPA的数据查询方法，用于按设备、区域、时间统计用水量
 */
@Repository
public interface StatisticsRepository extends JpaRepository<DrinkRecord, Long> {

    /**
     * 按设备统计指定时间范围内的用水量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 包含设备ID和对应总用水量的数组列表
     */
    @Query("SELECT d.deviceId, SUM(d.waterConsumption) FROM DrinkRecord d " +
            "WHERE d.drinkTime BETWEEN ?1 AND ?2 " +
            "GROUP BY d.deviceId ORDER BY SUM(d.waterConsumption) DESC")
    List<Object[]> getWaterUsageByDevice(LocalDate startDate, LocalDate endDate);

    /**
     * 按区域统计指定时间范围内的用水量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 包含区域ID和对应总用水量的数组列表
     */
    @Query("SELECT d.areaId, SUM(r.waterConsumption) FROM Device d " +
            "JOIN DrinkRecord r ON d.deviceId = r.deviceId " +
            "WHERE r.drinkTime BETWEEN ?1 AND ?2 " +
            "GROUP BY d.areaId")
    List<Object[]> getWaterUsageByArea(LocalDate startDate, LocalDate endDate);

    /**
     * 按日期统计指定时间范围内的用水量
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 包含日期和对应总用水量的数组列表
     */
    @Query("SELECT DATE(r.drinkTime), SUM(r.waterConsumption) FROM DrinkRecord r " +
            "WHERE r.drinkTime BETWEEN ?1 AND ?2 " +
            "GROUP BY DATE(r.drinkTime) ORDER BY DATE(r.drinkTime)")
    List<Object[]> getDailyWaterUsage(LocalDate startDate, LocalDate endDate);
}