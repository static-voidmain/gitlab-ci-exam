package com.example.antdemo;

import com.example.antdemo.model.Client;
import com.example.antdemo.repository.ClientRepository;
import com.example.antdemo.service.ClientService;
import com.example.antdemo.external.NotificationService;

import java.util.Optional;

public class App {
    public static void main(String[] args) {
        ClientRepository repository = clientId -> Optional.of(new Client(clientId, "Ant Demo"));
        NotificationService notificationService = (clientId, message) -> {
            System.out.println("Notification sent to " + clientId + ": " + message);
            return true;
        };
        ClientService service = new ClientService(repository, notificationService);
        boolean result = service.notifyClient("client-1");
        System.out.println("Notification success: " + result);
    }
}
