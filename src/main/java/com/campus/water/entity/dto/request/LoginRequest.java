// LoginRequest.java（原LoginDTO，按项目规范重命名）
package main.java.com.campus.water.entity.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class LoginRequest { // 命名改为Request，符合dto/request分类
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "用户类型不能为空")
    private String userType; // admin/repairer/user
}