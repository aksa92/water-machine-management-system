package com.campus.water.controller.web;

import com.campus.water.test.BaseTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 设备数据查询接口测试（后端2开发的接口）
 */
public class DeviceDataControllerTest extends BaseTest {

    // 测试1：按设备ID查询数据（正常场景）
    @Test
    public void testQueryDeviceDataByDeviceId() throws Exception {
        mockMvc.perform(
                        // 替换为你项目的实际接口路径，比如/api/web/device/data
                        get("/api/web/device/data")
                                .param("deviceId", "ZSJ001") // 传入测试设备ID
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk()) // 断言返回200
                .andExpect(jsonPath("$.code").value(200)) // 断言返回码（如果你的接口有统一返回体）
                .andExpect(jsonPath("$.data.deviceId").value("ZSJ001")); // 断言返回的设备ID正确
    }

    // 测试2：按设备ID查询数据（设备ID不存在）
    @Test
    public void testQueryDeviceDataWithInvalidId() throws Exception {
        mockMvc.perform(
                        get("/api/web/device/data")
                                .param("deviceId", "INVALID001")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(404)) // 断言返回404
                .andExpect(jsonPath("$.msg").value("设备不存在")); // 断言提示语
    }

    // 测试3：按时间范围查询数据（正常场景）
    @Test
    public void testQueryDeviceDataByTime() throws Exception {
        mockMvc.perform(
                        get("/api/web/device/data")
                                .param("deviceId", "ZSJ001")
                                .param("startTime", "2025-11-01 00:00:00")
                                .param("endTime", "2025-11-01 23:59:59")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data").isArray()); // 断言返回数组
    }
}