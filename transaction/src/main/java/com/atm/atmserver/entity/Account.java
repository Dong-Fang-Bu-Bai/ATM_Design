package com.atm.atmserver.entity;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class Account {
    private Long id;
    private Long customerId;
    private String accountNo;
    private BigDecimal balance;
    private Integer accountType;
}
