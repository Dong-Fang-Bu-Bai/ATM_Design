package com.atm.atmserver.controller;

import com.atm.atmserver.common.Result;
import com.atm.atmserver.dto.AccountInfoResponse;
import com.atm.atmserver.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

/**
 * 账户相关接口控制器
 * 接口前缀：/api/atm/account（因为项目配置了 context-path: /api/atm）
 */
@RestController
@RequestMapping("/account")
public class AccountController {

    // 注入账户业务层（Spring自动装配）
    @Autowired
    private AccountService accountService;

    /**
     * 账户信息查询接口（初版）
     * 访问地址：http://localhost:8080/api/atm/account/info?cardNo=6222020000000001
     * @param cardNo 银行卡号
     * @return 账户完整信息（客户+账户+银行卡）
     */
    @GetMapping("/info")
    public Result<AccountInfoResponse> getAccountInfo(@RequestParam String cardNo) {
        AccountInfoResponse accountInfo = accountService.getAccountInfo(cardNo);
        return Result.success(accountInfo);
    }

    /**
     * 账户余额查询接口
     * 访问地址：http://localhost:8080/api/atm/account/balance?cardNo=6222020000000001
     * @param cardNo 银行卡号
     * @return 账户余额
     */
    @GetMapping("/balance")
    public Result<BigDecimal> getBalance(@RequestParam String cardNo) {
        BigDecimal balance = accountService.getBalance(cardNo);
        return Result.success(balance);
    }
}