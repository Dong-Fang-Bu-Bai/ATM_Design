package com.atm.atmserver.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

/**
 * 修改密码请求 DTO
 */
@Data
public class ChangePasswordRequest {
    @JsonAlias("token")
    private String sessionId;
    private String oldPassword;
    private String newPassword;
}
