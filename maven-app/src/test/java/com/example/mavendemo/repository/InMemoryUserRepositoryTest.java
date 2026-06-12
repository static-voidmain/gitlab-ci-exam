package com.example.mavendemo.repository;

import com.example.mavendemo.model.User;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryUserRepositoryTest {
    @Test
    void findByIdReturnsKnownUser() {
        InMemoryUserRepository repository = new InMemoryUserRepository();

        assertThat(repository.findById("user-1"))
                .hasValueSatisfying(user -> {
                    assertThat(user.getId()).isEqualTo("user-1");
                    assertThat(user.getName()).isEqualTo("Alice");
                    assertThat(user.getOutstandingBalance()).isEqualByComparingTo("120.50");
                });
    }

    @Test
    void findByIdReturnsEmptyForUnknownUser() {
        InMemoryUserRepository repository = new InMemoryUserRepository();

        assertThat(repository.findById("missing-user")).isEmpty();
    }
}
