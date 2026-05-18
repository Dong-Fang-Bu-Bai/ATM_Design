package com.atm.atmserver.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionHistoryItem {
    private String transactionId;
    private String transactionType;
    private BigDecimal amount;
    private String transactionStatus;
    private LocalDateTime transactionTime;
}
