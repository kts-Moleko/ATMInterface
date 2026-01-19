package com.calpay.core.enums;

/**
 * Status of an account (user, parent, school, etc.).
 * 
 * <p><b>Generic enum:</b> Reusable across different account types in CalPay.
 * 
 * @author CalPay Team
 * @version 2.0
 */
public enum AccountStatus {
    PENDING("Pending Verification"),
    ACTIVE("Active"),
    SUSPENDED("Suspended"),
    CLOSED("Closed");
    
    private final String displayName;
    
    AccountStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}