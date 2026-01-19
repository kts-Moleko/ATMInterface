package com.calpay.util;

import java.util.UUID;

/**
 * ID generation utility for CalPay entities.
 * 
 * <p><b>ID Formats:</b>
 * <ul>
 *   <li>UUID: For most entities (universally unique)</li>
 *   <li>Sequential codes: For user-facing identifiers (SCH001, PAR001)</li>
 *   <li>Payment references: For receipts (PAY-2025-001234)</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public final class IdGenerator {
    
    private IdGenerator() {
        // Utility class
    }
    
    /**
     * Generates a UUID.
     * 
     * @return UUID string
     */
    public static String generateUuid() {
        return UUID.randomUUID().toString();
    }
    
    /**
     * Generates a school code.
     * 
     * @param sequence sequence number
     * @return school code (SCH001, SCH002, etc.)
     */
    public static String generateSchoolCode(int sequence) {
        return String.format("SCH%03d", sequence);
    }
    
    /**
     * Generates a parent code.
     * 
     * @param sequence sequence number
     * @return parent code (PAR001, PAR002, etc.)
     */
    public static String generateParentCode(int sequence) {
        return String.format("PAR%03d", sequence);
    }
    
    /**
     * Generates a student code.
     * 
     * @param sequence sequence number
     * @return student code (STU001, STU002, etc.)
     */
    public static String generateStudentCode(int sequence) {
        return String.format("STU%03d", sequence);
    }
    
    /**
     * Generates a payment reference.
     * 
     * @param year year
     * @param month month
     * @param sequence sequence number
     * @return payment reference (PAY-2025-01-001234)
     */
    public static String generatePaymentReference(int year, int month, int sequence) {
        return String.format("PAY-%04d-%02d-%06d", year, month, sequence);
    }
    
    /**
     * Generates an account number (10 digits).
     * 
     * @return account number
     */
    public static String generateAccountNumber() {
        return String.format("%010d", System.currentTimeMillis() % 10000000000L);
    }
    
    /**
     * Generates an API key.
     * 
     * @return API key
     */
    public static String generateApiKey() {
        return "api_" + UUID.randomUUID().toString().replace("-", "");
    }
    
    /**
     * Generates a session ID.
     * 
     * @return session ID
     */
    public static String generateSessionId() {
        return "sess_" + UUID.randomUUID().toString().replace("-", "");
    }
}