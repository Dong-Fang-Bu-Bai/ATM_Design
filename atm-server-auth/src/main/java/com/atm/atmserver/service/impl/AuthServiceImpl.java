package com.atm.atmserver.service.impl;

import com.atm.atmserver.dto.LoginRequest;
import com.atm.atmserver.dto.LoginResponse;
import com.atm.atmserver.entity.BankCard;
import com.atm.atmserver.mapper.BankCardMapper;
import com.atm.atmserver.service.AuthService;
import com.atm.atmserver.util.TokenManager; // 导入 Token 工具类
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private BankCardMapper bankCardMapper;

    @Autowired // 注入 Token 管理器
    private TokenManager tokenManager;

    @Override
    public LoginResponse login(LoginRequest request) {
        // 1. 查询银行卡信息
        BankCard bankCard = bankCardMapper.selectByCardNo(request.getCardNo());
        if (bankCard == null) {
            throw new RuntimeException("卡号不存在");
        }
        // 2. 验证密码
        if (!bankCard.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("密码错误");
        }
        // 3. 生成 Token（核心修改：用 Token 替代原来的静态 sessionId）
        String token = tokenManager.generateToken(request.getCardNo());

        // 4. 封装返回结果
        LoginResponse response = new LoginResponse();
        response.setSessionId(token); // 返回 Token 作为 sessionId
        response.setAccountId(1L);    // 账户 ID（可根据实际业务调整）
        return response;
    }

    @Override
    public void logout(String token) {
        // 1. 验证 Token 是否有效
        if (!tokenManager.isValidToken(token)) {
            throw new RuntimeException("Token 无效或已过期");
        }
        // 2. 删除 Token，完成退出登录
        tokenManager.logout(token);
    }
}