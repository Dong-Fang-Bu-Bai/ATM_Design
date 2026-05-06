package com.atm.atmserver.dto;

import lombok.Data;

/**
 * 修改密码请求DTO
 */
@Data
public class ChangePasswordRequest {
    private String oldPassword;  // 原密码
    private String newPassword;  // 新密码
}