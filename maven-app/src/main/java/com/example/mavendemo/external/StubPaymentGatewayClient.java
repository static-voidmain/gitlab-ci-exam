package com.example.mavendemo.external;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StubPaymentGatewayClient implements PaymentGatewayClient {
    @Override
    public boolean authorizeCharge(String userId, BigDecimal amount) {
        return amount.compareTo(new BigDecimal("200.00")) <= 0;
    }
}
