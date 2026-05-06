package com.atm.atmserver.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 交易前验证响应DTO
 */
@Data
public class TransactionValidationResponse {
    private boolean valid;           // 验证是否通过
    private String cardNo;           // 银行卡号
    private Long accountId;          // 账户ID
    private String accountNo;        // 账号
    private BigDecimal balance;      // 账户余额
    private String customerName;     // 客户姓名
    private String message;          // 验证结果消息
    private Integer accountType;     // 账户类型：1-储蓄卡 2-信用卡
    private String status;           // 账户状态
}