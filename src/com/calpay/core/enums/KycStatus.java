package com.calpay.core.enums;

/**
 * KYC (Know Your Customer) verification status.
 * 
 * <p><b>Note:</b> Not currently used for school payments, but kept
 * for potential future compliance requirements or other CalPay modules.
 * 
 * @author CalPay Team
 * @version 2.0
 */
public enum KycStatus {
    NOT_STARTED("Not Started"),
    PENDING("Pending Verification"),
    IN_REVIEW("Under Review"),
    APPROVED("Approved"),
    REJECTED("Rejected"),
    EXPIRED("Expired");
    
    private final String displayName;
    
    KycStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}