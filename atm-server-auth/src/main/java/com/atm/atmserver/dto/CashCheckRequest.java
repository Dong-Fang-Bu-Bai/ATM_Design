package com.atm.atmserver.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CashCheckRequest {
    private BigDecimal amount;
}
