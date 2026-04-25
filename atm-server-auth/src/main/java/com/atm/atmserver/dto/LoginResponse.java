package com.atm.atmserver.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private String sessionId;
    private Long accountId;
}