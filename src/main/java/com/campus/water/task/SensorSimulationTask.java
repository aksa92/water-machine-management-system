package com.campus.water.task;

import com.campus.water.entity.Device;
import com.campus.water.mapper.DeviceRepository;
import com.campus.water.service.MqttSensorSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SensorSimulationTask {
    private final MqttSensorSender mqttSensorSender;
    private final DeviceRepository deviceRepository;

    /**
     * 定时发送「正常状态数据」：每30秒执行一次
     */
    @Scheduled(fixedRate = 30000)
    public void sendRegularStateData() {
        log.info("=== 开始发送设备正常状态数据 ===");

        // 1. 从数据库查询「制水机」（water_maker）类型设备（传入枚举类型）
        List<String> waterMakerDevices = deviceRepository.findByDeviceType(Device.DeviceType.water_maker)
                .stream()
                .map(Device::getDeviceId)
                .collect(Collectors.toList());

        // 2. 从数据库查询「供水机」（water_supply）类型设备（传入枚举类型）
        List<String> waterSupplyDevices = deviceRepository.findByDeviceType(Device.DeviceType.water_supply)
                .stream()
                .map(Device::getDeviceId)
                .collect(Collectors.toList());

        // 发送制水机状态
        if (!waterMakerDevices.isEmpty()) {
            waterMakerDevices.forEach(deviceId -> mqttSensorSender.sendWaterMakerState(deviceId));
        } else {
            log.warn("⚠️ device表中无「water_maker」类型设备");
        }

        // 发送供水机状态
        if (!waterSupplyDevices.isEmpty()) {
            waterSupplyDevices.forEach(deviceId -> mqttSensorSender.sendWaterSupplyData(deviceId));
        } else {
            log.warn("⚠️ device表中无「water_supply」类型设备");
        }

        log.info("=== 设备正常状态数据发送完成 ===");
    }

    /**
     * 定时发送「随机告警数据」：每5分钟执行一次
     */
    @Scheduled(fixedRate = 300000)
    public void sendRandomWarningData() {
        log.info("=== 开始发送随机告警数据 ===");

        // 从数据库查询制水机列表（传入枚举类型）
        List<String> waterMakerDevices = deviceRepository.findByDeviceType(Device.DeviceType.water_maker)
                .stream()
                .map(Device::getDeviceId)
                .collect(Collectors.toList());

        if (waterMakerDevices.isEmpty()) {
            log.warn("⚠️ device表中无「water_maker」类型设备，跳过告警发送");
            return;
        }

        // 随机选择1台制水机发送告警
        int randomIndex = (int) (Math.random() * waterMakerDevices.size());
        String targetDevice = waterMakerDevices.get(randomIndex);
        mqttSensorSender.sendWaterMakerWarning(targetDevice);

        log.info("=== 随机告警数据发送完成 | 告警设备：{} ===", targetDevice);
    }
}