package com.calpay.core.exceptions;

public class PaymentFailedException extends CalPayException {
    
    private static final long serialVersionUID = 1L;
    
    private final String paymentReference;
    private final String gatewayErrorCode;
    
    public PaymentFailedException(String paymentReference, String message, String gatewayErrorCode) {
        super(
            String.format("Payment failed: %s (Payment: %s, Code: %s)",
                message, paymentReference, gatewayErrorCode),
            "PAYMENT_FAILED"
        );
        this.paymentReference = paymentReference;
        this.gatewayErrorCode = gatewayErrorCode;
    }
    
    public PaymentFailedException(String paymentReference, String message, String gatewayErrorCode, Throwable cause) {
        super(
            String.format("Payment failed: %s (Payment: %s, Code: %s)",
                message, paymentReference, gatewayErrorCode),
            "PAYMENT_FAILED",
            cause
        );
        this.paymentReference = paymentReference;
        this.gatewayErrorCode = gatewayErrorCode;
    }
    
    public String getPaymentReference() { return paymentReference; }
    public String getGatewayErrorCode() { return gatewayErrorCode; }
}