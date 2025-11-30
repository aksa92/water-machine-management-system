package com.campus.water.controller; // 修正包路径：去掉main.java + 按规范放在common子包

import com.campus.water.entity.dto.request.LoginRequest; // 替换原LoginDTO为规范的LoginRequest
import com.campus.water.entity.vo.LoginVO;
import com.campus.water.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * 登录接口控制器（公共接口）
 */
@RestController
@RequestMapping("/api/common") // 保持统一接口前缀
public class LoginController {

    @Autowired
    private LoginService loginService;

    /**
     * 登录接口
     * @param loginRequest 登录请求参数（替换原LoginDTO）
     * @return 登录响应结果
     */
    @PostMapping("/login")
    public ResponseEntity<LoginVO> login(@Valid @RequestBody LoginRequest loginRequest) { // 参数类型替换为LoginRequest
        LoginVO loginVO = loginService.login(loginRequest); // 同步修改入参类型
        return ResponseEntity.ok(loginVO);
    }
}