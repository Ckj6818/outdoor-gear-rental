package com.outdoor.rental.controller;

import com.outdoor.rental.common.Result;
import com.outdoor.rental.dto.LoginDTO;
import com.outdoor.rental.service.AuthService;
import com.outdoor.rental.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录，返回 JWT Token
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@RequestBody @Valid LoginDTO loginDTO) {
        return Result.success("登录成功", authService.login(loginDTO));
    }
}
