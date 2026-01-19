package com.calpay.core.enums;

/**
 * Status of a payment transaction.
 * 
 * <p><b>Payment Lifecycle:</b>
 * <ol>
 *   <li>PENDING - Payment initiated, awaiting gateway response</li>
 *   <li>PROCESSING - Payment being processed by gateway</li>
 *   <li>COMPLETED - Payment successful</li>
 *   <li>FAILED - Payment failed (insufficient funds, declined, etc.)</li>
 *   <li>CANCELLED - Payment cancelled by user or system</li>
 *   <li>REFUNDED - Payment was refunded</li>
 * </ol>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public enum PaymentStatus {
    PENDING("Pending"),
    PROCESSING("Processing"),
    COMPLETED("Completed"),
    FAILED("Failed"),
    CANCELLED("Cancelled"),
    REFUNDED("Refunded");
    
    private final String displayName;
    
    PaymentStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Checks if payment is in a final state (no further changes).
     * 
     * @return true if COMPLETED, FAILED, CANCELLED, or REFUNDED
     */
    public boolean isFinal() {
        return this == COMPLETED || this == FAILED || this == CANCELLED || this == REFUNDED;
    }
    
    /**
     * Checks if payment is successful.
     * 
     * @return true if COMPLETED
     */
    public boolean isSuccessful() {
        return this == COMPLETED;
    }
    
    /**
     * Checks if payment can be retried.
     * 
     * @return true if FAILED or CANCELLED
     */
    public boolean canRetry() {
        return this == FAILED || this == CANCELLED;
    }
}