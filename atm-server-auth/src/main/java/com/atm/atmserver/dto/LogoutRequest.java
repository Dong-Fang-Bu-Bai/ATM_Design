package com.atm.atmserver.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

/**
 * 退出登录请求参数 DTO
 */
@Data // Lombok 自动生成 getter/setter
public class LogoutRequest {
    @JsonAlias("token")
    private String sessionId; // 登录时返回的 ATM 会话 ID
}
