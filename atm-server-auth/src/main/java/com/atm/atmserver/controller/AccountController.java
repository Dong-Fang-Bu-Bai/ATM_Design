package com.atm.atmserver.controller;

import com.atm.atmserver.common.ApiException;
import com.atm.atmserver.common.Result;
import com.atm.atmserver.dto.AccountInfoResponse;
import com.atm.atmserver.dto.BalanceResponse;
import com.atm.atmserver.service.AccountService;
import com.atm.atmserver.util.TokenManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
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
    private TokenManager tokenManager; // 注入 Token 管理器

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
        String cardNo = resolveCardNo(token, sessionId);
        AccountInfoResponse accountInfo = accountService.getAccountInfo(cardNo);
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
        BigDecimal balance = accountService.getBalance(resolveCardNo(token, sessionId));
        BalanceResponse response = new BalanceResponse();
        response.setBalance(balance);
        return Result.success(response);
    }

    private String resolveCardNo(String token, String sessionId) {
        String resolvedToken = StringUtils.hasText(token) ? token : sessionId;
        if (!StringUtils.hasText(resolvedToken) || !tokenManager.isValidToken(resolvedToken)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Token 无效或未登录");
        }

        String cardNo = tokenManager.getCardNoByToken(resolvedToken);
        if (!StringUtils.hasText(cardNo)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Token 无效或未登录");
        }
        return cardNo;
    }
}
