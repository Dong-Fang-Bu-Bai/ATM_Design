package com.atm.atmserver.service;

import com.atm.atmserver.dto.AccountInfoResponse;
import java.math.BigDecimal;

/**
 * 账户业务层接口
 */
public interface AccountService {
    /**
     * 根据卡号查询账户完整信息（客户+账户+银行卡）
     * @param cardNo 银行卡号
     * @return 账户信息响应DTO
     */
    AccountInfoResponse getAccountInfo(String cardNo);

    /**
     * 根据卡号查询账户余额
     * @param cardNo 银行卡号
     * @return 账户余额
     */
    BigDecimal getBalance(String cardNo);
}