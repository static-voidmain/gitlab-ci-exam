package com.example.gradledemo.controller;

import com.example.gradledemo.service.AccountService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Test
    void getAccountReturnsComputedBalanceAndStatus() throws Exception {
        Mockito.when(accountService.convertBalanceToKrw(Mockito.any()))
                .thenReturn(new BigDecimal("59215.00"));
        Mockito.when(accountService.balanceStatus(Mockito.any()))
                .thenReturn("NORMAL");

        mockMvc.perform(get("/api/accounts/acct-1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("acct-1"))
                .andExpect(jsonPath("$.owner").value("Test Owner"))
                .andExpect(jsonPath("$.krwBalance").value(59215.00))
                .andExpect(jsonPath("$.status").value("NORMAL"));
    }
}
