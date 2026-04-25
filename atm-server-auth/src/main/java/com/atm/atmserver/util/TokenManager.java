package com.atm.atmserver.util;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Token 管理器（开发环境临时用 HashMap，生产环境替换为 Redis）
 */
@Component // 交给 Spring 管理，可注入到其他类
public class TokenManager {
    // 存储：Token -> 卡号
    private final Map<String, String> tokenToCard = new ConcurrentHashMap<>();
    // 新增：卡号 -> Token（快速根据卡号查旧 Token）
    private final Map<String, String> cardToToken = new ConcurrentHashMap<>();

    /**
     * 生成 Token（登录时调用）
     * @param cardNo 银行卡号
     * @return 唯一 Token 字符串
     */
    public synchronized String generateToken(String cardNo) {
        // 1. 先删除该卡号关联的旧 Token
        if (cardToToken.containsKey(cardNo)) {
            String oldToken = cardToToken.get(cardNo);
            tokenToCard.remove(oldToken); // 从 Token 映射中删除旧 Token
        }
        // 2. 生成新 Token
        String newToken = UUID.randomUUID().toString().replace("-", "");
        // 3. 更新双向映射
        tokenToCard.put(newToken, cardNo);
        cardToToken.put(cardNo, newToken);
        return newToken;
    }

    /**
     * 验证 Token 是否有效（退出/其他接口时调用）
     * @param token 登录返回的 Token
     * @return true=有效，false=无效/已过期
     */
    public boolean isValidToken(String token) {
        return tokenToCard.containsKey(token);
    }

    /**
     * 退出登录（删除 Token，使其失效）
     * @param token 登录返回的 Token
     */
    public synchronized void logout(String token) {
        if (tokenToCard.containsKey(token)) {
            String cardNo = tokenToCard.get(token);
            // 双向删除，保证映射一致
            tokenToCard.remove(token);
            cardToToken.remove(cardNo);
        }
    }

    /**
     * 根据 Token 获取卡号（扩展用，比如后续接口验证身份）
     * @param token 登录返回的 Token
     * @return 银行卡号
     */
    public String getCardNoByToken(String token) {
        return tokenToCard.get(token);
    }
}

