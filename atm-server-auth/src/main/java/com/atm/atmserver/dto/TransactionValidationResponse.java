package com.atm.atmserver.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 交易前验证响应 DTO
 */
@Data
public class TransactionValidationResponse {
    private boolean valid;
    private String cardNo;
    private Long accountId;
    private String accountNo;
    private BigDecimal balance;
    private String customerName;
    private String message;
    private Integer accountType;
    private String status;
}
