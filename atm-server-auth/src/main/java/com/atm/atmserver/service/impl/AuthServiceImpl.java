package com.atm.atmserver.service.impl;

import com.atm.atmserver.common.ApiException;
import com.atm.atmserver.dto.ChangePasswordRequest;
import com.atm.atmserver.dto.ChangePasswordResponse;
import com.atm.atmserver.dto.LoginRequest;
import com.atm.atmserver.dto.LoginResponse;
import com.atm.atmserver.entity.BankCard;
import com.atm.atmserver.mapper.BankCardMapper;
import com.atm.atmserver.service.AuthService;
import com.atm.atmserver.util.TokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

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
            throw new ApiException(HttpStatus.UNAUTHORIZED, "卡号不存在");
        }
        if (!bankCard.getPassword().equals(request.getPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "密码错误");
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
            throw new ApiException(HttpStatus.UNAUTHORIZED, "sessionId 无效或已过期");
        }
        tokenManager.logout(token);
    }

    @Override
    public ChangePasswordResponse changePassword(String cardNo, ChangePasswordRequest request) {
        BankCard bankCard = bankCardMapper.selectByCardNo(cardNo);
        if (bankCard == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "卡号不存在");
        }

        if (!bankCard.getPassword().equals(request.getOldPassword())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "原密码错误");
        }

        if (!StringUtils.hasText(request.getNewPassword()) || !request.getNewPassword().matches("\\d{6}")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "新密码必须为 6 位数字");
        }

        if (bankCard.getPassword().equals(request.getNewPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "新密码不能与原密码一致");
        }

        int rows = bankCardMapper.updatePassword(cardNo, request.getNewPassword());
        if (rows <= 0) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "密码修改失败，请稍后重试");
        }

        String currentToken = tokenManager.getTokenByCardNo(cardNo);
        if (currentToken != null) {
            tokenManager.logout(currentToken);
        }

        ChangePasswordResponse response = new ChangePasswordResponse();
        response.setSuccess(true);
        response.setMessage("密码修改成功，请重新登录");
        return response;
    }
}
