package com.atm.atmserver.service.impl;

import com.atm.atmserver.common.ApiException;
import com.atm.atmserver.dto.AccountInfoResponse;
import com.atm.atmserver.dto.FullAccountInfoResponse;
import com.atm.atmserver.dto.TransactionValidationResponse;
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
        AccountContext context = loadAccountContext(cardNo);

        // 4. 封装响应DTO（整合所有信息）
        AccountInfoResponse response = new AccountInfoResponse();
        response.setCardNo(cardNo);
        response.setCustomerName(context.customer.getCustomerName());
        // 身份证号脱敏（只显示前6后4，中间用*代替）
        response.setIdCard(maskIdCard(context.customer.getIdCard()));
        response.setAccountNo(context.account.getAccountNo());
        response.setBalance(context.account.getBalance());
        // 账户类型转文字描述
        response.setAccountType(resolveAccountType(context.account.getAccountType()));
        response.setCreateTime(context.customer.getCreateTime());

        return response;
    }

    @Override
    public FullAccountInfoResponse getFullAccountInfo(String cardNo) {
        AccountContext context = loadAccountContext(cardNo);

        FullAccountInfoResponse response = new FullAccountInfoResponse();
        response.setCardNo(cardNo);
        response.setCustomerName(context.customer.getCustomerName());
        response.setIdCard(maskIdCard(context.customer.getIdCard()));
        response.setPhone(maskPhone(context.customer.getPhone()));
        response.setAccountNo(context.account.getAccountNo());
        response.setBalance(context.account.getBalance());
        response.setAccountType(resolveAccountType(context.account.getAccountType()));
        response.setCreateTime(context.customer.getCreateTime());
        response.setStatus("正常");
        return response;
    }

    @Override
    public BigDecimal getBalance(String cardNo) {
        return loadAccountContext(cardNo).account.getBalance();
    }

    @Override
    public TransactionValidationResponse validateForTransaction(String cardNo) {
        TransactionValidationResponse response = new TransactionValidationResponse();

        BankCard bankCard = bankCardMapper.selectByCardNo(cardNo);
        if (bankCard == null) {
            response.setValid(false);
            response.setMessage("卡号不存在");
            return response;
        }

        Account account = accountMapper.selectById(bankCard.getAccountId());
        if (account == null) {
            response.setValid(false);
            response.setMessage("账户不存在");
            return response;
        }

        Customer customer = customerMapper.selectById(account.getCustomerId());
        if (customer == null) {
            response.setValid(false);
            response.setMessage("客户信息不存在");
            return response;
        }

        response.setValid(true);
        response.setCardNo(cardNo);
        response.setAccountId(bankCard.getAccountId());
        response.setAccountNo(account.getAccountNo());
        response.setBalance(account.getBalance());
        response.setCustomerName(customer.getCustomerName());
        response.setAccountType(account.getAccountType());
        response.setStatus("正常");
        response.setMessage("验证通过");
        return response;
    }

    private AccountContext loadAccountContext(String cardNo) {
        BankCard bankCard = bankCardMapper.selectByCardNo(cardNo);
        if (bankCard == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "卡号不存在");
        }

        Account account = accountMapper.selectById(bankCard.getAccountId());
        if (account == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "账户信息不存在");
        }

        Customer customer = customerMapper.selectById(account.getCustomerId());
        if (customer == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "客户信息不存在");
        }

        return new AccountContext(account, customer);
    }

    private String maskIdCard(String idCard) {
        return idCard == null ? null : idCard.replaceAll("(\\d{6})\\d{8}(\\d{4})", "$1********$2");
    }

    private String maskPhone(String phone) {
        return phone == null ? null : phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    private String resolveAccountType(Integer accountType) {
        return Integer.valueOf(1).equals(accountType) ? "储蓄卡" : "信用卡";
    }

    private static class AccountContext {
        private final Account account;
        private final Customer customer;

        private AccountContext(Account account, Customer customer) {
            this.account = account;
            this.customer = customer;
        }
    }
}
