package com.calpay.core.exceptions;

public class InvalidRequestException extends CalPayException {
    
    private static final long serialVersionUID = 1L;
    
    public InvalidRequestException(String message) {
        super(message, "INVALID_REQUEST");
    }
    
    public InvalidRequestException(String message, Throwable cause) {
        super(message, "INVALID_REQUEST", cause);
    }
}