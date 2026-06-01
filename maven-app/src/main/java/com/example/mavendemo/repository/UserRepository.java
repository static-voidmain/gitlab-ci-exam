package com.example.mavendemo.repository;

import com.example.mavendemo.model.User;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(String userId);
}
