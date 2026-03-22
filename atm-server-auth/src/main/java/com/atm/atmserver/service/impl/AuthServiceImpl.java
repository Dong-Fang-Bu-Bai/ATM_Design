package com.atm.atmserver.service.impl;

import com.atm.atmserver.dto.LoginRequest;
import com.atm.atmserver.dto.LoginResponse;
import com.atm.atmserver.entity.BankCard;
import com.atm.atmserver.mapper.BankCardMapper;
import com.atm.atmserver.service.AuthService;
import com.atm.atmserver.util.TokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private BankCardMapper bankCardMapper;
    @Autowired
    private TokenManager tokenManager;

    @Override
    public LoginResponse login(LoginRequest request) {
        BankCard bankCard = bankCardMapper.selectByCardNo(request.getCardNo());
        if (bankCard == null) {
            throw new RuntimeException("卡号不存在");
        }
        if (!bankCard.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        // 生成 Token
        String token = tokenManager.generateToken(request.getCardNo());

        LoginResponse response = new LoginResponse();
        response.setSessionId(token);
        // 从 bankCard 中获取真实的 accountId
        response.setAccountId(bankCard.getAccountId());
        return response;
    }

    @Override
    public void logout(String token) {
        if (!tokenManager.isValidToken(token)) {
            throw new RuntimeException("Token 无效或已过期");
        }
        tokenManager.logout(token);
    }
}