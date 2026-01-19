package com.calpay.core.exceptions;

/**
 * Base exception for all CalPay platform exceptions.
 * 
 * <p>CalPay is a multi-industry payment platform. This base exception
 * is used across all industry modules (school, healthcare, retail, etc.).
 * 
 * <p><b>Industry Modules:</b>
 * <ul>
 *   <li>SchoolPay: School fee payments</li>
 *   <li>HealthPay: Medical bill payments (future)</li>
 *   <li>RetailPay: E-commerce payments (future)</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 1.0
 */
public class CalPayException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final String errorCode;
    
    public CalPayException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public CalPayException(String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    @Override
    public String toString() {
        return String.format("%s[code=%s, message=%s]",
            getClass().getSimpleName(), errorCode, getMessage());
    }
}