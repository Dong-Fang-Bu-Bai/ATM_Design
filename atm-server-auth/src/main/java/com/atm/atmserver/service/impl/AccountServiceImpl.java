package com.atm.atmserver.service.impl;

import com.atm.atmserver.common.ApiException;
import com.atm.atmserver.dto.AccountInfoResponse;
import com.atm.atmserver.entity.Account;
import com.atm.atmserver.entity.BankCard;
import com.atm.atmserver.entity.Customer;
import com.atm.atmserver.mapper.AccountMapper;
import com.atm.atmserver.mapper.BankCardMapper;
import com.atm.atmserver.mapper.CustomerMapper;
import com.atm.atmserver.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 账户业务层实现类
 */
@Service
public class AccountServiceImpl implements AccountService {

    // 注入Mapper（Spring自动装配，你的BankCardMapper用@Mapper注解完全兼容）
    @Autowired
    private BankCardMapper bankCardMapper;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private CustomerMapper customerMapper;

    @Override
    public AccountInfoResponse getAccountInfo(String cardNo) {
        // 1. 根据卡号查询银行卡（获取关联的账户ID）
        BankCard bankCard = bankCardMapper.selectByCardNo(cardNo);
        if (bankCard == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "卡号不存在");
        }

        // 2. 根据账户ID查询账户信息
        Account account = accountMapper.selectById(bankCard.getAccountId());
        if (account == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "账户信息不存在");
        }

        // 3. 根据客户ID查询客户信息
        Customer customer = customerMapper.selectById(account.getCustomerId());
        if (customer == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "客户信息不存在");
        }

        // 4. 封装响应DTO（整合所有信息）
        AccountInfoResponse response = new AccountInfoResponse();
        response.setCardNo(cardNo);
        response.setCustomerName(customer.getCustomerName());
        // 身份证号脱敏（只显示前6后4，中间用*代替）
        response.setIdCard(customer.getIdCard().replaceAll("(\\d{6})\\d{8}(\\d{4})", "$1********$2"));
        response.setAccountNo(account.getAccountNo());
        response.setBalance(account.getBalance());
        // 账户类型转文字描述
        response.setAccountType(account.getAccountType() == 1 ? "储蓄卡" : "信用卡");
        response.setCreateTime(customer.getCreateTime());

        return response;
    }

    @Override
    public BigDecimal getBalance(String cardNo) {
        // 1. 查询银行卡
        BankCard bankCard = bankCardMapper.selectByCardNo(cardNo);
        if (bankCard == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "卡号不存在");
        }
        // 2. 查询账户余额
        Account account = accountMapper.selectById(bankCard.getAccountId());
        if (account == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "账户不存在");
        }
        return account.getBalance();
    }
}
