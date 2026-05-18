package com.atm.atmserver.dto;

import lombok.Data;

@Data
public class TransactionHistoryResponse {
    private Integer page;
    private Integer size;
    private Long total;
    private java.util.List<TransactionHistoryItem> records;
}
