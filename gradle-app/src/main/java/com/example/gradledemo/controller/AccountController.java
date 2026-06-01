package com.example.gradledemo.controller;

import com.example.gradledemo.model.Account;
import com.example.gradledemo.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String id) {
        Account account = new Account(id, "Test Owner", new BigDecimal("45.50"));
        BigDecimal krw = accountService.convertBalanceToKrw(account);
        String status = accountService.balanceStatus(account);

        return ResponseEntity.ok(new AccountResponse(account.getId(), account.getOwner(), krw, status));
    }

    public static class AccountResponse {
        private final String id;
        private final String owner;
        private final BigDecimal krwBalance;
        private final String status;

        public AccountResponse(String id, String owner, BigDecimal krwBalance, String status) {
            this.id = id;
            this.owner = owner;
            this.krwBalance = krwBalance;
            this.status = status;
        }

        public String getId() {
            return id;
        }

        public String getOwner() {
            return owner;
        }

        public BigDecimal getKrwBalance() {
            return krwBalance;
        }

        public String getStatus() {
            return status;
        }
    }
}
