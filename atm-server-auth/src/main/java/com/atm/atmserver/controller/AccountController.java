package com.atm.atmserver.controller;

import com.atm.atmserver.common.Result;
import com.atm.atmserver.dto.AccountInfoResponse;
import com.atm.atmserver.service.AccountService;
import com.atm.atmserver.util.TokenManager;
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
    private TokenManager tokenManager; // 注入 Token 管理器

    /**
     * 账户信息查询接口（加 Token 鉴权）
     * 访问地址：http://localhost:8080/api/atm/account/info?token=xxx
     * @param token 登录返回的 Token
     * @return 账户信息
     */
    @GetMapping("/info")
    public Result<AccountInfoResponse> getAccountInfo(@RequestParam String token) {
        // 1. 验证 Token 有效性
        if (!tokenManager.isValidToken(token)) {
            return Result.error("Token 无效或未登录");
        }
        // 2. 根据 Token 解析卡号（禁止前端传卡号）
        String cardNo = tokenManager.getCardNoByToken(token);
        // 3. 查询账户信息
        AccountInfoResponse accountInfo = accountService.getAccountInfo(cardNo);
        return Result.success(accountInfo);
    }

    /**
     * 余额查询接口（加 Token 鉴权）
     * 访问地址：http://localhost:8080/api/atm/account/balance?token=xxx
     * @param token 登录返回的 Token
     * @return 余额
     */
    @GetMapping("/balance")
    public Result<BigDecimal> getBalance(@RequestParam String token) {
        // 1. 验证 Token 有效性
        if (!tokenManager.isValidToken(token)) {
            return Result.error("Token 无效或未登录");
        }
        // 2. 根据 Token 解析卡号
        String cardNo = tokenManager.getCardNoByToken(token);
        // 3. 查询余额
        BigDecimal balance = accountService.getBalance(cardNo);
        return Result.success(balance);
    }
}