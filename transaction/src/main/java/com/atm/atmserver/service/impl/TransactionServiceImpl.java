package com.atm.atmserver.service.impl;

import com.atm.atmserver.common.ApiException;
import com.atm.atmserver.dto.DepositRequest;
import com.atm.atmserver.dto.DepositResponse;
import com.atm.atmserver.dto.ReceiptResponse;
import com.atm.atmserver.dto.TransactionHistoryItem;
import com.atm.atmserver.dto.TransactionHistoryResponse;
import com.atm.atmserver.dto.TransactionResponse;
import com.atm.atmserver.dto.TransferRequest;
import com.atm.atmserver.dto.TransferResponse;
import com.atm.atmserver.dto.WithdrawRequest;
import com.atm.atmserver.dto.WithdrawResponse;
import com.atm.atmserver.entity.Account;
import com.atm.atmserver.entity.BankCard;
import com.atm.atmserver.entity.Transaction;
import com.atm.atmserver.mapper.AccountMapper;
import com.atm.atmserver.mapper.BankCardMapper;
import com.atm.atmserver.mapper.TransactionMapper;
import com.atm.atmserver.service.DeviceService;
import com.atm.atmserver.service.TransactionService;
import com.atm.atmserver.util.SessionValidator;
import com.atm.atmserver.util.TransactionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal DAILY_WITHDRAW_LIMIT = new BigDecimal("20000");
    private static final BigDecimal SINGLE_WITHDRAW_LIMIT = new BigDecimal("5000");
    private static final BigDecimal SINGLE_DEPOSIT_LIMIT = new BigDecimal("50000");
    private static final BigDecimal SINGLE_TRANSFER_LIMIT = new BigDecimal("10000");
    private static final BigDecimal DAILY_TRANSFER_LIMIT = new BigDecimal("50000");

    private final BankCardMapper bankCardMapper;
    private final AccountMapper accountMapper;
    private final TransactionMapper transactionMapper;
    private final SessionValidator sessionValidator;
    private final DeviceService deviceService;

    public TransactionServiceImpl(BankCardMapper bankCardMapper,
                                  AccountMapper accountMapper,
                                  TransactionMapper transactionMapper,
                                  SessionValidator sessionValidator,
                                  DeviceService deviceService) {
        this.bankCardMapper = bankCardMapper;
        this.accountMapper = accountMapper;
        this.transactionMapper = transactionMapper;
        this.sessionValidator = sessionValidator;
        this.deviceService = deviceService;
    }

    @Override
    @Transactional
    public WithdrawResponse withdraw(WithdrawRequest request) {
        requireRequest(request);
        BigDecimal amount = requirePositiveAmount(request.getAmount());
        AccountContext context = loadAccountContext(request.getSessionId());

        if (amount.remainder(HUNDRED).compareTo(BigDecimal.ZERO) != 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "取款金额必须为100的整数倍");
        }

        if (amount.compareTo(SINGLE_WITHDRAW_LIMIT) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "超单次取款限额（5000元）");
        }

        BigDecimal todayWithdraw = transactionMapper.sumTodayAmount(context.cardNo, Transaction.TYPE_WITHDRAW);
        if (todayWithdraw.add(amount).compareTo(DAILY_WITHDRAW_LIMIT) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "超单日取款限额（20000元）");
        }

        if (context.account.getBalance().compareTo(amount) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "余额不足");
        }
        deviceService.ensureCashAvailable(amount);

        BigDecimal balanceBefore = context.account.getBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(amount);
        Transaction transaction = createTransaction(context, Transaction.TYPE_WITHDRAW, amount, balanceBefore, balanceAfter);
        transaction.setDescription("ATM取款");
        transactionMapper.insert(transaction);

        int updateCount = accountMapper.subtractBalance(context.account.getId(), amount);
        if (updateCount == 0) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "取款失败");
        }
        deviceService.dispenseCash(amount);

        markSuccess(transaction, balanceAfter);

        WithdrawResponse response = new WithdrawResponse();
        response.setTransactionId(transaction.getTransactionId());
        response.setSuccess(true);
        response.setMessage("取款成功");
        response.setRemainingBalance(balanceAfter);
        return response;
    }

    @Override
    @Transactional
    public DepositResponse deposit(DepositRequest request) {
        requireRequest(request);
        BigDecimal amount = requirePositiveAmount(request.getAmount());
        AccountContext context = loadAccountContext(request.getSessionId());

        if (amount.compareTo(SINGLE_DEPOSIT_LIMIT) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "超单次存款限额（50000元）");
        }

        BigDecimal balanceBefore = context.account.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);
        Transaction transaction = createTransaction(context, Transaction.TYPE_DEPOSIT, amount, balanceBefore, balanceAfter);
        transaction.setDescription("ATM存款");
        transactionMapper.insert(transaction);

        int updateCount = accountMapper.addBalance(context.account.getId(), amount);
        if (updateCount == 0) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "存款失败");
        }

        markSuccess(transaction, balanceAfter);

        DepositResponse response = new DepositResponse();
        response.setTransactionId(transaction.getTransactionId());
        response.setSuccess(true);
        response.setMessage("存款成功");
        response.setUpdatedBalance(balanceAfter);
        return response;
    }

    @Override
    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        requireRequest(request);
        BigDecimal amount = requirePositiveAmount(request.getAmount());
        AccountContext sourceContext = loadAccountContext(request.getSessionId());

        if (!StringUtils.hasText(request.getTargetAccountNo())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "目标账户不能为空");
        }

        Account targetAccount = accountMapper.selectByAccountNo(request.getTargetAccountNo());
        if (targetAccount == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "目标账户不存在");
        }

        if (sourceContext.account.getId().equals(targetAccount.getId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "不能向本人账户转账");
        }

        if (amount.compareTo(SINGLE_TRANSFER_LIMIT) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "超单次转账限额（10000元）");
        }

        BigDecimal todayTransfer = transactionMapper.sumTodayAmount(sourceContext.cardNo, Transaction.TYPE_TRANSFER);
        if (todayTransfer.add(amount).compareTo(DAILY_TRANSFER_LIMIT) > 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "超单日转账限额（50000元）");
        }

        if (sourceContext.account.getBalance().compareTo(amount) < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "余额不足");
        }

        BigDecimal sourceBalanceBefore = sourceContext.account.getBalance();
        BigDecimal sourceBalanceAfter = sourceBalanceBefore.subtract(amount);
        BigDecimal targetBalanceBefore = targetAccount.getBalance();
        BigDecimal targetBalanceAfter = targetBalanceBefore.add(amount);

        Transaction sourceTransaction = createTransaction(
                sourceContext,
                Transaction.TYPE_TRANSFER,
                amount,
                sourceBalanceBefore,
                sourceBalanceAfter
        );
        sourceTransaction.setTargetAccountNo(request.getTargetAccountNo());
        sourceTransaction.setDescription("转账支出");
        transactionMapper.insert(sourceTransaction);

        int subtractCount = accountMapper.subtractBalance(sourceContext.account.getId(), amount);
        int addCount = accountMapper.addBalance(targetAccount.getId(), amount);
        if (subtractCount == 0 || addCount == 0) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "转账失败");
        }

        Transaction targetTransaction = new Transaction();
        targetTransaction.setTransactionId(TransactionUtils.generateTransactionId());
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

        markSuccess(sourceTransaction, sourceBalanceAfter);

        TransferResponse response = new TransferResponse();
        response.setTransactionId(sourceTransaction.getTransactionId());
        response.setSuccess(true);
        response.setMessage("转账成功");
        response.setRemainingBalance(sourceBalanceAfter);
        return response;
    }

    @Override
    public TransactionResponse getTransactionById(String transactionId) {
        if (!StringUtils.hasText(transactionId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "交易编号不能为空");
        }

        Transaction transaction = transactionMapper.selectByTransactionId(transactionId);
        if (transaction == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "交易记录不存在");
        }

        return convertToResponse(transaction);
    }

    @Override
    public TransactionHistoryResponse getTransactionHistory(String sessionId, Integer page, Integer size) {
        AccountContext context = loadAccountContext(sessionId);
        int currentPage = normalizePage(page);
        int pageSize = normalizeSize(size);
        int offset = (currentPage - 1) * pageSize;

        List<TransactionHistoryItem> records = transactionMapper
                .selectByAccountIdPaged(context.account.getId(), pageSize, offset)
                .stream()
                .map(this::convertToHistoryItem)
                .collect(Collectors.toList());

        TransactionHistoryResponse response = new TransactionHistoryResponse();
        response.setPage(currentPage);
        response.setSize(pageSize);
        response.setTotal(transactionMapper.countByAccountId(context.account.getId()));
        response.setRecords(records);
        return response;
    }

    @Override
    public ReceiptResponse getReceipt(String transactionId, String sessionId) {
        if (!StringUtils.hasText(transactionId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "交易编号不能为空");
        }

        AccountContext context = loadAccountContext(sessionId);
        Transaction transaction = transactionMapper.selectByTransactionId(transactionId);
        if (transaction == null || !context.account.getId().equals(transaction.getAccountId())) {
            throw new ApiException(HttpStatus.NOT_FOUND, "凭条不存在");
        }
        if (!Transaction.STATUS_SUCCESS.equals(transaction.getTransactionStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "仅成功交易可生成凭条");
        }

        ReceiptResponse response = new ReceiptResponse();
        response.setTransactionId(transaction.getTransactionId());
        response.setType(getTransactionTypeCode(transaction.getTransactionType()));
        response.setAmount(transaction.getAmount());
        response.setBalanceAfter(transaction.getBalanceAfter());
        response.setTime(transaction.getCompletedAt() == null ? transaction.getCreatedAt() : transaction.getCompletedAt());
        response.setAccountNo(context.account.getAccountNo());
        return response;
    }

    private void requireRequest(Object request) {
        if (request == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "请求体不能为空");
        }
    }

    private BigDecimal requirePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "无效金额");
        }
        return amount;
    }

    private AccountContext loadAccountContext(String sessionId) {
        String cardNo = sessionValidator.validateAndGetCardNo(sessionId, null);
        BankCard bankCard = bankCardMapper.selectByCardNo(cardNo);
        if (bankCard == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "卡号不存在");
        }

        Account account = accountMapper.selectById(bankCard.getAccountId());
        if (account == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "账户不存在");
        }

        return new AccountContext(cardNo, account);
    }

    private Transaction createTransaction(AccountContext context,
                                          Integer type,
                                          BigDecimal amount,
                                          BigDecimal balanceBefore,
                                          BigDecimal balanceAfter) {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(TransactionUtils.generateTransactionId());
        transaction.setAccountId(context.account.getId());
        transaction.setCardNo(context.cardNo);
        transaction.setTransactionType(type);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setTransactionStatus(Transaction.STATUS_PENDING);
        transaction.setCreatedAt(LocalDateTime.now());
        return transaction;
    }

    private void markSuccess(Transaction transaction, BigDecimal balanceAfter) {
        transaction.setTransactionStatus(Transaction.STATUS_SUCCESS);
        transaction.setCompletedAt(LocalDateTime.now());
        transactionMapper.updateStatus(
                transaction.getId(),
                Transaction.STATUS_SUCCESS,
                balanceAfter,
                null,
                transaction.getCompletedAt()
        );
    }

    private TransactionResponse convertToResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setTransactionId(transaction.getTransactionId());
        response.setTransactionType(transaction.getTransactionType());
        response.setTransactionTypeName(getTransactionTypeName(transaction.getTransactionType()));
        response.setAmount(transaction.getAmount());
        response.setBalanceBefore(transaction.getBalanceBefore());
        response.setBalanceAfter(transaction.getBalanceAfter());
        response.setTransactionStatus(transaction.getTransactionStatus());
        response.setTransactionStatusName(getTransactionStatusName(transaction.getTransactionStatus()));
        response.setTargetAccountNo(transaction.getTargetAccountNo());
        response.setFailureReason(transaction.getFailureReason());
        response.setDescription(transaction.getDescription());
        response.setCreatedAt(transaction.getCreatedAt());
        response.setCompletedAt(transaction.getCompletedAt());
        return response;
    }

    private TransactionHistoryItem convertToHistoryItem(Transaction transaction) {
        TransactionHistoryItem item = new TransactionHistoryItem();
        item.setTransactionId(transaction.getTransactionId());
        item.setTransactionType(getTransactionTypeCode(transaction.getTransactionType()));
        item.setAmount(transaction.getAmount());
        item.setTransactionStatus(getTransactionStatusCode(transaction.getTransactionStatus()));
        item.setTransactionTime(transaction.getCompletedAt() == null ? transaction.getCreatedAt() : transaction.getCompletedAt());
        return item;
    }

    private int normalizePage(Integer page) {
        if (page == null) {
            return 1;
        }
        if (page < 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "页码必须大于0");
        }
        return page;
    }

    private int normalizeSize(Integer size) {
        if (size == null) {
            return 10;
        }
        if (size < 1 || size > 50) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "分页大小必须在1到50之间");
        }
        return size;
    }

    private String getTransactionTypeCode(Integer type) {
        if (type == null) {
            return "UNKNOWN";
        }
        switch (type) {
            case 1:
                return "WITHDRAW";
            case 2:
                return "DEPOSIT";
            case 3:
                return "TRANSFER";
            default:
                return "UNKNOWN";
        }
    }

    private String getTransactionStatusCode(Integer status) {
        if (status == null) {
            return "UNKNOWN";
        }
        switch (status) {
            case 0:
                return "PENDING";
            case 1:
                return "SUCCESS";
            case 2:
                return "FAILED";
            case 3:
                return "CANCELLED";
            default:
                return "UNKNOWN";
        }
    }

    private String getTransactionTypeName(Integer type) {
        if (type == null) {
            return "未知";
        }
        switch (type) {
            case 1:
                return "取款";
            case 2:
                return "存款";
            case 3:
                return "转账";
            default:
                return "未知";
        }
    }

    private String getTransactionStatusName(Integer status) {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "待处理";
            case 1:
                return "成功";
            case 2:
                return "失败";
            case 3:
                return "已撤销";
            default:
                return "未知";
        }
    }

    private static class AccountContext {
        private final String cardNo;
        private final Account account;

        private AccountContext(String cardNo, Account account) {
            this.cardNo = cardNo;
            this.account = account;
        }
    }
}
