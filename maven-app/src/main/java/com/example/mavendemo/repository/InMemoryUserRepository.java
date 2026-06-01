package com.example.mavendemo.repository;

import com.example.mavendemo.model.User;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public class InMemoryUserRepository implements UserRepository {
    @Override
    public Optional<User> findById(String userId) {
        if ("user-1".equals(userId)) {
            return Optional.of(new User("user-1", "Alice", new BigDecimal("120.50")));
        }
        return Optional.empty();
    }
}
