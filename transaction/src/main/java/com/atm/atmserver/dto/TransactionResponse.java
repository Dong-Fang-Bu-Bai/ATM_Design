package com.atm.atmserver.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionResponse {
    private Long id;
    private String transactionNo;
    private String cardNo;
    private Integer transactionType;
    private String transactionTypeName;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private Integer transactionStatus;
    private String transactionStatusName;
    private String targetAccountNo;
    private String targetBank;
    private String failureReason;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}
