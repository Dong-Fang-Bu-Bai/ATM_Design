package com.atm.atmserver.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferRequest {
    private String cardNo;
    private String targetAccountNo;
    private String targetBank;
    private BigDecimal amount;
    private String sessionId;
}
