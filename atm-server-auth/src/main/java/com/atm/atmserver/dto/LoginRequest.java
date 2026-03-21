package com.atm.atmserver.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String cardNo;
    private String password;
}