package com.calpay.core.enums;

/**
 * Fee payment frequency.
 * 
 * <p><b>Frequency Options:</b>
 * <ul>
 *   <li>MONTHLY - Every month (e.g., tuition)</li>
 *   <li>QUARTERLY - Every 3 months (e.g., term fees)</li>
 *   <li>ANNUALLY - Once per year (e.g., registration)</li>
 *   <li>ONE_TIME - Single payment (e.g., uniform)</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public enum Frequency {
    MONTHLY("Monthly", 1),
    QUARTERLY("Quarterly", 3),
    ANNUALLY("Annually", 12),
    ONE_TIME("One-Time", 0);
    
    private final String displayName;
    private final int monthInterval;
    
    Frequency(String displayName, int monthInterval) {
        this.displayName = displayName;
        this.monthInterval = monthInterval;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Returns number of months between payments.
     * 
     * @return month interval (0 for ONE_TIME)
     */
    public int getMonthInterval() {
        return monthInterval;
    }
    
    /**
     * Checks if frequency is recurring.
     * 
     * @return true if not ONE_TIME
     */
    public boolean isRecurring() {
        return this != ONE_TIME;
    }
}