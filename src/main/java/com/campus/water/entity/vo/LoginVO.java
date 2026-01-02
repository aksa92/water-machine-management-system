// LoginVO.java（保持VO命名，无需修改）
package com.campus.water.entity.vo;

import lombok.Data;

@Data
public class LoginVO {
    private String token;       // 登录令牌
    private String userId;      // 用户ID
    private String username;    // 用户名
    private String userType;    // 用户类型
    private String areaId; // 新增：维修人员所属区域ID

}