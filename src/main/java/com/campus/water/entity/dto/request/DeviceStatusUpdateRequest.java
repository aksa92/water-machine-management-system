/**
 * 设备状态更新请求数据传输对象（DTO）
 * 功能：接收设备状态变更的请求参数
 * 用途：统一设备状态管理接口的入参规范
 * 参数：
 *   - deviceId: 设备唯一标识（必填）
 *   - status: 目标状态（online/offline/fault/maintenance）
 *   - remark: 状态变更备注（可选）
 * 验证：非空验证、状态枚举验证
 */
package com.campus.water.entity.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class DeviceStatusUpdateRequest {
    @NotBlank(message = "设备ID不能为空")
    private String deviceId;

    @NotBlank(message = "设备状态不能为空")
    private String status;  // online/offline/fault/maintenance

    private String remark;  // 状态变更备注
}

