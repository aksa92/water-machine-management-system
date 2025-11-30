package com.campus.water.controller.common;

import com.campus.water.test.BaseTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import java.util.HashMap;
import java.util.Map;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 登录接口测试用例
 */
public class LoginControllerTest extends BaseTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 测试1：维修人员正常登录
    @Test
    public void testRepairLogin() throws Exception {
        Map<String, String> loginParams = new HashMap<>();
        loginParams.put("username", "REPAIR001");
        loginParams.put("password", "123456");
        loginParams.put("userType", "repairer"); // 必须指定用户类型

        mockMvc.perform(
                        post("/api/common/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginParams))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200)) // 正常响应code
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.userType").value("repairer"));
    }

    // 测试2：学生登录（密码错误）
    @Test
    public void testStudentLoginWithWrongPwd() throws Exception {
        Map<String, String> loginParams = new HashMap<>();
        loginParams.put("username", "STUDENT001");
        loginParams.put("password", "wrong123");
        loginParams.put("userType", "user");

        mockMvc.perform(
                        post("/api/common/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginParams))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.msg").value("密码错误"));
    }

    // 测试3：无效用户类型
    @Test
    public void testInvalidUserType() throws Exception {
        Map<String, String> loginParams = new HashMap<>();
        loginParams.put("username", "TEST001");
        loginParams.put("password", "123456");
        loginParams.put("userType", "invalid");

        mockMvc.perform(
                        post("/api/common/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginParams))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.msg").value("无效的用户类型：invalid"));
    }
}