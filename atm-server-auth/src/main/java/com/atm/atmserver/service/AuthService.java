package com.atm.atmserver.service;

import com.atm.atmserver.dto.ChangePasswordRequest;
import com.atm.atmserver.dto.ChangePasswordResponse;
import com.atm.atmserver.dto.LoginRequest;
import com.atm.atmserver.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    // 退出登录方法
    void logout(String token);
    // 修改密码方法
    ChangePasswordResponse changePassword(String cardNo, ChangePasswordRequest request);
}