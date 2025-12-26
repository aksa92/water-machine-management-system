package com.campus.water.service;

import com.campus.water.entity.DeviceTerminalMapping;
import com.campus.water.mapper.DeviceTerminalMappingRepository;
import com.campus.water.util.DeviceMappingUtil; // 硬编码映射工具类（之前定义的）
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class StudentWaterDataService {

    @Autowired
    private DeviceTerminalMappingRepository terminalMappingRepo;

    // 随机数工具（模拟实时数据）
    private static final Random RANDOM = new Random();
    // 时间格式化器
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 核心：实时查询终端关联的制水机+供水机数据
     * @param terminalId 终端ID（如TERM001）
     * @return 包含制水机、供水机实时数据的完整结果
     */
    public Map<String, Object> queryRealtimeData(String terminalId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 1. 校验终端ID非空
            if (terminalId == null || terminalId.trim().isEmpty()) {
                result.put("code", 400);
                result.put("msg", "终端ID不能为空");
                return result;
            }

            // 2. 通过终端ID查询映射关系（核心：终端→制水机）
            Optional<DeviceTerminalMapping> mappingOpt = terminalMappingRepo.findByTerminalId(terminalId);
            if (mappingOpt.isEmpty()) {
                result.put("code", 404);
                result.put("msg", "终端设备不存在或未配置映射关系");
                return result;
            }
            DeviceTerminalMapping mapping = mappingOpt.get();
            String makerDeviceId = mapping.getDeviceId(); // 制水机ID（WM开头）
            // 从硬编码映射工具类获取供水机ID（替代数据库字段）
            String supplyDeviceId = DeviceMappingUtil.getSupplyDeviceId(makerDeviceId);

            // 3. 生成制水机实时数据（调用模拟生成服务）
            Map<String, Object> makerRealtimeData = new HashMap<>();
            if (makerDeviceId != null && makerDeviceId.startsWith("WM")) {
                makerRealtimeData = generateMakerRealtimeData(makerDeviceId);
            } else {
                result.put("makerWarn", "终端未绑定有效制水机（ID格式需以WM开头）");
            }

            // 4. 生成供水机实时数据（调用模拟生成服务）
            Map<String, Object> supplyRealtimeData = new HashMap<>();
            if (supplyDeviceId != null && supplyDeviceId.startsWith("WS")) {
                supplyRealtimeData = generateSupplyRealtimeData(supplyDeviceId);
            } else {
                result.put("supplyWarn", "制水机未关联有效供水机（ID格式需以WS开头）");
            }

            // 5. 封装最终返回结果（包含终端、制水机、供水机全量数据）
            result.put("code", 200);
            result.put("msg", "实时数据查询成功");
            // 终端基础信息
            result.put("terminalInfo", Map.of(
                    "terminalId", terminalId,
                    "terminalName", mapping.getTerminalName(),
                    "terminalStatus", mapping.getTerminalStatus().name(),
                    "installDate", mapping.getInstallDate() != null ? mapping.getInstallDate().toString() : "未配置"
            ));
            // 制水机数据
            result.put("makerDevice", Map.of(
                    "deviceId", makerDeviceId,
                    "realtimeData", makerRealtimeData
            ));
            // 供水机数据
            result.put("supplyDevice", Map.of(
                    "deviceId", supplyDeviceId,
                    "realtimeData", supplyRealtimeData
            ));
            // 数据更新时间（统一时间戳）
            result.put("updateTime", LocalDateTime.now().format(DATE_FORMATTER));

        } catch (Exception e) {
            // 全局异常捕获，保证接口不抛错
            result.put("code", 500);
            result.put("msg", "实时数据查询失败：" + e.getMessage());
            result.put("errorDetail", e.getStackTrace()[0].toString()); // 调试用，生产可移除
        }
        return result;
    }

    /**
     * 模拟生成制水机实时数据（独立服务方法，可单独调用）
     * @param makerDeviceId 制水机ID
     * @return 制水机实时数据
     */
    public Map<String, Object> generateMakerRealtimeData(String makerDeviceId) {
        Map<String, Object> makerData = new HashMap<>();
        // 基础设备信息
        makerData.put("deviceId", makerDeviceId);
        makerData.put("deviceType", "校园直饮矿化制水机");
        makerData.put("onlineStatus", RANDOM.nextBoolean() ? "在线" : "离线"); // 模拟在线状态
        // 核心水质参数（符合直饮水标准）
        makerData.put("tdsValue", RANDOM.nextInt(50) + 10); // TDS值：10-60mg/L
        makerData.put("phValue", String.format("%.1f", RANDOM.nextDouble() * 0.8 + 7.0)); // pH值：7.0-7.8
        makerData.put("temperature", RANDOM.nextInt(15) + 30); // 出水温度：30-45℃
        makerData.put("filterLife", 100 - RANDOM.nextInt(10)); // 滤芯寿命：90-100%
        makerData.put("flowRate", String.format("%.2f", RANDOM.nextDouble() * 2 + 1)); // 流量：1.00-3.00L/min
        // 运行数据
        makerData.put("totalUsage", RANDOM.nextInt(5000) + 10000); // 累计用水量：10000-15000L
        makerData.put("faultCode", RANDOM.nextInt(100) > 95 ? "E01" : "无"); // 模拟故障码（5%概率故障）
        makerData.put("updateTime", LocalDateTime.now().format(DATE_FORMATTER));
        return makerData;
    }

    /**
     * 模拟生成供水机实时数据（独立服务方法，可单独调用）
     * @param supplyDeviceId 供水机ID
     * @return 供水机实时数据
     */
    public Map<String, Object> generateSupplyRealtimeData(String supplyDeviceId) {
        Map<String, Object> supplyData = new HashMap<>();
        // 基础设备信息
        supplyData.put("deviceId", supplyDeviceId);
        supplyData.put("deviceType", "配套供水增压机");
        supplyData.put("onlineStatus", RANDOM.nextBoolean() ? "在线" : "离线");
        // 核心运行参数
        supplyData.put("waterPressure", String.format("%.2f", RANDOM.nextDouble() * 0.5 + 0.8)); // 水压：0.80-1.30MPa
        supplyData.put("waterLevel", RANDOM.nextInt(30) + 70); // 水箱水位：70-100%
        supplyData.put("pumpStatus", RANDOM.nextBoolean() ? "运行中" : "待机"); // 水泵状态
        supplyData.put("voltage", RANDOM.nextInt(10) + 220); // 工作电压：220-230V
        // 运行数据
        supplyData.put("runHours", RANDOM.nextInt(1000) + 5000); // 累计运行时长：5000-6000h
        supplyData.put("faultCode", RANDOM.nextInt(100) > 98 ? "P02" : "无"); // 模拟故障码（2%概率故障）
        supplyData.put("updateTime", LocalDateTime.now().format(DATE_FORMATTER));
        return supplyData;
    }

    // 原有其他方法（扫码用水、水质查询等）保持不变...
}