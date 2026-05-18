package com.atm.atmserver.controller;

import com.atm.atmserver.common.Result;
import com.atm.atmserver.dto.ReceiptResponse;
import com.atm.atmserver.service.TransactionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/receipts")
public class ReceiptController {

    private final TransactionService transactionService;

    public ReceiptController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping("/{transactionId}")
    public Result<ReceiptResponse> getReceipt(
            @PathVariable String transactionId,
            @RequestParam String sessionId
    ) {
        return Result.success(transactionService.getReceipt(transactionId, sessionId));
    }
}
