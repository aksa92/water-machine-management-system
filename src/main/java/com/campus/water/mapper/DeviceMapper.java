package com.campus.water.mapper;

import com.campus.water.entity.Device;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 设备数据访问接口（MyBatis Mapper）
 * 功能：定义设备相关的数据库操作方法，包括设备信息CRUD和状态管理
 * 用途：为设备管理提供数据访问支持，支持设备状态查询、更新、统计等操作
 * 包含方法：
 *   - 设备基本信息CRUD操作
 *   - 设备状态管理相关操作
 *   - 设备统计和查询操作
 * 对应XML：DeviceMapper.xml中的SQL实现
 */
@Mapper
public interface DeviceMapper {

    // ========== 设备基本信息CRUD操作 ==========

    /**
     * 根据设备ID查询设备信息
     * @param deviceId 设备ID
     * @return 设备实体对象
     */
    Device findById(@Param("deviceId") String deviceId);

    /**
     * 查询所有设备
     * @return 设备列表
     */
    List<Device> findAll();

    /**
     * 根据设备名称模糊查询
     * @param deviceName 设备名称（模糊匹配）
     * @return 匹配的设备列表
     */
    List<Device> findByDeviceNameLike(@Param("deviceName") String deviceName);

    /**
     * 新增设备
     * @param device 设备实体
     * @return 影响的行数
     */
    int insert(Device device);

    /**
     * 更新设备信息
     * @param device 设备实体
     * @return 影响的行数
     */
    int update(Device device);

    /**
     * 删除设备
     * @param deviceId 设备ID
     * @return 影响的行数
     */
    int delete(@Param("deviceId") String deviceId);

    // ========== 设备状态管理相关操作（新增） ==========

    /**
     * 更新设备状态
     * @param deviceId 设备ID
     * @param status 目标状态（online/offline/fault/maintenance）
     * @param remark 状态变更备注
     * @return 影响的行数
     */
    int updateDeviceStatus(@Param("deviceId") String deviceId,
                           @Param("status") String status,
                           @Param("remark") String remark);

    /**
     * 标记设备为在线状态
     * @param deviceId 设备ID
     * @return 影响的行数
     */
    int markDeviceOnline(@Param("deviceId") String deviceId);

    /**
     * 标记设备为离线状态
     * @param deviceId 设备ID
     * @param reason 离线原因
     * @return 影响的行数
     */
    int markDeviceOffline(@Param("deviceId") String deviceId,
                          @Param("reason") String reason);

    /**
     * 标记设备为故障状态
     * @param deviceId 设备ID
     * @param faultType 故障类型
     * @param description 故障描述
     * @return 影响的行数
     */
    int markDeviceFault(@Param("deviceId") String deviceId,
                        @Param("faultType") String faultType,
                        @Param("description") String description);

    /**
     * 批量更新设备状态
     * @param deviceIds 设备ID列表
     * @param status 目标状态
     * @param remark 状态变更备注
     * @return 影响的行数
     */
    int batchUpdateDeviceStatus(@Param("deviceIds") List<String> deviceIds,
                                @Param("status") String status,
                                @Param("remark") String remark);

    // ========== 设备统计和查询操作 ==========

    /**
     * 根据状态查询设备
     * @param status 设备状态（online/offline/fault/maintenance）
     * @param areaId 区域ID（可选）
     * @param deviceType 设备类型（water_maker/water_supply）（可选）
     * @return 设备列表
     */
    List<Device> findByStatus(@Param("status") String status,
                              @Param("areaId") String areaId,
                              @Param("deviceType") String deviceType);

    /**
     * 统计各状态设备数量
     * @param areaId 区域ID（可选）
     * @param deviceType 设备类型（可选）
     * @return 状态统计列表，每个元素包含status和count
     */
    List<Map<String, Object>> countByStatus(@Param("areaId") String areaId,
                                            @Param("deviceType") String deviceType);

    /**
     * 根据区域ID查询设备
     * @param areaId 区域ID
     * @return 设备列表
     */
    List<Device> findByAreaId(@Param("areaId") String areaId);

    /**
     * 根据设备类型查询设备
     * @param deviceType 设备类型（water_maker/water_supply）
     * @return 设备列表
     */
    List<Device> findByDeviceType(@Param("deviceType") String deviceType);

    /**
     * 根据安装位置模糊查询设备
     * @param location 安装位置关键词
     * @return 设备列表
     */
    List<Device> findByInstallLocationContaining(@Param("location") String location);

    /**
     * 查询安装日期在指定范围内的设备
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 设备列表
     */
    List<Device> findByInstallDateBetween(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    // ========== 设备监控相关操作 ==========

    /**
     * 获取设备最后在线时间
     * @param deviceId 设备ID
     * @return 设备信息（包含updated_time）
     */
    Device getDeviceLastOnlineTime(@Param("deviceId") String deviceId);

    /**
     * 查询离线时间超过阈值的设备
     * @param thresholdMinutes 离线阈值（分钟）
     * @param areaId 区域ID（可选）
     * @return 离线设备列表
     */
    List<Device> findOfflineDevicesExceedThreshold(@Param("thresholdMinutes") Integer thresholdMinutes,
                                                   @Param("areaId") String areaId);

    /**
     * 更新设备最后通信时间
     * @param deviceId 设备ID
     * @return 影响的行数
     */
    int updateLastCommunicationTime(@Param("deviceId") String deviceId);

    /**
     * 查询需要维护的设备（根据维护计划）
     * @param currentDate 当前日期
     * @return 需要维护的设备列表
     */
    List<Device> findDevicesNeedMaintenance(@Param("currentDate") LocalDate currentDate);

    // ========== 设备统计报表相关操作 ==========

    /**
     * 统计各区域设备数量
     * @return 区域设备统计列表，每个元素包含areaId, areaName, deviceCount
     */
    List<Map<String, Object>> countDevicesByArea();

    /**
     * 统计各类型设备数量
     * @return 类型设备统计列表，每个元素包含deviceType, deviceCount
     */
    List<Map<String, Object>> countDevicesByType();

    /**
     * 统计设备在线率（按区域）
     * @return 在线率统计列表，每个元素包含areaId, areaName, totalDevices, onlineDevices, onlineRate
     */
    List<Map<String, Object>> getOnlineRateByArea();

    /**
     * 查询设备运行时长统计
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 设备运行时长列表，每个元素包含deviceId, deviceName, totalOnlineHours
     */
    List<Map<String, Object>> getDeviceRuntimeStats(@Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    // ========== 批量操作 ==========

    /**
     * 批量插入设备
     * @param devices 设备列表
     * @return 影响的行数
     */
    int batchInsert(@Param("devices") List<Device> devices);

    /**
     * 批量更新设备信息
     * @param devices 设备列表
     * @return 影响的行数
     */
    int batchUpdate(@Param("devices") List<Device> devices);

    // ========== 设备关联查询 ==========

    /**
     * 根据设备ID查询关联的终端设备
     * @param deviceId 设备ID
     * @return 终端映射列表
     */
    List<Map<String, Object>> findTerminalsByDeviceId(@Param("deviceId") String deviceId);

    /**
     * 根据设备ID查询最近的告警记录
     * @param deviceId 设备ID
     * @param limit 限制条数
     * @return 告警记录列表
     */
    List<Map<String, Object>> findRecentAlertsByDeviceId(@Param("deviceId") String deviceId,
                                                         @Param("limit") Integer limit);

    /**
     * 根据设备ID查询最近的维修记录
     * @param deviceId 设备ID
     * @param limit 限制条数
     * @return 工单记录列表
     */
    List<Map<String, Object>> findRecentWorkOrdersByDeviceId(@Param("deviceId") String deviceId,
                                                             @Param("limit") Integer limit);

    // ========== 设备高级搜索 ==========

    /**
     * 多条件组合查询设备
     * @param deviceName 设备名称（可选）
     * @param deviceType 设备类型（可选）
     * @param status 设备状态（可选）
     * @param areaId 区域ID（可选）
     * @param startDate 安装开始日期（可选）
     * @param endDate 安装结束日期（可选）
     * @return 设备列表
     */
    List<Device> searchDevices(@Param("deviceName") String deviceName,
                               @Param("deviceType") String deviceType,
                               @Param("status") String status,
                               @Param("areaId") String areaId,
                               @Param("startDate") LocalDate startDate,
                               @Param("endDate") LocalDate endDate);

    // ========== 设备导出相关 ==========

    /**
     * 获取设备导出数据（用于Excel导出）
     * @param conditions 查询条件
     * @return 设备导出数据列表
     */
    List<Map<String, Object>> getDeviceExportData(@Param("conditions") Map<String, Object> conditions);
}