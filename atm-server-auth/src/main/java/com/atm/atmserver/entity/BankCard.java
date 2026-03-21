package com.atm.atmserver.entity;

import lombok.Data;

@Data
public class BankCard {
    private Long cardId;
    private String cardNo;
    private String cardPassword;
    private Long accountId;
}