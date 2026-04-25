package com.atm.atmserver.controller;

import com.atm.atmserver.common.Result;
import com.atm.atmserver.dto.*;
import com.atm.atmserver.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @PostMapping("/withdraw")
    public Result<WithdrawResponse> withdraw(@RequestBody WithdrawRequest request) {
        WithdrawResponse response = transactionService.withdraw(request);
        return Result.success(response);
    }

    @PostMapping("/deposit")
    public Result<DepositResponse> deposit(@RequestBody DepositRequest request) {
        DepositResponse response = transactionService.deposit(request);
        return Result.success(response);
    }

    @PostMapping("/transfer")
    public Result<TransferResponse> transfer(@RequestBody TransferRequest request) {
        TransferResponse response = transactionService.transfer(request);
        return Result.success(response);
    }

    @GetMapping("/{transactionId}")
    public Result<TransactionResponse> getTransaction(@PathVariable Long transactionId) {
        TransactionResponse response = transactionService.getTransactionById(transactionId);
        return Result.success(response);
    }
}
