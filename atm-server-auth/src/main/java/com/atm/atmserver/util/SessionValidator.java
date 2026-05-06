package com.atm.atmserver.util;

import com.atm.atmserver.common.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 会话校验工具类。sessionId 是主契约，token 仅作为兼容别名。
 */
@Component
public class SessionValidator {

    @Autowired
    private TokenManager tokenManager;

    public String validateAndGetCardNo(String sessionId, String token) {
        String resolvedSessionId = StringUtils.hasText(sessionId) ? sessionId : token;
        if (!StringUtils.hasText(resolvedSessionId)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "sessionId 不能为空");
        }

        if (!tokenManager.isValidToken(resolvedSessionId)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "sessionId 无效或未登录");
        }

        String cardNo = tokenManager.getCardNoByToken(resolvedSessionId);
        if (!StringUtils.hasText(cardNo)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "sessionId 无效或未登录");
        }

        return cardNo;
    }
}
