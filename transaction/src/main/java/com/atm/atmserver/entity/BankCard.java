package com.atm.atmserver.entity;

import lombok.Data;

@Data
public class BankCard {
    private Long id;
    private String cardNo;
    private String password;
    private Long accountId;
}
