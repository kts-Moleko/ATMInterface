package com.calpay.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Validation utility methods for CalPay.
 * 
 * <p><b>Validation Categories:</b>
 * <ul>
 *   <li>South African specific: ID numbers, phone numbers, postal codes</li>
 *   <li>Financial: Amounts, account numbers</li>
 *   <li>General: Email, names, dates</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public final class ValidationUtils {
    
    // Email pattern (RFC 5322 simplified)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    // South African phone: +27XXXXXXXXX or 0XXXXXXXXX
    private static final Pattern SA_PHONE_PATTERN = Pattern.compile(
        "^(\\+27|0)[0-9]{9}$"
    );
    
    // South African postal code: 4 digits
    private static final Pattern SA_POSTAL_CODE_PATTERN = Pattern.compile(
        "^\\d{4}$"
    );
    
    // Account number: 10 digits
    private static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile(
        "^\\d{10}$"
    );
    
    // School code: SCH + 3-6 digits
    private static final Pattern SCHOOL_CODE_PATTERN = Pattern.compile(
        "^SCH\\d{3,6}$"
    );
    
    // Grade level: "Grade R" or "Grade 1-12"
    private static final Pattern GRADE_LEVEL_PATTERN = Pattern.compile(
        "^Grade (R|[1-9]|1[0-2])$"
    );
    
    private ValidationUtils() {
        // Utility class, prevent instantiation
    }
    
    /**
     * Validates an email address.
     * 
     * @param email email to validate
     * @return true if valid
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * Validates a South African phone number.
     * 
     * @param phone phone number
     * @return true if valid
     */
    public static boolean isValidSAPhone(String phone) {
        return phone != null && SA_PHONE_PATTERN.matcher(normalizePhone(phone)).matches();
    }
    
    /**
     * Normalizes a phone number to +27XXXXXXXXX format.
     * 
     * @param phone phone number
     * @return normalized phone
     */
    public static String normalizePhone(String phone) {
        if (phone == null) return null;
        
        String cleaned = phone.replaceAll("[\\s-()]", "");
        
        if (cleaned.startsWith("0")) {
            return "+27" + cleaned.substring(1);
        }
        
        if (!cleaned.startsWith("+")) {
            return "+" + cleaned;
        }
        
        return cleaned;
    }
    
    /**
     * Validates a South African postal code.
     * 
     * @param postalCode postal code
     * @return true if valid
     */
    public static boolean isValidSAPostalCode(String postalCode) {
        return postalCode != null && SA_POSTAL_CODE_PATTERN.matcher(postalCode).matches();
    }
    
    /**
     * Validates a South African ID number.
     * 
     * <p><b>Format:</b> YYMMDDGSSSCAZ
     * <ul>
     *   <li>YYMMDD: Date of birth</li>
     *   <li>G: Gender (0-4=female, 5-9=male)</li>
     *   <li>SSS: Sequence number</li>
     *   <li>C: Citizenship (0=SA, 1=non-SA)</li>
     *   <li>A: Usually 8 or 9</li>
     *   <li>Z: Checksum digit</li>
     * </ul>
     * 
     * @param idNumber SA ID number
     * @return true if valid
     */
    public static boolean isValidSAIdNumber(String idNumber) {
        if (idNumber == null || idNumber.length() != 13) {
            return false;
        }
        
        if (!idNumber.matches("\\d{13}")) {
            return false;
        }
        
        // Validate date of birth portion
        try {
            int year = Integer.parseInt(idNumber.substring(0, 2));
            int month = Integer.parseInt(idNumber.substring(2, 4));
            int day = Integer.parseInt(idNumber.substring(4, 6));
            
            // Determine century (assume current century for years 00-current year, previous century otherwise)
            int currentYear = LocalDate.now().getYear() % 100;
            int fullYear = year <= currentYear ? 2000 + year : 1900 + year;
            
            LocalDate.of(fullYear, month, day); // Will throw if invalid
            
        } catch (Exception e) {
            return false;
        }
        
        // Validate checksum using Luhn algorithm
        return validateLuhnChecksum(idNumber);
    }
    
    /**
     * Validates Luhn checksum (used in SA ID numbers, credit cards).
     * 
     * @param number number to validate
     * @return true if checksum valid
     */
    private static boolean validateLuhnChecksum(String number) {
        int sum = 0;
        boolean alternate = false;
        
        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return sum % 10 == 0;
    }
    
    /**
     * Validates an account number.
     * 
     * @param accountNumber account number
     * @return true if valid
     */
    public static boolean isValidAccountNumber(String accountNumber) {
        return accountNumber != null && ACCOUNT_NUMBER_PATTERN.matcher(accountNumber).matches();
    }
    
    /**
     * Validates a school code.
     * 
     * @param schoolCode school code
     * @return true if valid
     */
    public static boolean isValidSchoolCode(String schoolCode) {
        return schoolCode != null && SCHOOL_CODE_PATTERN.matcher(schoolCode).matches();
    }
    
    /**
     * Validates a grade level.
     * 
     * @param gradeLevel grade level
     * @return true if valid
     */
    public static boolean isValidGradeLevel(String gradeLevel) {
        return gradeLevel != null && GRADE_LEVEL_PATTERN.matcher(gradeLevel).matches();
    }
    
    /**
     * Validates a person's name.
     * 
     * @param name name to validate
     * @return true if valid
     */
    public static boolean isValidName(String name) {
        return name != null && 
               !name.trim().isEmpty() && 
               name.length() >= 2 && 
               name.length() <= 100;
    }
    
    /**
     * Validates a date string.
     * 
     * @param dateStr date string (ISO format: yyyy-MM-dd)
     * @return true if valid
     */
    public static boolean isValidDate(String dateStr) {
        if (dateStr == null) return false;
        
        try {
            LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    
    /**
     * Validates a datetime string.
     * 
     * @param dateTimeStr datetime string (ISO format: yyyy-MM-ddTHH:mm:ss)
     * @return true if valid
     */
    public static boolean isValidDateTime(String dateTimeStr) {
        if (dateTimeStr == null) return false;
        
        try {
            LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    
    /**
     * Checks if a string is null or empty.
     * 
     * @param str string to check
     * @return true if null or empty
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    /**
     * Checks if a string is not empty.
     * 
     * @param str string to check
     * @return true if not empty
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }
}