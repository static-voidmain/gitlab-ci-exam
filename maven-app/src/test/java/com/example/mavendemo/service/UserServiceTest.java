package com.example.mavendemo.service;

import com.example.mavendemo.external.PaymentGatewayClient;
import com.example.mavendemo.model.User;
import com.example.mavendemo.model.UserSummary;
import com.example.mavendemo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class UserServiceTest {
    @Test
    void getUserSummaryReturnsActiveWhenPaymentAuthorized() {
        UserRepository repository = Mockito.mock(UserRepository.class);
        PaymentGatewayClient paymentClient = Mockito.mock(PaymentGatewayClient.class);

        User sample = new User("user-1", "Alice", new BigDecimal("120.50"));
        Mockito.when(repository.findById("user-1")).thenReturn(Optional.of(sample));
        Mockito.when(paymentClient.authorizeCharge("user-1", sample.getOutstandingBalance())).thenReturn(true);

        UserService service = new UserService(repository, paymentClient);
        UserSummary summary = service.getUserSummary("user-1");

        assertThat(summary.getStatus()).isEqualTo("ACTIVE");
        assertThat(summary.getId()).isEqualTo("user-1");
        assertThat(summary.getName()).isEqualTo("Alice");
    }

    @Test
    void calculateFutureBalanceSubtractsPayment() {
        UserRepository repository = Mockito.mock(UserRepository.class);
        PaymentGatewayClient paymentClient = Mockito.mock(PaymentGatewayClient.class);

        User sample = new User("user-2", "Bob", new BigDecimal("250.00"));
        Mockito.when(repository.findById("user-2")).thenReturn(Optional.of(sample));

        UserService service = new UserService(repository, paymentClient);
        assertThat(service.calculateFutureBalance("user-2", new BigDecimal("50.00")))
                .isEqualByComparingTo(new BigDecimal("200.00"));
    }
}
