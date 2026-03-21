package com.atm.atmserver.entity;

import lombok.Data;

/**
 * 银行卡实体（对应 bank_card 表）
 */
@Data
public class BankCard {
    private Long id;         // 主键ID
    private String cardNo;   // 卡号（对应表的 card_no）
    private String CardPassword; // 密码（对应表的 password）
    private Long accountId;  // 新增：关联账户ID（对应表的 account_id）
    public String getPassword(){
        return CardPassword;
    }
}