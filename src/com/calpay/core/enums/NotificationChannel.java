package com.calpay.core.enums;

/**
 * Notification delivery channels.
 * 
 * <p><b>Priority Order:</b>
 * <ol>
 *   <li>WHATSAPP - Primary (cheapest, highest engagement)</li>
 *   <li>SMS - Fallback (more expensive, universal)</li>
 *   <li>EMAIL - Secondary (for receipts, statements)</li>
 * </ol>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public enum NotificationChannel {
    WHATSAPP("WhatsApp", 0.05),  // ~R0.05 per message
    SMS("SMS", 0.10),            // ~R0.10 per message
    EMAIL("Email", 0.0);         // Free
    
    private final String displayName;
    private final double costPerMessage;  // In ZAR
    
    NotificationChannel(String displayName, double costPerMessage) {
        this.displayName = displayName;
        this.costPerMessage = costPerMessage;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Returns cost per message in ZAR.
     * 
     * @return cost per message
     */
    public double getCostPerMessage() {
        return costPerMessage;
    }
    
    /**
     * Checks if channel is free.
     * 
     * @return true if EMAIL
     */
    public boolean isFree() {
        return this == EMAIL;
    }
}