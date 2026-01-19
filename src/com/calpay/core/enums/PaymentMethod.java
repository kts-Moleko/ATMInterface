package com.calpay.core.enums;

/**
 * Payment methods supported by CalPay.
 * 
 * <p><b>South African Payment Methods:</b>
 * <ul>
 *   <li>INSTANT_EFT - Instant EFT (Capitec, FNB, etc.)</li>
 *   <li>BANK_TRANSFER - Manual bank transfer/EFT</li>
 *   <li>CARD - Credit/Debit card</li>
 *   <li>CASH - Cash payment at school</li>
 *   <li>MOBILE_MONEY - Mobile money (future: SnapScan, Zapper)</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public enum PaymentMethod {
    INSTANT_EFT("Instant EFT", true),
    BANK_TRANSFER("Bank Transfer/EFT", false),
    CARD("Credit/Debit Card", true),
    CASH("Cash", false),
    MOBILE_MONEY("Mobile Money", true);
    
    private final String displayName;
    private final boolean requiresOnlineGateway;
    
    PaymentMethod(String displayName, boolean requiresOnlineGateway) {
        this.displayName = displayName;
        this.requiresOnlineGateway = requiresOnlineGateway;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Checks if payment method requires online payment gateway.
     * 
     * @return true if gateway integration needed
     */
    public boolean requiresOnlineGateway() {
        return requiresOnlineGateway;
    }
    
    /**
     * Checks if payment is instant (real-time verification).
     * 
     * @return true if INSTANT_EFT, CARD, or MOBILE_MONEY
     */
    public boolean isInstant() {
        return this == INSTANT_EFT || this == CARD || this == MOBILE_MONEY;
    }
    
    /**
     * Checks if payment requires manual verification.
     * 
     * @return true if BANK_TRANSFER or CASH
     */
    public boolean requiresManualVerification() {
        return this == BANK_TRANSFER || this == CASH;
    }
    
    /**
     * Checks if payment is electronic.
     * 
     * @return true if not CASH
     */
    public boolean isElectronic() {
        return this != CASH;
    }
}