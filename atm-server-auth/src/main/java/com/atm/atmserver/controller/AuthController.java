package com.atm.atmserver.controller;

import com.atm.atmserver.common.Result;
import com.atm.atmserver.dto.ChangePasswordRequest;
import com.atm.atmserver.dto.ChangePasswordResponse;
import com.atm.atmserver.dto.LoginRequest;
import com.atm.atmserver.dto.LoginResponse;
import com.atm.atmserver.dto.LogoutRequest;
import com.atm.atmserver.service.AuthService;
import com.atm.atmserver.util.SessionValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录/退出登录接口控制器
 * 接口前缀：/api/atm/auth（context-path: /api/atm）
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private SessionValidator sessionValidator;

    // 登录接口
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    // 退出登录接口
    @PostMapping("/logout")
    public Result<Void> logout(@RequestBody LogoutRequest request) {
        try {
            // 1. 验证 Token 有效性
            sessionValidator.validateAndGetCardNo(request.getToken());
            // 2. 执行退出登录
            authService.logout(request.getToken());
            return Result.success(null);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage()); // 返回错误提示
        }
    }

    // 修改密码接口
    @PostMapping("/change-password")
    public Result<ChangePasswordResponse> changePassword(
            @RequestParam String token,
            @RequestBody ChangePasswordRequest request) {
        try {
            // 1. 验证 Token 有效性并获取卡号
            String cardNo = sessionValidator.validateAndGetCardNo(token);
            // 2. 调用服务层修改密码
            ChangePasswordResponse response = authService.changePassword(cardNo, request);
            if (response.isSuccess()) {
                return Result.success(response);
            } else {
                return Result.error(response.getMessage());
            }
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}