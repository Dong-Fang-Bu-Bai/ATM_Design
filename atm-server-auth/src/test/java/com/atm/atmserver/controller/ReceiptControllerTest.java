package com.atm.atmserver.controller;

import com.atm.atmserver.common.GlobalExceptionHandler;
import com.atm.atmserver.dto.ReceiptResponse;
import com.atm.atmserver.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReceiptControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        ReceiptController receiptController = new ReceiptController(transactionService);
        mockMvc = MockMvcBuilders.standaloneSetup(receiptController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void receiptUsesTransactionIdAndSessionIdContract() throws Exception {
        ReceiptResponse response = new ReceiptResponse();
        response.setTransactionId("TX202605180001");
        response.setType("WITHDRAW");
        response.setAmount(new BigDecimal("100.00"));
        response.setBalanceAfter(new BigDecimal("4900.00"));
        response.setAccountNo("ACC10001");
        response.setTime(LocalDateTime.of(2026, 5, 18, 10, 30));

        given(transactionService.getReceipt(eq("TX202605180001"), eq("session-1"))).willReturn(response);

        mockMvc.perform(get("/receipts/TX202605180001")
                        .param("sessionId", "session-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.transactionId").value("TX202605180001"))
                .andExpect(jsonPath("$.data.type").value("WITHDRAW"))
                .andExpect(jsonPath("$.data.accountNo").value("ACC10001"));
    }
}
