package com.example.mavendemo.model;

import java.math.BigDecimal;

public class User {
    private final String id;
    private final String name;
    private final BigDecimal outstandingBalance;

    public User(String id, String name, BigDecimal outstandingBalance) {
        this.id = id;
        this.name = name;
        this.outstandingBalance = outstandingBalance;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getOutstandingBalance() {
        return outstandingBalance;
    }
}
