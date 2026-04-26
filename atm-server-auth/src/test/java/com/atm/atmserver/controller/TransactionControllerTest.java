package com.atm.atmserver.controller;

import com.atm.atmserver.common.GlobalExceptionHandler;
import com.atm.atmserver.service.impl.TransactionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class TransactionControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TransactionController transactionController = new TransactionController(new TransactionServiceImpl());
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void transactionEndpointsAreMountedAsIterationSkeleton() throws Exception {
        mockMvc.perform(post("/transactions/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sessionId": "session-1",
                                  "amount": 100,
                                  "printReceipt": false
                                }
                                """))
                .andExpect(status().isNotImplemented())
                .andExpect(jsonPath("$.code").value(501))
                .andExpect(jsonPath("$.message").value("交易模块已接入后端，当前为第一次迭代骨架，完整业务将在第二次迭代实现"));
    }
}
