package com.atm.atmserver.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账户信息查询响应DTO（给前端返回的结构化数据）
 */
@Data
public class AccountInfoResponse {
    private String cardNo;          // 银行卡号
    private String customerName;    // 客户姓名
    private String idCard;          // 脱敏后的身份证号
    private String accountNo;       // 账号
    private BigDecimal balance;     // 账户余额
    private String accountType;     // 账户类型（文字描述：储蓄卡/信用卡）
    private LocalDateTime createTime; // 开户时间
}