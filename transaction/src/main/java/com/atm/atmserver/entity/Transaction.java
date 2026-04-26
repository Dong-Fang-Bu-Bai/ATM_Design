package com.atm.atmserver.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Transaction {
    private Long id;
    private String transactionId;
    private Long accountId;
    private String cardNo;
    private Integer transactionType;
    private BigDecimal amount;
    private BigDecimal balanceBefore;
    private BigDecimal balanceAfter;
    private Integer transactionStatus;
    private String targetAccountNo;
    private String targetBank;
    private String failureReason;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;

    public static final Integer TYPE_WITHDRAW = 1;
    public static final Integer TYPE_DEPOSIT = 2;
    public static final Integer TYPE_TRANSFER = 3;

    public static final Integer STATUS_PENDING = 0;
    public static final Integer STATUS_SUCCESS = 1;
    public static final Integer STATUS_FAILED = 2;
    public static final Integer STATUS_CANCELLED = 3;
}
