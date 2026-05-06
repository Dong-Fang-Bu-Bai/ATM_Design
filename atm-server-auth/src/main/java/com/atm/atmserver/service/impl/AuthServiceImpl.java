package com.atm.atmserver.service.impl;

import com.atm.atmserver.dto.ChangePasswordRequest;
import com.atm.atmserver.dto.ChangePasswordResponse;
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

    @Override
    public ChangePasswordResponse changePassword(String cardNo, ChangePasswordRequest request) {
        ChangePasswordResponse response = new ChangePasswordResponse();
        
        // 1. 验证卡号是否存在
        BankCard bankCard = bankCardMapper.selectByCardNo(cardNo);
        if (bankCard == null) {
            response.setSuccess(false);
            response.setMessage("卡号不存在");
            return response;
        }
        
        // 2. 验证原密码是否正确
        if (!bankCard.getPassword().equals(request.getOldPassword())) {
            response.setSuccess(false);
            response.setMessage("原密码错误");
            return response;
        }
        
        // 3. 验证新密码是否符合要求（至少6位）
        if (request.getNewPassword() == null || request.getNewPassword().length() < 6) {
            response.setSuccess(false);
            response.setMessage("新密码长度不能少于6位");
            return response;
        }
        
        // 4. 更新密码到数据库
        int rows = bankCardMapper.updatePassword(cardNo, request.getNewPassword());
        if (rows <= 0) {
            response.setSuccess(false);
            response.setMessage("密码修改失败，请稍后重试");
            return response;
        }
        
        // 5. 密码修改成功
        response.setSuccess(true);
        response.setMessage("密码修改成功");
        
        // 6. 使当前Token失效，要求重新登录
        String currentToken = tokenManager.getTokenByCardNo(cardNo);
        if (currentToken != null) {
            tokenManager.logout(currentToken);
        }
        
        return response;
    }
}