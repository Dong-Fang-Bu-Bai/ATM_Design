package com.atm.atmserver.service;

import com.atm.atmserver.common.ApiException;
import com.atm.atmserver.dto.DepositRequest;
import com.atm.atmserver.dto.DepositResponse;
import com.atm.atmserver.dto.DeviceStatusResponse;
import com.atm.atmserver.dto.ReceiptResponse;
import com.atm.atmserver.dto.TransactionHistoryResponse;
import com.atm.atmserver.dto.TransactionResponse;
import com.atm.atmserver.dto.TransferRequest;
import com.atm.atmserver.dto.TransferResponse;
import com.atm.atmserver.dto.WithdrawRequest;
import com.atm.atmserver.dto.WithdrawResponse;
import com.atm.atmserver.entity.Account;
import com.atm.atmserver.entity.Transaction;
import com.atm.atmserver.mapper.AccountMapper;
import com.atm.atmserver.service.DeviceService;
import com.atm.atmserver.util.TokenManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("dev")
@Sql(scripts = {"/dev/schema.sql", "/dev/data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class TransactionServiceIntegrationTest {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountMapper accountMapper;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private TokenManager tokenManager;

    @Test
    void successfulWithdrawAndDepositUpdateBalanceAndPersistDetails() {
        String sessionId = loginPrimaryCard();

        WithdrawRequest withdrawRequest = new WithdrawRequest();
        withdrawRequest.setSessionId(sessionId);
        withdrawRequest.setAmount(new BigDecimal("100.00"));
        withdrawRequest.setPrintReceipt(false);

        WithdrawResponse withdrawResponse = transactionService.withdraw(withdrawRequest);

        assertTrue(withdrawResponse.getSuccess());
        assertNotNull(withdrawResponse.getTransactionId());
        assertMoney("4900.00", withdrawResponse.getRemainingBalance());
        assertMoney("4900.00", accountMapper.selectByAccountNo("ACC10001").getBalance());
        assertMoney("29900.00", deviceService.getStatus().getCashAvailable());

        TransactionResponse withdrawDetail = transactionService.getTransactionById(withdrawResponse.getTransactionId());
        assertEquals(Transaction.TYPE_WITHDRAW, withdrawDetail.getTransactionType());
        assertEquals(Transaction.STATUS_SUCCESS, withdrawDetail.getTransactionStatus());
        assertMoney("100.00", withdrawDetail.getAmount());

        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setSessionId(sessionId);
        depositRequest.setAmount(new BigDecimal("200.00"));
        depositRequest.setPrintReceipt(false);

        DepositResponse depositResponse = transactionService.deposit(depositRequest);

        assertTrue(depositResponse.getSuccess());
        assertNotNull(depositResponse.getTransactionId());
        assertMoney("5100.00", depositResponse.getUpdatedBalance());
        assertMoney("5100.00", accountMapper.selectByAccountNo("ACC10001").getBalance());
        assertMoney("29900.00", deviceService.getStatus().getCashAvailable());

        TransactionResponse depositDetail = transactionService.getTransactionById(depositResponse.getTransactionId());
        assertEquals(Transaction.TYPE_DEPOSIT, depositDetail.getTransactionType());
        assertEquals(Transaction.STATUS_SUCCESS, depositDetail.getTransactionStatus());
        assertMoney("200.00", depositDetail.getAmount());
    }

    @Test
    void withdrawRejectsInsufficientBalanceWithoutChangingBalance() {
        String sessionId = loginPrimaryCard();

        WithdrawRequest drainBalanceRequest = new WithdrawRequest();
        drainBalanceRequest.setSessionId(sessionId);
        drainBalanceRequest.setAmount(new BigDecimal("5000.00"));
        drainBalanceRequest.setPrintReceipt(false);
        transactionService.withdraw(drainBalanceRequest);

        WithdrawRequest insufficientRequest = new WithdrawRequest();
        insufficientRequest.setSessionId(sessionId);
        insufficientRequest.setAmount(new BigDecimal("100.00"));
        insufficientRequest.setPrintReceipt(false);

        ApiException exception = assertThrows(ApiException.class, () -> transactionService.withdraw(insufficientRequest));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("余额不足", exception.getMessage());
        assertMoney("0.00", accountMapper.selectByAccountNo("ACC10001").getBalance());
    }

    @Test
    void transferRejectsMissingTargetAccountWithoutChangingSourceBalance() {
        String sessionId = loginPrimaryCard();

        TransferRequest request = new TransferRequest();
        request.setSessionId(sessionId);
        request.setTargetAccountNo("ACC99999");
        request.setAmount(new BigDecimal("100.00"));
        request.setPrintReceipt(false);

        ApiException exception = assertThrows(ApiException.class, () -> transactionService.transfer(request));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("目标账户不存在", exception.getMessage());
        assertMoney("5000.00", accountMapper.selectByAccountNo("ACC10001").getBalance());
    }

    @Test
    void transferKeepsSourceAndTargetBalancesConsistent() {
        String sessionId = loginPrimaryCard();
        Account sourceBefore = accountMapper.selectByAccountNo("ACC10001");
        Account targetBefore = accountMapper.selectByAccountNo("ACC20001");

        TransferRequest request = new TransferRequest();
        request.setSessionId(sessionId);
        request.setTargetAccountNo("ACC20001");
        request.setAmount(new BigDecimal("800.00"));
        request.setPrintReceipt(false);

        TransferResponse response = transactionService.transfer(request);

        assertTrue(response.getSuccess());
        assertNotNull(response.getTransactionId());
        assertMoney("4200.00", response.getRemainingBalance());

        Account sourceAfter = accountMapper.selectByAccountNo("ACC10001");
        Account targetAfter = accountMapper.selectByAccountNo("ACC20001");
        assertMoney(sourceBefore.getBalance().subtract(new BigDecimal("800.00")), sourceAfter.getBalance());
        assertMoney(targetBefore.getBalance().add(new BigDecimal("800.00")), targetAfter.getBalance());

        TransactionResponse detail = transactionService.getTransactionById(response.getTransactionId());
        assertEquals(Transaction.TYPE_TRANSFER, detail.getTransactionType());
        assertEquals(Transaction.STATUS_SUCCESS, detail.getTransactionStatus());
        assertEquals("ACC20001", detail.getTargetAccountNo());
        assertMoney("800.00", detail.getAmount());
        assertMoney("4200.00", detail.getBalanceAfter());
    }

    @Test
    void historyAndReceiptUseCurrentSessionAccountScope() {
        String primarySessionId = loginPrimaryCard();
        String secondarySessionId = tokenManager.generateToken("6222020000000002");

        WithdrawRequest withdrawRequest = new WithdrawRequest();
        withdrawRequest.setSessionId(primarySessionId);
        withdrawRequest.setAmount(new BigDecimal("100.00"));
        withdrawRequest.setPrintReceipt(true);
        WithdrawResponse withdrawResponse = transactionService.withdraw(withdrawRequest);

        DepositRequest depositRequest = new DepositRequest();
        depositRequest.setSessionId(primarySessionId);
        depositRequest.setAmount(new BigDecimal("200.00"));
        depositRequest.setPrintReceipt(false);
        transactionService.deposit(depositRequest);

        TransactionHistoryResponse history = transactionService.getTransactionHistory(primarySessionId, 1, 5);
        assertEquals(2, history.getTotal());
        assertEquals(2, history.getRecords().size());
        assertEquals("DEPOSIT", history.getRecords().get(0).getTransactionType());
        assertEquals("SUCCESS", history.getRecords().get(0).getTransactionStatus());

        ReceiptResponse receipt = transactionService.getReceipt(withdrawResponse.getTransactionId(), primarySessionId);
        assertEquals(withdrawResponse.getTransactionId(), receipt.getTransactionId());
        assertEquals("WITHDRAW", receipt.getType());
        assertEquals("ACC10001", receipt.getAccountNo());
        assertMoney("100.00", receipt.getAmount());

        ApiException exception = assertThrows(
                ApiException.class,
                () -> transactionService.getReceipt(withdrawResponse.getTransactionId(), secondarySessionId)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("凭条不存在", exception.getMessage());
    }

    @Test
    void deviceCashCheckReflectsAvailableCash() {
        DeviceStatusResponse status = deviceService.getStatus();
        assertEquals("ATM001", status.getAtmCode());
        assertEquals("RUNNING", status.getStatus());
        assertMoney("30000.00", status.getCashAvailable());

        deviceService.ensureCashAvailable(new BigDecimal("30000.00"));
        ApiException exception = assertThrows(
                ApiException.class,
                () -> deviceService.ensureCashAvailable(new BigDecimal("30100.00"))
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        assertEquals("ATM现金不足", exception.getMessage());
    }

    private String loginPrimaryCard() {
        return tokenManager.generateToken("6222020000000001");
    }

    private void assertMoney(String expected, BigDecimal actual) {
        assertMoney(new BigDecimal(expected), actual);
    }

    private void assertMoney(BigDecimal expected, BigDecimal actual) {
        assertNotNull(actual);
        assertEquals(0, expected.compareTo(actual), "expected " + expected + " but was " + actual);
    }
}
