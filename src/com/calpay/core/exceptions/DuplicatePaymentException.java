package com.calpay.core.exceptions;

public class DuplicatePaymentException extends CalPayException {
    
    private static final long serialVersionUID = 1L;
    
    private final String idempotencyKey;
    private final String existingPaymentId;
    
    public DuplicatePaymentException(String idempotencyKey, String existingPaymentId) {
        super(
            String.format("Duplicate payment detected. Idempotency key: %s", idempotencyKey),
            "DUPLICATE_PAYMENT"
        );
        this.idempotencyKey = idempotencyKey;
        this.existingPaymentId = existingPaymentId;
    }
    
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getExistingPaymentId() { return existingPaymentId; }
}