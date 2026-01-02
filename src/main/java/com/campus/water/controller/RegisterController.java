package main.java.com.campus.water.controller;

import main.java.com.campus.water.entity.dto.request.RegisterRequest;
import main.java.com.campus.water.service.RegisterService;
import main.java.com.campus.water.util.ResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * 注册接口控制器（公共接口）
 */
@RestController
@RequestMapping("/api/common")
public class RegisterController {

    @Autowired
    private RegisterService registerService;

    /**
     * 用户注册接口
     * @param registerRequest 注册请求参数
     * @return 注册结果
     */
    @PostMapping("/register")
    public ResponseEntity<ResultVO<Boolean>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        boolean success = registerService.register(registerRequest);
        return ResponseEntity.ok(ResultVO.success(success, "注册成功"));
    }
}