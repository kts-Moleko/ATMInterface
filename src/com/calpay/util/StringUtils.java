package com.calpay.util;

/**
 * String utility methods for CalPay.
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public final class StringUtils {
    
    private StringUtils() {
        // Utility class
    }
    
    /**
     * Truncates a string to a maximum length.
     * 
     * @param str string to truncate
     * @param maxLength maximum length
     * @return truncated string
     */
    public static String truncate(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
    
    /**
     * Capitalizes first letter of each word.
     * 
     * @param str string to capitalize
     * @return capitalized string
     */
    public static String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) return str;
        
        String[] words = str.split("\\s+");
        StringBuilder result = new StringBuilder();
        
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1).toLowerCase())
                      .append(" ");
            }
        }
        
        return result.toString().trim();
    }
    
    /**
     * Masks sensitive data (e.g., ID numbers, account numbers).
     * 
     * @param str string to mask
     * @param visibleChars number of visible characters at end
     * @return masked string
     */
    public static String mask(String str, int visibleChars) {
        if (str == null || str.length() <= visibleChars) {
            return str;
        }
        
        int maskLength = str.length() - visibleChars;
        return "*".repeat(maskLength) + str.substring(maskLength);
    }
    
    /**
     * Masks email address (shows first char and domain).
     * 
     * @param email email to mask
     * @return masked email
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];
        
        if (username.length() <= 2) {
            return username.charAt(0) + "***@" + domain;
        }
        
        return username.charAt(0) + "***" + username.charAt(username.length() - 1) + "@" + domain;
    }
    
    /**
     * Masks phone number (shows last 4 digits).
     * 
     * @param phone phone to mask
     * @return masked phone
     */
    public static String maskPhone(String phone) {
        return mask(phone, 4);
    }
    
    /**
     * Formats a currency amount.
     * 
     * @param amount amount
     * @return formatted string
     */
    public static String formatCurrency(double amount) {
        return String.format("ZAR %,.2f", amount);
    }
    
    /**
     * Removes all whitespace from a string.
     * 
     * @param str string
     * @return string without whitespace
     */
    public static String removeWhitespace(String str) {
        return str != null ? str.replaceAll("\\s+", "") : null;
    }
    
    /**
     * Checks if two strings are equal (case-insensitive, null-safe).
     * 
     * @param str1 first string
     * @param str2 second string
     * @return true if equal
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        if (str1 == null && str2 == null) return true;
        if (str1 == null || str2 == null) return false;
        return str1.equalsIgnoreCase(str2);
    }
    
    /**
     * Pads a string to a specified length with leading zeros.
     * 
     * @param str string to pad
     * @param length target length
     * @return padded string
     */
    public static String padZeros(String str, int length) {
        if (str == null) str = "";
        return String.format("%0" + length + "d", Integer.parseInt(str));
    }
}