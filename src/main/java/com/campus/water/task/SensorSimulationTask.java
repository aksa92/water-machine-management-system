package com.campus.water.task;

import com.campus.water.entity.Device;
import com.campus.water.Repository.DeviceRepository;
import com.campus.water.service.MqttSensorSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class SensorSimulationTask {
    private final MqttSensorSender mqttSensorSender;
    private final DeviceRepository deviceRepository;
    private final Random random = new Random(); // 新增随机数生成器

    /**
     * 定时发送「正常状态数据」：每30分钟执行一次
     */
    @Scheduled(fixedRate = 1800000) // 30分钟 = 30*60*1000 = 1800000毫秒
    public void sendRegularStateData() {
        log.info("=== 开始发送设备正常状态数据 ===");

        // 1. 查询制水机设备
        List<String> waterMakerDevices = deviceRepository.findByDeviceType(Device.DeviceType.water_maker)
                .stream()
                .map(Device::getDeviceId)
                .collect(Collectors.toList());

        // 2. 分批次发送制水机数据（每批间隔1-3秒）
        if (!waterMakerDevices.isEmpty()) {
            sendInBatches(waterMakerDevices, true);
        } else {
            log.warn("⚠️ device表中无「water_maker」类型设备");
        }

        // 3. 查询供水机设备
        List<String> waterSupplyDevices = deviceRepository.findByDeviceType(Device.DeviceType.water_supply)
                .stream()
                .map(Device::getDeviceId)
                .collect(Collectors.toList());

        // 4. 分批次发送供水机数据（每批间隔1-3秒）
        if (!waterSupplyDevices.isEmpty()) {
            sendInBatches(waterSupplyDevices, false);
        } else {
            log.warn("⚠️ device表中无「water_supply」类型设备");
        }

        log.info("=== 设备正常状态数据发送完成 ===");
    }

    /**
     * 分批次发送设备数据
     * @param deviceIds 设备ID列表
     * @param isWaterMaker 是否为制水机
     */
    private void sendInBatches(List<String> deviceIds, boolean isWaterMaker) {
        int batchSize = 3; // 每批发送3台设备
        int delayBetweenBatches = 1000 + random.nextInt(2000); // 1-3秒随机间隔

        for (int i = 0; i < deviceIds.size(); i++) {
            String deviceId = deviceIds.get(i);
            // 发送当前设备数据
            if (isWaterMaker) {
                mqttSensorSender.sendWaterMakerState(deviceId);
            } else {
                mqttSensorSender.sendWaterSupplyData(deviceId);
            }

            // 每发送batchSize台设备后休眠指定时间（最后一批除外）
            if ((i + 1) % batchSize == 0 && i != deviceIds.size() - 1) {
                try {
                    Thread.sleep(delayBetweenBatches);
                } catch (InterruptedException e) {
                    log.error("批次发送休眠被中断", e);
                    Thread.currentThread().interrupt(); // 恢复中断状态
                }
            }
        }
    }

    /**
     * 定时发送「随机告警数据」：每5分钟执行一次（同时包含制水机和供水机）
     */
    @Scheduled(fixedRate = 300000)
    public void sendRandomWarningData() {
        log.info("=== 开始发送随机告警数据 ===");

        // 处理制水机告警
        List<String> waterMakerDevices = deviceRepository.findByDeviceType(Device.DeviceType.water_maker)
                .stream()
                .map(Device::getDeviceId)
                .collect(Collectors.toList());

        if (!waterMakerDevices.isEmpty()) {
            int randomMakerIndex = random.nextInt(waterMakerDevices.size());
            String targetMakerDevice = waterMakerDevices.get(randomMakerIndex);
            mqttSensorSender.sendWaterMakerWarning(targetMakerDevice);
            log.info("制水机告警发送完成 | 告警设备：{}", targetMakerDevice);
        } else {
            log.warn("⚠️ device表中无「water_maker」类型设备，跳过制水机告警发送");
        }

        // 新增：处理供水机告警
        List<String> waterSupplyDevices = deviceRepository.findByDeviceType(Device.DeviceType.water_supply)
                .stream()
                .map(Device::getDeviceId)
                .collect(Collectors.toList());

        if (!waterSupplyDevices.isEmpty()) {
            int randomSupplyIndex = random.nextInt(waterSupplyDevices.size());
            String targetSupplyDevice = waterSupplyDevices.get(randomSupplyIndex);
            mqttSensorSender.sendWaterSupplyWarning(targetSupplyDevice); // 调用新增的供水机告警发送方法
            log.info("供水机告警发送完成 | 告警设备：{}", targetSupplyDevice);
        } else {
            log.warn("⚠️ device表中无「water_supply」类型设备，跳过供水机告警发送");
        }

        log.info("=== 随机告警数据发送完成 ===");
    }
}