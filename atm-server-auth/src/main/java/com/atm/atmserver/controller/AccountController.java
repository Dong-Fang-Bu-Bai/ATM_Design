package com.atm.atmserver.controller;

import com.atm.atmserver.common.Result;
import com.atm.atmserver.dto.AccountInfoResponse;
import com.atm.atmserver.dto.BalanceResponse;
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
@RequestMapping({"/account", "/accounts"})
public class AccountController {

    @Autowired
    private AccountService accountService;
    @Autowired
    private SessionValidator sessionValidator;

    /**
     * 账户信息查询接口
     * 兼容新老路径与参数名：
     * /api/atm/account/info?token=xxx
     * /api/atm/accounts/profile?sessionId=xxx
     * @return 账户信息
     */
    @GetMapping({"/info", "/profile"})
    public Result<AccountInfoResponse> getAccountInfo(
            @RequestParam(required = false) String token,
            @RequestParam(required = false) String sessionId
    ) {
        String cardNo = sessionValidator.validateAndGetCardNo(sessionId, token);
        AccountInfoResponse accountInfo = accountService.getAccountInfo(cardNo);
        return Result.success(accountInfo);
    }

    /**
     * 完整账户信息查询接口
     * 主路径：/api/atm/accounts/full-info?sessionId=xxx
     * 兼容：/api/atm/account/full-info?token=xxx
     * @return 完整账户信息
     */
    @GetMapping("/full-info")
    public Result<FullAccountInfoResponse> getFullAccountInfo(
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) String token
    ) {
        String cardNo = sessionValidator.validateAndGetCardNo(sessionId, token);
        FullAccountInfoResponse accountInfo = accountService.getFullAccountInfo(cardNo);
        return Result.success(accountInfo);
    }

    /**
     * 余额查询接口
     * 兼容新老路径与参数名：
     * /api/atm/account/balance?token=xxx
     * /api/atm/accounts/balance?sessionId=xxx
     * @return 余额
     */
    @GetMapping("/balance")
    public Result<BalanceResponse> getBalance(
            @RequestParam(required = false) String token,
            @RequestParam(required = false) String sessionId
    ) {
        BigDecimal balance = accountService.getBalance(sessionValidator.validateAndGetCardNo(sessionId, token));
        BalanceResponse response = new BalanceResponse();
        response.setBalance(balance);
        return Result.success(response);
    }

    /**
     * 交易前身份和账户校验接口
     * 主路径：/api/atm/accounts/validate-transaction?sessionId=xxx
     * @return 交易验证结果
     */
    @GetMapping("/validate-transaction")
    public Result<TransactionValidationResponse> validateForTransaction(
            @RequestParam(required = false) String sessionId,
            @RequestParam(required = false) String token
    ) {
        String cardNo = sessionValidator.validateAndGetCardNo(sessionId, token);
        TransactionValidationResponse validation = accountService.validateForTransaction(cardNo);
        return Result.success(validation);
    }
}
