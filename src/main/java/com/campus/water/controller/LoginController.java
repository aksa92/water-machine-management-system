package com.campus.water.controller; // 修正包路径：去掉main.java + 按规范放在common子包

import com.campus.water.entity.dto.request.LoginRequest; // 替换原LoginDTO为规范的LoginRequest
import com.campus.water.entity.vo.LoginVO;
import com.campus.water.service.LoginService;
import com.campus.water.util.ResultVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * 登录接口控制器（公共接口）
 */
@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @PostMapping("/login")
    public ResultVO<LoginVO> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginVO loginVO = loginService.login(loginRequest);
        return ResultVO.success(loginVO);
    }
}