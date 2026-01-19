package com.calpay.core.exceptions;

/**
 * Thrown when authentication fails (invalid credentials).
 * 
 * <p><b>Security note:</b> Don't reveal whether username or password
 * was incorrect - just say "invalid credentials" to prevent username
 * enumeration attacks.
 * 
 * @author CalPay Team
 * @since 2.0
 */
public class AuthenticationException extends CalPayException {
    
    private static final long serialVersionUID = 1L;
    
    public AuthenticationException(String message) {
        super(message, "AUTHENTICATION_FAILED");
    }
    
    public AuthenticationException(String message, Throwable cause) {
        super(message, "AUTHENTICATION_FAILED", cause);
    }
}