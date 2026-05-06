package com.atm.atmserver.dto;

import lombok.Data;

/**
 * 修改密码响应DTO
 */
@Data
public class ChangePasswordResponse {
    private String message;  // 操作结果消息
    private boolean success; // 是否成功
}