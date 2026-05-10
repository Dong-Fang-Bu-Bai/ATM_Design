package com.atm.atmserver.controller;

import com.atm.atmserver.common.GlobalExceptionHandler;
import com.atm.atmserver.dto.WithdrawRequest;
import com.atm.atmserver.dto.WithdrawResponse;
import com.atm.atmserver.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        TransactionController transactionController = new TransactionController(transactionService);
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void withdrawUsesUnifiedSessionContract() throws Exception {
        WithdrawResponse response = new WithdrawResponse();
        response.setTransactionId("TX202605070001");
        response.setSuccess(true);
        response.setMessage("取款成功");
        response.setRemainingBalance(new BigDecimal("4900.00"));

        given(transactionService.withdraw(any(WithdrawRequest.class))).willReturn(response);

        mockMvc.perform(post("/transactions/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sessionId": "session-1",
                                  "amount": 100,
                                  "printReceipt": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.transactionId").value("TX202605070001"))
                .andExpect(jsonPath("$.data.success").value(true));
    }
}
