package com.atm.atmserver.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DepositRequest {
    private String cardNo;
    private BigDecimal amount;
    private String sessionId;
}
