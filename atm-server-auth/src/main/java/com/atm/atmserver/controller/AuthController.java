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
import org.springframework.util.StringUtils;
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
        authService.logout(request.getSessionId());
        return Result.success(null);
    }

    // 修改密码接口
    @PostMapping("/change-password")
    public Result<ChangePasswordResponse> changePassword(
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) String token,
            @RequestBody ChangePasswordRequest request
    ) {
        String requestSessionId = StringUtils.hasText(request.getSessionId()) ? request.getSessionId() : sessionId;
        String cardNo = sessionValidator.validateAndGetCardNo(requestSessionId, token);
        ChangePasswordResponse response = authService.changePassword(cardNo, request);
        return Result.success(response, response.getMessage());
    }
}
