package com.atm.atmserver.service.impl;

import com.atm.atmserver.common.ApiException;
import com.atm.atmserver.dto.*;
import com.atm.atmserver.entity.Account;
import com.atm.atmserver.entity.BankCard;
import com.atm.atmserver.entity.Transaction;
import com.atm.atmserver.mapper.AccountMapper;
import com.atm.atmserver.mapper.BankCardMapper;
import com.atm.atmserver.mapper.TransactionMapper;
import com.atm.atmserver.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private BankCardMapper bankCardMapper;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private TransactionMapper transactionMapper;

    private static final BigDecimal DAILY_WITHDRAW_LIMIT = new BigDecimal("20000");
    private static final BigDecimal SINGLE_WITHDRAW_LIMIT = new BigDecimal("5000");
    private static final BigDecimal SINGLE_DEPOSIT_LIMIT = new BigDecimal("50000");
    private static final BigDecimal SINGLE_TRANSFER_LIMIT = new BigDecimal("10000");
    private static final BigDecimal DAILY_TRANSFER_LIMIT = new BigDecimal("50000");

    @Override
    @Transactional
    public WithdrawResponse withdraw(WithdrawRequest request) {
        String cardNo = request.getCardNo();
        BigDecimal amount = request.getAmount();

        BankCard bankCard = bankCardMapper.selectByCardNo(cardNo);
        if (bankCard == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "卡号不存在");
        }

        Account account = accountMapper.selectById(bankCard.getAccountId());
        if (account == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "账户不存在");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "无效金额");
        }

        if (amount.remainder(new BigDecimal("100")).compareTo(BigDecimal.ZERO) != 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "取款金额必须为100的整数倍");
        }

        if (amount.compareTo(SINGLE_WITHDRAW_LIMIT) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "超单次取款限额（5000元）");
        }

        BigDecimal todayWithdraw = transactionMapper.sumTodayAmount(cardNo, Transaction.TYPE_WITHDRAW);
        if (todayWithdraw.add(amount).compareTo(DAILY_WITHDRAW_LIMIT) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "超单日取款限额（20000元）");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "余额不足");
        }

        BigDecimal balanceBefore = account.getBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(amount);

        Transaction transaction = new Transaction();
        transaction.setTransactionNo(generateTransactionNo());
        transaction.setAccountId(account.getId());
        transaction.setCardNo(cardNo);
        transaction.setTransactionType(Transaction.TYPE_WITHDRAW);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setTransactionStatus(Transaction.STATUS_PENDING);
        transaction.setDescription("ATM取款");
        transaction.setCreatedAt(LocalDateTime.now());
        transactionMapper.insert(transaction);

        int updateCount = accountMapper.subtractBalance(account.getId(), amount);
        if (updateCount == 0) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "取款失败");
        }

        transactionMapper.updateStatus(transaction.getId(), Transaction.STATUS_SUCCESS, balanceAfter, null, LocalDateTime.now());

        WithdrawResponse response = new WithdrawResponse();
        response.setTransactionNo(transaction.getTransactionNo());
        response.setSuccess(true);
        response.setMessage("取款成功");
        response.setRemainingBalance(balanceAfter);
        return response;
    }

    @Override
    @Transactional
    public DepositResponse deposit(DepositRequest request) {
        String cardNo = request.getCardNo();
        BigDecimal amount = request.getAmount();

        BankCard bankCard = bankCardMapper.selectByCardNo(cardNo);
        if (bankCard == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "卡号不存在");
        }

        Account account = accountMapper.selectById(bankCard.getAccountId());
        if (account == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "账户不存在");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "无效金额");
        }

        if (amount.compareTo(SINGLE_DEPOSIT_LIMIT) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "超单次存款限额（50000元）");
        }

        BigDecimal balanceBefore = account.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);

        Transaction transaction = new Transaction();
        transaction.setTransactionNo(generateTransactionNo());
        transaction.setAccountId(account.getId());
        transaction.setCardNo(cardNo);
        transaction.setTransactionType(Transaction.TYPE_DEPOSIT);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setTransactionStatus(Transaction.STATUS_PENDING);
        transaction.setDescription("ATM存款");
        transaction.setCreatedAt(LocalDateTime.now());
        transactionMapper.insert(transaction);

        accountMapper.addBalance(account.getId(), amount);

        transactionMapper.updateStatus(transaction.getId(), Transaction.STATUS_SUCCESS, balanceAfter, null, LocalDateTime.now());

        DepositResponse response = new DepositResponse();
        response.setTransactionNo(transaction.getTransactionNo());
        response.setSuccess(true);
        response.setMessage("存款成功");
        response.setUpdatedBalance(balanceAfter);
        return response;
    }

    @Override
    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        String cardNo = request.getCardNo();
        String targetAccountNo = request.getTargetAccountNo();
        BigDecimal amount = request.getAmount();

        BankCard bankCard = bankCardMapper.selectByCardNo(cardNo);
        if (bankCard == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "卡号不存在");
        }

        Account sourceAccount = accountMapper.selectById(bankCard.getAccountId());
        if (sourceAccount == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "账户不存在");
        }

        Account targetAccount = accountMapper.selectByAccountNo(targetAccountNo);
        if (targetAccount == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "目标账户不存在");
        }

        if (sourceAccount.getId().equals(targetAccount.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "不能向本人账户转账");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "无效金额");
        }

        if (amount.compareTo(SINGLE_TRANSFER_LIMIT) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "超单次转账限额（10000元）");
        }

        BigDecimal todayTransfer = transactionMapper.sumTodayAmount(cardNo, Transaction.TYPE_TRANSFER);
        if (todayTransfer.add(amount).compareTo(DAILY_TRANSFER_LIMIT) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "超单日转账限额（50000元）");
        }

        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "余额不足");
        }

        BigDecimal sourceBalanceBefore = sourceAccount.getBalance();
        BigDecimal sourceBalanceAfter = sourceBalanceBefore.subtract(amount);
        BigDecimal targetBalanceBefore = targetAccount.getBalance();
        BigDecimal targetBalanceAfter = targetBalanceBefore.add(amount);

        Transaction sourceTransaction = new Transaction();
        sourceTransaction.setTransactionNo(generateTransactionNo());
        sourceTransaction.setAccountId(sourceAccount.getId());
        sourceTransaction.setCardNo(cardNo);
        sourceTransaction.setTransactionType(Transaction.TYPE_TRANSFER);
        sourceTransaction.setAmount(amount);
        sourceTransaction.setBalanceBefore(sourceBalanceBefore);
        sourceTransaction.setBalanceAfter(sourceBalanceAfter);
        sourceTransaction.setTransactionStatus(Transaction.STATUS_PENDING);
        sourceTransaction.setTargetAccountNo(targetAccountNo);
        sourceTransaction.setTargetBank(request.getTargetBank());
        sourceTransaction.setDescription("转账支出");
        sourceTransaction.setCreatedAt(LocalDateTime.now());
        transactionMapper.insert(sourceTransaction);

        accountMapper.subtractBalance(sourceAccount.getId(), amount);
        accountMapper.addBalance(targetAccount.getId(), amount);

        Transaction targetTransaction = new Transaction();
        targetTransaction.setTransactionNo(generateTransactionNo());
        targetTransaction.setAccountId(targetAccount.getId());
        targetTransaction.setCardNo("SYSTEM");
        targetTransaction.setTransactionType(Transaction.TYPE_DEPOSIT);
        targetTransaction.setAmount(amount);
        targetTransaction.setBalanceBefore(targetBalanceBefore);
        targetTransaction.setBalanceAfter(targetBalanceAfter);
        targetTransaction.setTransactionStatus(Transaction.STATUS_SUCCESS);
        targetTransaction.setDescription("转账收入");
        targetTransaction.setCreatedAt(LocalDateTime.now());
        targetTransaction.setCompletedAt(LocalDateTime.now());
        transactionMapper.insert(targetTransaction);

        transactionMapper.updateStatus(sourceTransaction.getId(), Transaction.STATUS_SUCCESS, sourceBalanceAfter, null, LocalDateTime.now());

        TransferResponse response = new TransferResponse();
        response.setTransactionNo(sourceTransaction.getTransactionNo());
        response.setSuccess(true);
        response.setMessage("转账成功");
        response.setRemainingBalance(sourceBalanceAfter);
        return response;
    }

    @Override
    public TransactionResponse getTransactionById(Long transactionId) {
        Transaction transaction = transactionMapper.selectById(transactionId);
        if (transaction == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "交易记录不存在");
        }

        return convertToResponse(transaction);
    }

    private TransactionResponse convertToResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setTransactionNo(transaction.getTransactionNo());
        response.setCardNo(transaction.getCardNo());
        response.setTransactionType(transaction.getTransactionType());
        response.setTransactionTypeName(getTransactionTypeName(transaction.getTransactionType()));
        response.setAmount(transaction.getAmount());
        response.setBalanceBefore(transaction.getBalanceBefore());
        response.setBalanceAfter(transaction.getBalanceAfter());
        response.setTransactionStatus(transaction.getTransactionStatus());
        response.setTransactionStatusName(getTransactionStatusName(transaction.getTransactionStatus()));
        response.setTargetAccountNo(transaction.getTargetAccountNo());
        response.setTargetBank(transaction.getTargetBank());
        response.setFailureReason(transaction.getFailureReason());
        response.setDescription(transaction.getDescription());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setCompletedAt(transaction.getCompletedAt());
        return response;
    }

    private String getTransactionTypeName(Integer type) {
        if (type == null) return "未知";
        switch (type) {
            case 1: return "取款";
            case 2: return "存款";
            case 3: return "转账";
            default: return "未知";
        }
    }

    private String getTransactionStatusName(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待处理";
            case 1: return "成功";
            case 2: return "失败";
            case 3: return "已撤销";
            default: return "未知";
        }
    }

    private String generateTransactionNo() {
        return "TX" + System.currentTimeMillis() + String.format("%04d", (int) (Math.random() * 10000));
    }
}
