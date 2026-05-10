package com.atm.atmserver.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DepositRequest {
    private String sessionId;
    private BigDecimal amount;
    private Boolean printReceipt;
}
