package com.atm.atmserver.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

/**
 * 退出登录请求参数 DTO
 */
@Data // Lombok 自动生成 getter/setter
public class LogoutRequest {
    @JsonAlias("sessionId")
    private String token; // 登录时返回的 Token
}
