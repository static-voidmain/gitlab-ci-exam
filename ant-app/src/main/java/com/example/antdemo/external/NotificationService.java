package com.example.antdemo.external;

public interface NotificationService {
    boolean sendBillingReminder(String clientId, String message);
}
