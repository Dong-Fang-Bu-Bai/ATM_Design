package com.atm.atmserver.service.impl;

import com.atm.atmserver.dto.LoginRequest;
import com.atm.atmserver.dto.LoginResponse;
import com.atm.atmserver.entity.BankCard;
import com.atm.atmserver.mapper.BankCardMapper;
import com.atm.atmserver.service.AuthService;
import org.springframework.stereotype.Service;
import jakarta.annotation.Resource;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private BankCardMapper bankCardMapper;

    @Override
    public LoginResponse login(LoginRequest request) {
        BankCard card = bankCardMapper.selectByCardNo(request.getCardNo());

        if (card == null) {
            throw new RuntimeException("卡号不存在");
        }

        if (!card.getCardPassword().equals(request.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        LoginResponse resp = new LoginResponse();
        resp.setSessionId(UUID.randomUUID().toString());
        resp.setAccountId(card.getAccountId());
        return resp;
    }
}