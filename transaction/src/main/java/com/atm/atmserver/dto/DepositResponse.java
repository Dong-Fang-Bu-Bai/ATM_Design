package com.atm.atmserver.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DepositResponse {
    private String transactionId;
    private Boolean success;
    private String message;
    private BigDecimal updatedBalance;
}
