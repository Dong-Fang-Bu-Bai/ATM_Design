package com.atm.atmserver.controller;

import com.atm.atmserver.common.Result;
import com.atm.atmserver.dto.AccountInfoResponse;
import com.atm.atmserver.dto.FullAccountInfoResponse;
import com.atm.atmserver.dto.TransactionValidationResponse;
import com.atm.atmserver.service.AccountService;
import com.atm.atmserver.util.SessionValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private AccountService accountService;
    @Autowired
    private SessionValidator sessionValidator; // 注入会话校验器

    /**
     * 账户信息查询接口（加 Token 鉴权）
     * 访问地址：http://localhost:8080/api/atm/account/info?token=xxx
     * @param token 登录返回的 Token
     * @return 账户信息
     */
    @GetMapping("/info")
    public Result<AccountInfoResponse> getAccountInfo(@RequestParam String token) {
        try {
            // 1. 验证 Token 有效性并获取卡号
            String cardNo = sessionValidator.validateAndGetCardNo(token);
            // 2. 查询账户信息
            AccountInfoResponse accountInfo = accountService.getAccountInfo(cardNo);
            return Result.success(accountInfo);
        } catch (RuntimeException e) {
            return Result.unauthorized(e.getMessage());
        }
    }

    /**
     * 完整账户信息查询接口（加 Token 鉴权）
     * 访问地址：http://localhost:8080/api/atm/account/full-info?token=xxx
     * @param token 登录返回的 Token
     * @return 完整账户信息
     */
    @GetMapping("/full-info")
    public Result<FullAccountInfoResponse> getFullAccountInfo(@RequestParam String token) {
        try {
            // 1. 验证 Token 有效性并获取卡号
            String cardNo = sessionValidator.validateAndGetCardNo(token);
            // 2. 查询完整账户信息
            FullAccountInfoResponse accountInfo = accountService.getFullAccountInfo(cardNo);
            return Result.success(accountInfo);
        } catch (RuntimeException e) {
            return Result.unauthorized(e.getMessage());
        }
    }

    /**
     * 余额查询接口（加 Token 鉴权）
     * 访问地址：http://localhost:8080/api/atm/account/balance?token=xxx
     * @param token 登录返回的 Token
     * @return 余额
     */
    @GetMapping("/balance")
    public Result<BigDecimal> getBalance(@RequestParam String token) {
        try {
            // 1. 验证 Token 有效性并获取卡号
            String cardNo = sessionValidator.validateAndGetCardNo(token);
            // 2. 查询余额
            BigDecimal balance = accountService.getBalance(cardNo);
            return Result.success(balance);
        } catch (RuntimeException e) {
            return Result.unauthorized(e.getMessage());
        }
    }

    /**
     * 交易前身份和账户校验接口（加 Token 鉴权）
     * 访问地址：http://localhost:8080/api/atm/account/validate-transaction?token=xxx
     * @param token 登录返回的 Token
     * @return 交易验证结果
     */
    @GetMapping("/validate-transaction")
    public Result<TransactionValidationResponse> validateForTransaction(@RequestParam String token) {
        try {
            // 1. 验证 Token 有效性并获取卡号
            String cardNo = sessionValidator.validateAndGetCardNo(token);
            // 2. 进行交易前验证
            TransactionValidationResponse validationResponse = accountService.validateForTransaction(cardNo);
            return Result.success(validationResponse);
        } catch (RuntimeException e) {
            return Result.unauthorized(e.getMessage());
        }
    }
}