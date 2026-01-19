package com.calpay.core.exceptions;

import com.calpay.core.model.Money;

/**
 * Thrown when attempting to debit more money than available in an account.
 * 
 * <p><b>Why specific exception?</b> Allows different handling than other
 * transaction failures. UI can show specific "insufficient funds" message
 * and suggest adding money, rather than generic "transaction failed".
 * 
 * @author CalPay Team
 * @since 2.0
 */
public class InsufficientFundsException extends CalPayException {
    
    private static final long serialVersionUID = 1L;
    
    private final String accountId;
    private final Money availableBalance;
    private final Money requiredAmount;
    
    /**
     * Constructs exception with account details.
     * 
     * <p><b>Why include amounts?</b> Helps with debugging and
     * generating informative error messages for users.
     * 
     * @param accountId the account with insufficient funds
     * @param availableBalance current account balance
     * @param requiredAmount amount needed for transaction
     */
    public InsufficientFundsException(String accountId, Money availableBalance, Money requiredAmount) {
        super(
            String.format("Insufficient funds in account %s. Available: %s, Required: %s",
                accountId, availableBalance, requiredAmount),
            "INSUFFICIENT_FUNDS"
        );
        this.accountId = accountId;
        this.availableBalance = availableBalance;
        this.requiredAmount = requiredAmount;
    }
    
    public String getAccountId() {
        return accountId;
    }
    
    public Money getAvailableBalance() {
        return availableBalance;
    }
    
    public Money getRequiredAmount() {
        return requiredAmount;
    }
    
    /**
     * Returns the shortage amount (how much is missing).
     * 
     * @return the deficit amount
     */
    public Money getShortage() {
        return requiredAmount.subtract(availableBalance);
    }
}