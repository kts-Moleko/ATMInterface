package com.calpay.core.enums;

/**
 * South African provinces.
 * 
 * <p><b>All 9 Provinces:</b>
 * Gauteng, Western Cape, KwaZulu-Natal, Eastern Cape,
 * Limpopo, Mpumalanga, North West, Free State, Northern Cape
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public enum Province {
    GAUTENG("Gauteng", "GP"),
    WESTERN_CAPE("Western Cape", "WC"),
    KWAZULU_NATAL("KwaZulu-Natal", "KZN"),
    EASTERN_CAPE("Eastern Cape", "EC"),
    LIMPOPO("Limpopo", "LP"),
    MPUMALANGA("Mpumalanga", "MP"),
    NORTH_WEST("North West", "NW"),
    FREE_STATE("Free State", "FS"),
    NORTHERN_CAPE("Northern Cape", "NC");
    
    private final String displayName;
    private final String code;
    
    Province(String displayName, String code) {
        this.displayName = displayName;
        this.code = code;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getCode() {
        return code;
    }
    
    /**
     * Parse province from string (case-insensitive).
     * 
     * @param value province name or code
     * @return Province or null if not found
     */
    public static Province fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        String normalized = value.trim();
        
        for (Province province : values()) {
            if (province.displayName.equalsIgnoreCase(normalized) ||
                province.code.equalsIgnoreCase(normalized) ||
                province.name().equalsIgnoreCase(normalized)) {
                return province;
            }
        }
        
        return null;
    }
}