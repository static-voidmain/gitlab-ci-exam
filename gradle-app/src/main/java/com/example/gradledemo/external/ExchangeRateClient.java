package com.example.gradledemo.external;

import java.math.BigDecimal;

public interface ExchangeRateClient {
    BigDecimal getUsdToKrwRate();
}
