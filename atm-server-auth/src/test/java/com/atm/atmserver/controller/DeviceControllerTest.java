package com.atm.atmserver.controller;

import com.atm.atmserver.common.GlobalExceptionHandler;
import com.atm.atmserver.dto.CashCheckRequest;
import com.atm.atmserver.dto.CashCheckResponse;
import com.atm.atmserver.dto.DeviceStatusResponse;
import com.atm.atmserver.service.DeviceService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DeviceControllerTest {

    private MockMvc mockMvc;

    @Mock
    private DeviceService deviceService;

    @BeforeEach
    void setUp() {
        DeviceController deviceController = new DeviceController(deviceService);
        mockMvc = MockMvcBuilders.standaloneSetup(deviceController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void statusReturnsAtmDeviceState() throws Exception {
        DeviceStatusResponse response = new DeviceStatusResponse();
        response.setAtmCode("ATM001");
        response.setLocation("一号教学楼大厅");
        response.setStatus("RUNNING");
        response.setCashAvailable(new BigDecimal("30000.00"));

        given(deviceService.getStatus()).willReturn(response);

        mockMvc.perform(get("/device/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.atmCode").value("ATM001"))
                .andExpect(jsonPath("$.data.status").value("RUNNING"));
    }

    @Test
    void cashCheckUsesAmountRequestBody() throws Exception {
        CashCheckResponse response = new CashCheckResponse();
        response.setAvailable(true);
        response.setAmount(new BigDecimal("1000.00"));
        response.setCashAvailable(new BigDecimal("30000.00"));

        given(deviceService.checkCashAvailability(any(CashCheckRequest.class))).willReturn(response);

        mockMvc.perform(post("/device/cash-check")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "amount": 1000
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.available").value(true))
                .andExpect(jsonPath("$.data.amount").value(1000.00));
    }
}
