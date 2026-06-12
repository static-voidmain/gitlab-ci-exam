package com.example.mavendemo.external;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class StubPaymentGatewayClientTest {
    @Test
    void authorizeChargeApprovesAmountsAtOrBelowLimit() {
        StubPaymentGatewayClient client = new StubPaymentGatewayClient();

        assertThat(client.authorizeCharge("user-1", new BigDecimal("200.00"))).isTrue();
        assertThat(client.authorizeCharge("user-1", new BigDecimal("199.99"))).isTrue();
    }

    @Test
    void authorizeChargeRejectsAmountsAboveLimit() {
        StubPaymentGatewayClient client = new StubPaymentGatewayClient();

        assertThat(client.authorizeCharge("user-1", new BigDecimal("200.01"))).isFalse();
    }
}
