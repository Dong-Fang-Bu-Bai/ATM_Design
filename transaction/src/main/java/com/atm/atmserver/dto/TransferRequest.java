package com.atm.atmserver.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferRequest {
    private String sessionId;
    private String targetAccountNo;
    private BigDecimal amount;
    private Boolean printReceipt;
}
