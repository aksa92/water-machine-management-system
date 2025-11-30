package com.campus.water.controller.app;

import com.campus.water.test.BaseTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import java.util.HashMap;
import java.util.Map;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 工单管理接口测试（抢单/拒单，后端2开发）
 * 直接复制粘贴，修改接口路径即可
 */
public class WorkOrderControllerTest extends BaseTest {

    // 工具：将Map转为JSON字符串
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 测试1：维修人员抢单（正常场景）
    @Test
    public void testGrabWorkOrder() throws Exception {
        // 构造请求参数
        Map<String, String> params = new HashMap<>();
        params.put("orderId", "1001"); // 测试工单ID
        params.put("repairId", "REPAIR001"); // 测试维修人员ID

        mockMvc.perform(
                        // 替换为你项目的抢单接口路径，比如/api/app/workOrder/grab
                        post("/api/app/workOrder/grab")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(params)) // 传入JSON参数
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("抢单成功"));
    }

    // 测试2：抢已被抢的工单（异常场景）
    @Test
    public void testGrabGrabbedOrder() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("orderId", "1001"); // 已被抢的工单ID
        params.put("repairId", "REPAIR002");

        mockMvc.perform(
                        post("/api/app/workOrder/grab")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(params))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500))
                .andExpect(jsonPath("$.msg").value("工单已被抢"));
    }

    // 测试3：维修人员拒单
    @Test
    public void testRejectWorkOrder() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("orderId", "1002");
        params.put("repairId", "REPAIR001");
        params.put("reason", "设备位置太远"); // 拒单原因

        mockMvc.perform(
                        post("/api/app/workOrder/reject")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(params))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.msg").value("拒单成功"));
    }
}