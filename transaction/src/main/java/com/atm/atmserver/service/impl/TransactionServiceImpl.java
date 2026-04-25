package com.atm.atmserver.service.impl;

import com.atm.atmserver.dto.*;
import com.atm.atmserver.service.TransactionService;
import org.springframework.stereotype.Service;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Override
    public WithdrawResponse withdraw(WithdrawRequest request) {
        return null;
    }

    @Override
    public DepositResponse deposit(DepositRequest request) {
        return null;
    }

    @Override
    public TransferResponse transfer(TransferRequest request) {
        return null;
    }

    @Override
    public TransactionResponse getTransactionById(Long transactionId) {
        return null;
    }
}
