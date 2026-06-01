package com.example.gradledemo.service;

import com.example.gradledemo.external.ExchangeRateClient;
import com.example.gradledemo.model.Account;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountService {
    private final ExchangeRateClient exchangeRateClient;

    public AccountService(ExchangeRateClient exchangeRateClient) {
        this.exchangeRateClient = exchangeRateClient;
    }

    public BigDecimal convertBalanceToKrw(Account account) {
        BigDecimal rate = exchangeRateClient.getUsdToKrwRate();
        return account.getBalance().multiply(rate);
    }

    public String balanceStatus(Account account) {
        if (account.getBalance().compareTo(BigDecimal.ZERO) < 0) {
            return "OVERDRAWN";
        }
        return "NORMAL";
    }
}
