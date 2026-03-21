package com.atm.atmserver.entity;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 账户实体（对应 account 表）
 */
@Data
public class Account {
    private Long id;             // 账户ID（对应表的 id）
    private Long customerId;     // 关联客户ID（对应表的 customer_id）
    private String accountNo;    // 账号（对应表的 account_no）
    private BigDecimal balance;  // 账户余额（对应表的 balance，用 BigDecimal 避免精度丢失）
    private Integer accountType; // 账户类型：1-储蓄卡 2-信用卡（对应表的 account_type）
}