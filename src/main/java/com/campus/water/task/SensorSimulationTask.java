package com.campus.water.task;

import com.campus.water.service.MqttSensorSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SensorSimulationTask {
    private final MqttSensorSender mqttSensorSender;

    // 模拟已部署的设备列表（实际项目可从数据库查询）
    private final List<String> waterMakerDevices = new ArrayList<>() {{
        add("WM001"); // 制水机1
        add("WM002"); // 制水机2
        add("WM003"); // 制水机3
        add("WM004"); // 制水机4
    }};

    private final List<String> waterSupplyDevices = new ArrayList<>() {{
        add("WS001"); // 供水机1
        add("WS002"); // 供水机2
        add("WS003"); // 供水机3
    }};

    /**
     * 定时发送「正常状态数据」：每30秒执行一次
     * fixedRate：固定间隔（从上一次任务开始时间计算）
     */
    @Scheduled(fixedRate = 30000)
    public void sendRegularStateData() {
        log.info("=== 开始发送设备正常状态数据 ===");

        // 1. 发送所有制水机状态
        for (String deviceId : waterMakerDevices) {
            mqttSensorSender.sendWaterMakerState(deviceId);
        }

        // 2. 发送所有供水机状态
        for (String deviceId : waterSupplyDevices) {
            mqttSensorSender.sendWaterSupplyData(deviceId);
        }

        log.info("=== 设备正常状态数据发送完成 ===");
    }

    /**
     * 定时发送「随机告警数据」：每5分钟执行一次
     * fixedRate：固定间隔（从上一次任务开始时间计算）
     */
    @Scheduled(fixedRate = 300000)
    public void sendRandomWarningData() {
        log.info("=== 开始发送随机告警数据 ===");

        // 随机选择1台制水机发送告警（模拟设备故障）
        int randomIndex = (int) (Math.random() * waterMakerDevices.size());
        String targetDevice = waterMakerDevices.get(randomIndex);
        mqttSensorSender.sendWaterMakerWarning(targetDevice);

        log.info("=== 随机告警数据发送完成 | 告警设备：{} ===", targetDevice);
    }
}