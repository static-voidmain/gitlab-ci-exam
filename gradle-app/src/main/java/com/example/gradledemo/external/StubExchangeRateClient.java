package com.example.gradledemo.external;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StubExchangeRateClient implements ExchangeRateClient {
    @Override
    public BigDecimal getUsdToKrwRate() {
        return new BigDecimal("1300.00");
    }
}
