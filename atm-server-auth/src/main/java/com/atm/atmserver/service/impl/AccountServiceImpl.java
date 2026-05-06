package com.atm.atmserver.service.impl;

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
            throw new RuntimeException("卡号不存在");
        }

        // 2. 根据账户ID查询账户信息
        Account account = accountMapper.selectById(bankCard.getAccountId());
        if (account == null) {
            throw new RuntimeException("账户信息不存在");
        }

        // 3. 根据客户ID查询客户信息
        Customer customer = customerMapper.selectById(account.getCustomerId());
        if (customer == null) {
            throw new RuntimeException("客户信息不存在");
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
    public FullAccountInfoResponse getFullAccountInfo(String cardNo) {
        // 1. 根据卡号查询银行卡（获取关联的账户ID）
        BankCard bankCard = bankCardMapper.selectByCardNo(cardNo);
        if (bankCard == null) {
            throw new RuntimeException("卡号不存在");
        }

        // 2. 根据账户ID查询账户信息
        Account account = accountMapper.selectById(bankCard.getAccountId());
        if (account == null) {
            throw new RuntimeException("账户信息不存在");
        }

        // 3. 根据客户ID查询客户信息
        Customer customer = customerMapper.selectById(account.getCustomerId());
        if (customer == null) {
            throw new RuntimeException("客户信息不存在");
        }

        // 4. 封装完整响应DTO（整合所有信息）
        FullAccountInfoResponse response = new FullAccountInfoResponse();
        response.setCardNo(cardNo);
        response.setCustomerName(customer.getCustomerName());
        // 身份证号脱敏（只显示前6后4，中间用*代替）
        response.setIdCard(customer.getIdCard().replaceAll("(\\d{6})\\d{8}(\\d{4})", "$1********$2"));
        // 手机号脱敏（显示前3后4，中间用****代替）
        response.setPhone(customer.getPhone().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2"));
        response.setAccountNo(account.getAccountNo());
        response.setBalance(account.getBalance());
        // 账户类型转文字描述
        response.setAccountType(account.getAccountType() == 1 ? "储蓄卡" : "信用卡");
        response.setCreateTime(customer.getCreateTime());
        // 假设账户状态为正常（实际应从数据库获取）
        response.setStatus("正常");

        return response;
    }

    @Override
    public BigDecimal getBalance(String cardNo) {
        // 1. 查询银行卡
        BankCard bankCard = bankCardMapper.selectByCardNo(cardNo);
        if (bankCard == null) {
            throw new RuntimeException("卡号不存在");
        }
        // 2. 查询账户余额
        Account account = accountMapper.selectById(bankCard.getAccountId());
        if (account == null) {
            throw new RuntimeException("账户不存在");
        }
        return account.getBalance();
    }

    @Override
    public TransactionValidationResponse validateForTransaction(String cardNo) {
        TransactionValidationResponse response = new TransactionValidationResponse();
        
        try {
            // 1. 验证卡号是否存在
            BankCard bankCard = bankCardMapper.selectByCardNo(cardNo);
            if (bankCard == null) {
                response.setValid(false);
                response.setMessage("卡号不存在");
                return response;
            }
            
            // 2. 验证账户是否存在
            Account account = accountMapper.selectById(bankCard.getAccountId());
            if (account == null) {
                response.setValid(false);
                response.setMessage("账户不存在");
                return response;
            }
            
            // 3. 验证客户是否存在
            Customer customer = customerMapper.selectById(account.getCustomerId());
            if (customer == null) {
                response.setValid(false);
                response.setMessage("客户信息不存在");
                return response;
            }
            
            // 4. 验证账户状态（这里假设账户状态正常，实际应从数据库获取）
            // 可以添加更多验证逻辑，如账户是否冻结、是否过期等
            
            // 5. 设置验证通过的信息
            response.setValid(true);
            response.setCardNo(cardNo);
            response.setAccountId(bankCard.getAccountId());
            response.setAccountNo(account.getAccountNo());
            response.setBalance(account.getBalance());
            response.setCustomerName(customer.getCustomerName());
            response.setAccountType(account.getAccountType());
            response.setStatus("正常");
            response.setMessage("验证通过");
            
        } catch (Exception e) {
            response.setValid(false);
            response.setMessage("验证过程中发生错误: " + e.getMessage());
        }
        
        return response;
    }
}