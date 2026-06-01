package com.example.mavendemo.external;

import java.math.BigDecimal;

public interface PaymentGatewayClient {
    boolean authorizeCharge(String userId, BigDecimal amount);
}
