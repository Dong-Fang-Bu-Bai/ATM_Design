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
    // 存储 Token -> 卡号（key=Token，value=银行卡号）
    private static final Map<String, String> TOKEN_STORE = new HashMap<>();

    /**
     * 生成 Token（登录时调用）
     * @param cardNo 银行卡号
     * @return 唯一 Token 字符串
     */
    public String generateToken(String cardNo) {
        // 生成 UUID 作为 Token（去掉横线，更简洁）
        String token = UUID.randomUUID().toString().replace("-", "");
        TOKEN_STORE.put(token, cardNo); // 存储 Token 和卡号的关联
        return token;
    }

    /**
     * 验证 Token 是否有效（退出/其他接口时调用）
     * @param token 登录返回的 Token
     * @return true=有效，false=无效/已过期
     */
    public boolean isValidToken(String token) {
        return TOKEN_STORE.containsKey(token);
    }

    /**
     * 退出登录（删除 Token，使其失效）
     * @param token 登录返回的 Token
     */
    public void logout(String token) {
        TOKEN_STORE.remove(token); // 从内存中删除 Token，后续无法使用
    }

    /**
     * 根据 Token 获取卡号（扩展用，比如后续接口验证身份）
     * @param token 登录返回的 Token
     * @return 银行卡号
     */
    public String getCardNoByToken(String token) {
        return TOKEN_STORE.get(token);
    }
}