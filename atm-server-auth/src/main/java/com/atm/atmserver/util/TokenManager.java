package com.atm.atmserver.util;

import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Token 管理器（开发环境临时用 HashMap，生产环境替换为 Redis）
 */
@Component // 交给 Spring 管理，可注入到其他类
public class TokenManager {
    // 存储：Token -> 卡号
    private static final Map<String, String> TOKEN_TO_CARD = new HashMap<>();
    // 新增：卡号 -> Token（快速根据卡号查旧 Token）
    private static final Map<String, String> CARD_TO_TOKEN = new HashMap<>();

    /**
     * 生成 Token（登录时调用）
     * @param cardNo 银行卡号
     * @return 唯一 Token 字符串
     */
    public String generateToken(String cardNo) {
        // 1. 先删除该卡号关联的旧 Token
        if (CARD_TO_TOKEN.containsKey(cardNo)) {
            String oldToken = CARD_TO_TOKEN.get(cardNo);
            TOKEN_TO_CARD.remove(oldToken); // 从 Token 映射中删除旧 Token
        }
        // 2. 生成新 Token
        String newToken = UUID.randomUUID().toString().replace("-", "");
        // 3. 更新双向映射
        TOKEN_TO_CARD.put(newToken, cardNo);
        CARD_TO_TOKEN.put(cardNo, newToken);
        return newToken;
    }

    /**
     * 验证 Token 是否有效（退出/其他接口时调用）
     * @param token 登录返回的 Token
     * @return true=有效，false=无效/已过期
     */
    public boolean isValidToken(String token) {
        return TOKEN_TO_CARD.containsKey(token);
    }

    /**
     * 退出登录（删除 Token，使其失效）
     * @param token 登录返回的 Token
     */
    public void logout(String token) {
        if (TOKEN_TO_CARD.containsKey(token)) {
            String cardNo = TOKEN_TO_CARD.get(token);
            // 双向删除，保证映射一致
            TOKEN_TO_CARD.remove(token);
            CARD_TO_TOKEN.remove(cardNo);
        }
    }

    /**
     * 根据 Token 获取卡号（扩展用，比如后续接口验证身份）
     * @param token 登录返回的 Token
     * @return 银行卡号
     */
    public String getCardNoByToken(String token) {
        return TOKEN_TO_CARD.get(token);
    }
}

