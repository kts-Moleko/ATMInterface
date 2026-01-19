package com.calpay.core.enums;

/**
 * Types of fees charged by schools.
 * 
 * <p><b>School-Specific Fee Categories:</b>
 * <ul>
 *   <li>TUITION - Regular school fees</li>
 *   <li>SPORT - Sports activities and equipment</li>
 *   <li>UNIFORM - School uniforms</li>
 *   <li>TRANSPORT - School bus/transport</li>
 *   <li>MEALS - School meals/cafeteria</li>
 *   <li>EXCURSION - Field trips</li>
 *   <li>TEXTBOOKS - Textbooks and learning materials</li>
 *   <li>STATIONERY - Pens, notebooks, etc.</li>
 *   <li>TECHNOLOGY - Computer lab fees, iPads</li>
 *   <li>EXTRA_CURRICULAR - Music, drama, clubs</li>
 *   <li>OTHER - Miscellaneous fees</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public enum FeeType {
    TUITION("Tuition Fees"),
    SPORT("Sport Fees"),
    UNIFORM("Uniform"),
    TRANSPORT("Transport"),
    MEALS("Meals"),
    EXCURSION("Excursion/Field Trip"),
    TEXTBOOKS("Textbooks"),
    STATIONERY("Stationery"),
    TECHNOLOGY("Technology Fees"),
    EXTRA_CURRICULAR("Extra-Curricular Activities"),
    LIBRARY("Library Fees"),
    HOSTEL("Hostel/Boarding"),
    REGISTRATION("Registration Fees"),
    EXAM("Examination Fees"),
    OTHER("Other Fees");
    
    private final String displayName;
    
    FeeType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Parse fee type from string (case-insensitive).
     * 
     * @param value string value
     * @return FeeType or null if not found
     */
    public static FeeType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        
        for (FeeType type : values()) {
            if (type.name().equalsIgnoreCase(value.trim()) ||
                type.displayName.equalsIgnoreCase(value.trim())) {
                return type;
            }
        }
        
        return null;
    }
}