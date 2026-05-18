package com.atm.atmserver.service;

import com.atm.atmserver.dto.*;

public interface TransactionService {
    WithdrawResponse withdraw(WithdrawRequest request);
    DepositResponse deposit(DepositRequest request);
    TransferResponse transfer(TransferRequest request);
    TransactionResponse getTransactionById(String transactionId);
    TransactionHistoryResponse getTransactionHistory(String sessionId, Integer page, Integer size);
    ReceiptResponse getReceipt(String transactionId, String sessionId);
}
