package com.atm.atmserver.service;

import com.atm.atmserver.dto.LoginRequest;
import com.atm.atmserver.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    // 退出登录方法
    void logout(String token);
}