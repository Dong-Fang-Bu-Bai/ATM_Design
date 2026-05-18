package com.atm.atmserver.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CashCheckResponse {
    private Boolean available;
    private BigDecimal amount;
    private BigDecimal cashAvailable;
}
