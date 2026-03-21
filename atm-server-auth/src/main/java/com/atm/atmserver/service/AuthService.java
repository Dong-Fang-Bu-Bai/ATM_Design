package com.atm.atmserver.service;

import com.atm.atmserver.dto.LoginRequest;
import com.atm.atmserver.dto.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest request);
}