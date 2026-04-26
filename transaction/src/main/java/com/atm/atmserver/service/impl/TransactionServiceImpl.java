package com.atm.atmserver.service.impl;

import com.atm.atmserver.common.ApiException;
import com.atm.atmserver.dto.*;
import com.atm.atmserver.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final String ITERATION_SKELETON_MESSAGE =
            "交易模块已接入后端，当前为第一次迭代骨架，完整业务将在第二次迭代实现";

    @Override
    public WithdrawResponse withdraw(WithdrawRequest request) {
        throw new ApiException(HttpStatus.NOT_IMPLEMENTED, ITERATION_SKELETON_MESSAGE);
    }

    @Override
    public DepositResponse deposit(DepositRequest request) {
        throw new ApiException(HttpStatus.NOT_IMPLEMENTED, ITERATION_SKELETON_MESSAGE);
    }

    @Override
    public TransferResponse transfer(TransferRequest request) {
        throw new ApiException(HttpStatus.NOT_IMPLEMENTED, ITERATION_SKELETON_MESSAGE);
    }

    @Override
    public TransactionResponse getTransactionById(String transactionId) {
        throw new ApiException(HttpStatus.NOT_IMPLEMENTED, ITERATION_SKELETON_MESSAGE);
    }
}
