package com.atm.atmserver.dto;

import lombok.Data;

/**
 * 修改密码响应 DTO
 */
@Data
public class ChangePasswordResponse {
    private boolean success;
    private String message;
}
