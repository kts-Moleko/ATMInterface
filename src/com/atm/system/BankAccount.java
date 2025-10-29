package com.atm.system;

public class BankAccount {
	// Account balance
    private double balance;

    // Create account with initial balance
    public BankAccount(double initialBalance) {
        this.balance = initialBalance;
    }

    // Get current balance
    public double getBalance() {
        return balance;
    }

    // Deposit funds
    public void deposit(double amount) {
        if (amount > 0) {
            balance += amount;
        }
    }

    // Withdraw funds
    public boolean withdraw(double amount) {
        if (amount > 0 && amount <= balance) {
            balance -= amount;
            return true;
        }
        return false;
    }

    // Transfer funds to another account
    public boolean transfer(BankAccount recipient, double amount) {
        if (withdraw(amount)) {
            recipient.deposit(amount);
            return true;
        }
        return false;
    }
}
