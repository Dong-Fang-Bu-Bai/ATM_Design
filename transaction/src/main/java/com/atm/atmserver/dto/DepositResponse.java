package com.atm.atmserver.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DepositResponse {
    private String transactionNo;
    private Boolean success;
    private String message;
    private BigDecimal updatedBalance;
}
