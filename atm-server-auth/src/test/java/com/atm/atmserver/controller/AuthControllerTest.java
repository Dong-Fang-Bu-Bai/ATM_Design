package com.atm.atmserver.controller;

import com.atm.atmserver.common.ApiException;
import com.atm.atmserver.common.GlobalExceptionHandler;
import com.atm.atmserver.dto.ChangePasswordResponse;
import com.atm.atmserver.service.AuthService;
import com.atm.atmserver.util.SessionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;
    @Mock
    private SessionValidator sessionValidator;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void loginReturnsHttpUnauthorizedForBusinessErrors() throws Exception {
        given(authService.login(any())).willThrow(new ApiException(HttpStatus.UNAUTHORIZED, "密码错误"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cardNo": "6222020000000001",
                                  "password": "000000"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.message").value("密码错误"));
    }

    @Test
    void logoutAcceptsSessionIdField() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sessionId": "legacy-session"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        verify(authService).logout("legacy-session");
    }

    @Test
    void changePasswordUsesSessionIdFromRequestBody() throws Exception {
        ChangePasswordResponse response = new ChangePasswordResponse();
        response.setSuccess(true);
        response.setMessage("密码修改成功，请重新登录");

        given(sessionValidator.validateAndGetCardNo("session-1", null)).willReturn("6222020000000001");
        given(authService.changePassword(eq("6222020000000001"), any())).willReturn(response);

        mockMvc.perform(post("/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sessionId": "session-1",
                                  "oldPassword": "123456",
                                  "newPassword": "654321"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("密码修改成功，请重新登录"))
                .andExpect(jsonPath("$.timestamp").isNumber())
                .andExpect(jsonPath("$.data.success").value(true));

        verify(authService).changePassword(eq("6222020000000001"), any());
    }
}
