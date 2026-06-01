package com.example.antdemo.service;

import com.example.antdemo.external.NotificationService;
import com.example.antdemo.model.Client;
import com.example.antdemo.repository.ClientRepository;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ClientServiceTest {

    @Test
    public void notifyClientReturnsTrueWhenClientExistsAndNotificationSucceeds() {
        ClientRepository repository = Mockito.mock(ClientRepository.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Mockito.when(repository.findById("client-1"))
                .thenReturn(Optional.of(new Client("client-1", "Ant User")));
        Mockito.when(notificationService.sendBillingReminder(Mockito.eq("client-1"), Mockito.anyString()))
                .thenReturn(true);

        ClientService service = new ClientService(repository, notificationService);
        assertTrue(service.notifyClient("client-1"));
    }

    @Test
    public void notifyClientReturnsFalseWhenClientMissing() {
        ClientRepository repository = Mockito.mock(ClientRepository.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        Mockito.when(repository.findById("client-2")).thenReturn(Optional.empty());

        ClientService service = new ClientService(repository, notificationService);
        assertFalse(service.notifyClient("client-2"));
    }
}
