package com.atm.atmserver.controller;

import com.atm.atmserver.common.GlobalExceptionHandler;
import com.atm.atmserver.dto.AccountInfoResponse;
import com.atm.atmserver.dto.FullAccountInfoResponse;
import com.atm.atmserver.dto.TransactionValidationResponse;
import com.atm.atmserver.service.AccountService;
import com.atm.atmserver.util.SessionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AccountService accountService;

    @Mock
    private SessionValidator sessionValidator;

    @InjectMocks
    private AccountController accountController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(accountController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void supportsLegacyProfilePathAndSessionIdParameter() throws Exception {
        AccountInfoResponse response = new AccountInfoResponse();
        response.setCustomerName("张三");
        response.setCardNo("6222020000000001");
        response.setAccountNo("ACC10001");

        given(sessionValidator.validateAndGetCardNo("session-1", null)).willReturn("6222020000000001");
        given(accountService.getAccountInfo("6222020000000001")).willReturn(response);

        mockMvc.perform(get("/accounts/profile").param("sessionId", "session-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.timestamp").isNumber())
                .andExpect(jsonPath("$.data.accountNo").value("ACC10001"));
    }

    @Test
    void returnsStructuredBalancePayload() throws Exception {
        given(sessionValidator.validateAndGetCardNo(null, "token-1")).willReturn("6222020000000001");
        given(accountService.getBalance("6222020000000001")).willReturn(new BigDecimal("5000.00"));

        mockMvc.perform(get("/account/balance").param("token", "token-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.balance").value(5000.0));
    }

    @Test
    void supportsFullInfoEndpointWithSessionId() throws Exception {
        FullAccountInfoResponse response = new FullAccountInfoResponse();
        response.setCustomerName("张三");
        response.setPhone("138****0000");
        response.setStatus("正常");

        given(sessionValidator.validateAndGetCardNo("session-1", null)).willReturn("6222020000000001");
        given(accountService.getFullAccountInfo("6222020000000001")).willReturn(response);

        mockMvc.perform(get("/accounts/full-info").param("sessionId", "session-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.phone").value("138****0000"))
                .andExpect(jsonPath("$.data.status").value("正常"));
    }

    @Test
    void supportsTransactionValidationEndpoint() throws Exception {
        TransactionValidationResponse response = new TransactionValidationResponse();
        response.setValid(true);
        response.setAccountNo("ACC10001");
        response.setMessage("验证通过");

        given(sessionValidator.validateAndGetCardNo("session-1", null)).willReturn("6222020000000001");
        given(accountService.validateForTransaction("6222020000000001")).willReturn(response);

        mockMvc.perform(get("/accounts/validate-transaction").param("sessionId", "session-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.valid").value(true))
                .andExpect(jsonPath("$.data.accountNo").value("ACC10001"));
    }
}
