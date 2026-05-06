package com.atm.atmserver.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 完整账户信息查询响应 DTO
 */
@Data
public class FullAccountInfoResponse {
    private String cardNo;
    private String customerName;
    private String idCard;
    private String phone;
    private String accountNo;
    private BigDecimal balance;
    private String accountType;
    private LocalDateTime createTime;
    private String status;
}
