package com.atm.atmserver.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenManager {
    private final Map<String, String> tokenMap = new ConcurrentHashMap<>();

    public String generateToken(String cardNo) {
        String token = java.util.UUID.randomUUID().toString();
        tokenMap.put(token, cardNo);
        return token;
    }

    public boolean isValidToken(String token) {
        return tokenMap.containsKey(token);
    }

    public String getCardNo(String token) {
        return tokenMap.get(token);
    }

    public void logout(String token) {
        tokenMap.remove(token);
    }
}
