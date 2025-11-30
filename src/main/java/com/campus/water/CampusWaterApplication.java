package com.campus.water;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.config.EnableIntegrationManagement;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot 主启动类
 * 核心注解：开启自动配置、定时任务、Spring Integration
 */
@SpringBootApplication(scanBasePackages = "com.campus.water") // 扫描所有业务组件
@EnableScheduling // 开启定时任务（支持@Scheduled）
@EnableIntegration // 开启Spring Integration（支持MQTT集成）
@EnableIntegrationManagement // 开启Integration管理（监控消息流转）
public class CampusWaterApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampusWaterApplication.class, args);
        System.out.println("=== 校园直饮矿化水系统（Spring Boot版）启动成功 ===");
        System.out.println("=== MQTT传感器模拟、数据接收、持久化功能已启用 ===");
    }
}