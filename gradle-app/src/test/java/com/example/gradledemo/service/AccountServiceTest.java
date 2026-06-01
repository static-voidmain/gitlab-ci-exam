package com.example.gradledemo.service;

import com.example.gradledemo.external.ExchangeRateClient;
import com.example.gradledemo.model.Account;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class AccountServiceTest {
    @Test
    void convertBalanceToKrwUsesExchangeRate() {
        ExchangeRateClient client = Mockito.mock(ExchangeRateClient.class);
        Mockito.when(client.getUsdToKrwRate()).thenReturn(new BigDecimal("1300.00"));

        AccountService service = new AccountService(client);
        Account account = new Account("acct-1", "Tester", new BigDecimal("10.00"));

        assertThat(service.convertBalanceToKrw(account)).isEqualByComparingTo(new BigDecimal("13000.00"));
    }

    @Test
    void balanceStatusReturnsOverdrawnForNegativeBalance() {
        ExchangeRateClient client = Mockito.mock(ExchangeRateClient.class);
        AccountService service = new AccountService(client);
        Account account = new Account("acct-2", "Tester", new BigDecimal("-5.00"));

        assertThat(service.balanceStatus(account)).isEqualTo("OVERDRAWN");
    }
}
