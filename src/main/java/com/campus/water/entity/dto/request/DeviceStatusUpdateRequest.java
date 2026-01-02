// 路径：com/campus/water/entity/dto/request/DeviceStatusUpdateRequest.java
package main.java.com.campus.water.entity.dto.request;

import main.java.com.campus.water.entity.Device;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeviceStatusUpdateRequest {
    @NotBlank(message = "设备ID不能为空")
    private String deviceId;

    private Device.DeviceStatus status;

    private String remark; // 状态变更备注
    private String faultType; // 故障类型（状态为fault时必填）
    private String faultDescription; // 故障描述（状态为fault时必填）
}