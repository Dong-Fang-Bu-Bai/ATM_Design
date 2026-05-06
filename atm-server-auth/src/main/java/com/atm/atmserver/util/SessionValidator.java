package com.atm.atmserver.util;

import com.atm.atmserver.common.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 会话校验工具类
 */
@Component
public class SessionValidator {

    @Autowired
    private TokenManager tokenManager;

    /**
     * 验证Token并获取卡号
     * @param token 登录返回的 Token
     * @return 卡号，如果Token无效则抛出异常
     */
    public String validateAndGetCardNo(String token) {
        // 1. 验证 Token 有效性
        if (token == null || token.isEmpty()) {
            throw new RuntimeException("Token 不能为空");
        }
        
        if (!tokenManager.isValidToken(token)) {
            throw new RuntimeException("Token 无效或未登录");
        }
        
        // 2. 根据 Token 解析卡号
        String cardNo = tokenManager.getCardNoByToken(token);
        if (cardNo == null) {
            throw new RuntimeException("无法获取用户信息");
        }
        
        return cardNo;
    }

    /**
     * 验证Token是否有效
     * @param token 登录返回的 Token
     * @return Result对象，包含验证结果
     */
    public Result<String> validateToken(String token) {
        try {
            String cardNo = validateAndGetCardNo(token);
            return Result.success(cardNo);
        } catch (RuntimeException e) {
            return Result.unauthorized(e.getMessage());
        }
    }
}