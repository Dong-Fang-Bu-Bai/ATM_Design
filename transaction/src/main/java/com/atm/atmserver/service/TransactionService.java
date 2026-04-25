package com.atm.atmserver.service;

import com.atm.atmserver.dto.*;

public interface TransactionService {
    WithdrawResponse withdraw(WithdrawRequest request);
    DepositResponse deposit(DepositRequest request);
    TransferResponse transfer(TransferRequest request);
    TransactionResponse getTransactionById(Long transactionId);
}
