package com.atm.system;

public class User {
    // User credentials
    private String userId;
    private String pin;

    // Linked bank account
    private BankAccount account;

    // Create user with starting balance
    public User(String userId, String pin, double initialBalance) {
        this.userId = userId;
        this.pin = pin;
        this.account = new BankAccount(initialBalance);
    }

    // Get user ID
    public String getUserId() {
        return userId;
    }

    // Get user PIN
    public String getPin() {
        return pin;
    }

    // Get user's account
    public BankAccount getAccount() {
        return account;
    }

    // Show user info
    public void displayUserInfo() {
        System.out.println("User ID: " + userId);
        System.out.println("Account Balance: R" + account.getBalance());
    }
}


