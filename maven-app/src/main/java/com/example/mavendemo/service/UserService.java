package com.example.mavendemo.service;

import com.example.mavendemo.external.PaymentGatewayClient;
import com.example.mavendemo.model.User;
import com.example.mavendemo.model.UserSummary;
import com.example.mavendemo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class UserService {
    private final UserRepository repository;
    private final PaymentGatewayClient paymentGatewayClient;

    public UserService(UserRepository repository, PaymentGatewayClient paymentGatewayClient) {
        this.repository = repository;
        this.paymentGatewayClient = paymentGatewayClient;
    }

    public UserSummary getUserSummary(String userId) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown user " + userId));

        boolean authorized = paymentGatewayClient.authorizeCharge(user.getId(), user.getOutstandingBalance());
        String status = authorized ? "ACTIVE" : "REVIEW";

        return new UserSummary(user.getId(), user.getName(), status);
    }

    public BigDecimal calculateFutureBalance(String userId, BigDecimal payment) {
        User user = repository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Unknown user " + userId));
        return user.getOutstandingBalance().subtract(payment);
    }
}
