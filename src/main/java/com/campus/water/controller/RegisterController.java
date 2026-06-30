package com.campus.water.controller;

import com.campus.water.entity.dto.request.RegisterRequest;
import com.campus.water.service.RegisterService;
import com.campus.water.util.ResultVO;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class RegisterController {

    private final RegisterService registerService;

    @PostMapping("/register")
    public ResultVO<Boolean> register(@Valid @RequestBody RegisterRequest registerRequest) {
        boolean success = registerService.register(registerRequest);
        return ResultVO.success(success, "注册成功");
    }
}