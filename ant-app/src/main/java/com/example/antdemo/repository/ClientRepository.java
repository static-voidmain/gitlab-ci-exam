package com.example.antdemo.repository;

import com.example.antdemo.model.Client;
import java.util.Optional;

public interface ClientRepository {
    Optional<Client> findById(String clientId);
}
