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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @Test
    void getUserSummaryReturnsReviewWhenPaymentNotAuthorized() {
        UserRepository repository = Mockito.mock(UserRepository.class);
        PaymentGatewayClient paymentClient = Mockito.mock(PaymentGatewayClient.class);

        User sample = new User("user-3", "Carol", new BigDecimal("350.00"));
        Mockito.when(repository.findById("user-3")).thenReturn(Optional.of(sample));
        Mockito.when(paymentClient.authorizeCharge("user-3", sample.getOutstandingBalance())).thenReturn(false);

        UserService service = new UserService(repository, paymentClient);
        UserSummary summary = service.getUserSummary("user-3");

        assertThat(summary.getStatus()).isEqualTo("REVIEW");
        assertThat(summary.getId()).isEqualTo("user-3");
        assertThat(summary.getName()).isEqualTo("Carol");
    }

    @Test
    void getUserSummaryThrowsWhenUserMissing() {
        UserRepository repository = Mockito.mock(UserRepository.class);
        PaymentGatewayClient paymentClient = Mockito.mock(PaymentGatewayClient.class);

        Mockito.when(repository.findById("missing-user")).thenReturn(Optional.empty());

        UserService service = new UserService(repository, paymentClient);

        assertThatThrownBy(() -> service.getUserSummary("missing-user"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Unknown user missing-user");
    }
}
