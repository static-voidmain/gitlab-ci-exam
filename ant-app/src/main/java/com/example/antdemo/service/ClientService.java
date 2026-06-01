package com.example.antdemo.service;

import com.example.antdemo.external.NotificationService;
import com.example.antdemo.model.Client;
import com.example.antdemo.repository.ClientRepository;

import java.util.Optional;

public class ClientService {
    private final ClientRepository repository;
    private final NotificationService notificationService;

    public ClientService(ClientRepository repository, NotificationService notificationService) {
        this.repository = repository;
        this.notificationService = notificationService;
    }

    public boolean notifyClient(String clientId) {
        Optional<Client> client = repository.findById(clientId);
        if (client.isEmpty()) {
            return false;
        }
        return notificationService.sendBillingReminder(clientId, "Please check your account balance.");
    }
}
