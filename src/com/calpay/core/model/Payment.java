package com.calpay.core.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an immutable payment transaction.
 * 
 * <p><b>Renamed from:</b> TransactionRecord (v1.0)
 * 
 * <p><b>Key Changes:</b>
 * <ul>
 *   <li>Added paymentMethod (INSTANT_EFT, CASH, CARD, BANK_TRANSFER)</li>
 *   <li>Added receiptUrl for receipt storage</li>
 *   <li>Added idempotencyKey for duplicate prevention</li>
 *   <li>Added gatewayResponse for external payment data</li>
 *   <li>Added retryCount for failure handling</li>
 * </ul>
 * 
 * <p><b>Immutability:</b> All fields are final. Once created, payments
 * cannot be modified (audit integrity). Use new Payment for corrections.
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public class Payment implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Identity
    private final String paymentId;
    private final String paymentReference;  // "PAY-2025-01-001234"
    private final String idempotencyKey;  // For duplicate prevention
    
    // Relationships
    private final String schoolId;
    private final String parentId;
    private final String studentId;
    private final String feeStructureId;
    
    // Payment details
    private final Money amount;
    private final String paymentMethod;  // INSTANT_EFT, CASH, CARD, BANK_TRANSFER
    private final String paymentStatus;  // PENDING, COMPLETED, FAILED, CANCELLED
    
    // Dates
    private final LocalDateTime timestamp;
    private final LocalDateTime paymentDate;  // When payment was actually processed
    private final String feeMonth;  // "2025-01" for monthly fees
    
    // External references
    private final String externalReference;  // Gateway transaction ID
    private final String gatewayResponse;  // Full gateway response (JSON)
    
    // Failure handling
    private final String failureReason;
    private final int retryCount;
    
    // Receipt
    private final String receiptId;
    private final String receiptUrl;
    
    // Metadata
    private final String description;
    private final String initiatedBy;  // User ID who initiated
    private final String notes;
    
    private Payment(Builder builder) {
        this.paymentId = Objects.requireNonNull(builder.paymentId, "Payment ID cannot be null");
        this.paymentReference = Objects.requireNonNull(builder.paymentReference, "Payment reference cannot be null");
        this.idempotencyKey = Objects.requireNonNull(builder.idempotencyKey, "Idempotency key cannot be null");
        this.schoolId = Objects.requireNonNull(builder.schoolId, "School ID cannot be null");
        this.parentId = Objects.requireNonNull(builder.parentId, "Parent ID cannot be null");
        this.studentId = Objects.requireNonNull(builder.studentId, "Student ID cannot be null");
        this.amount = Objects.requireNonNull(builder.amount, "Amount cannot be null");
        this.paymentMethod = Objects.requireNonNull(builder.paymentMethod, "Payment method cannot be null");
        this.paymentStatus = Objects.requireNonNull(builder.paymentStatus, "Payment status cannot be null");
        this.timestamp = Objects.requireNonNull(builder.timestamp, "Timestamp cannot be null");
        
        this.feeStructureId = builder.feeStructureId;
        this.paymentDate = builder.paymentDate;
        this.feeMonth = builder.feeMonth;
        this.externalReference = builder.externalReference;
        this.gatewayResponse = builder.gatewayResponse;
        this.failureReason = builder.failureReason;
        this.retryCount = builder.retryCount;
        this.receiptId = builder.receiptId;
        this.receiptUrl = builder.receiptUrl;
        this.description = builder.description;
        this.initiatedBy = builder.initiatedBy;
        this.notes = builder.notes;
        
        validatePaymentMethod();
        validateAmount();
    }
    
    private void validatePaymentMethod() {
        if (!paymentMethod.matches("INSTANT_EFT|CASH|CARD|BANK_TRANSFER")) {
            throw new IllegalArgumentException("Invalid payment method: " + paymentMethod);
        }
    }
    
    private void validateAmount() {
        if (amount.isNegativeOrZero()) {
            throw new IllegalArgumentException("Payment amount must be positive: " + amount);
        }
    }
    
    // Getters
    public String getPaymentId() { return paymentId; }
    public String getPaymentReference() { return paymentReference; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getSchoolId() { return schoolId; }
    public String getParentId() { return parentId; }
    public String getStudentId() { return studentId; }
    public String getFeeStructureId() { return feeStructureId; }
    public Money getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public LocalDateTime getPaymentDate() { return paymentDate; }
    public String getFeeMonth() { return feeMonth; }
    public String getExternalReference() { return externalReference; }
    public String getGatewayResponse() { return gatewayResponse; }
    public String getFailureReason() { return failureReason; }
    public int getRetryCount() { return retryCount; }
    public String getReceiptId() { return receiptId; }
    public String getReceiptUrl() { return receiptUrl; }
    public String getDescription() { return description; }
    public String getInitiatedBy() { return initiatedBy; }
    public String getNotes() { return notes; }
    
    // Business methods
    public boolean isCompleted() {
        return "COMPLETED".equals(paymentStatus);
    }
    
    public boolean isPending() {
        return "PENDING".equals(paymentStatus);
    }
    
    public boolean isFailed() {
        return "FAILED".equals(paymentStatus);
    }
    
    public boolean isCancelled() {
        return "CANCELLED".equals(paymentStatus);
    }
    
    public boolean isCashPayment() {
        return "CASH".equals(paymentMethod);
    }
    
    public boolean isElectronicPayment() {
        return "INSTANT_EFT".equals(paymentMethod) || 
               "CARD".equals(paymentMethod) || 
               "BANK_TRANSFER".equals(paymentMethod);
    }
    
    public boolean hasReceipt() {
        return receiptUrl != null && !receiptUrl.isEmpty();
    }
    
    public boolean needsRetry() {
        return isFailed() && retryCount < 3;
    }
    
    // Builder
    public static class Builder {
        private String paymentId;
        private String paymentReference;
        private String idempotencyKey;
        private String schoolId;
        private String parentId;
        private String studentId;
        private String feeStructureId;
        private Money amount;
        private String paymentMethod;
        private String paymentStatus = "PENDING";
        private LocalDateTime timestamp = LocalDateTime.now();
        private LocalDateTime paymentDate;
        private String feeMonth;
        private String externalReference;
        private String gatewayResponse;
        private String failureReason;
        private int retryCount = 0;
        private String receiptId;
        private String receiptUrl;
        private String description;
        private String initiatedBy;
        private String notes;
        
        public Builder paymentId(String paymentId) {
            this.paymentId = paymentId;
            return this;
        }
        
        public Builder paymentReference(String paymentReference) {
            this.paymentReference = paymentReference;
            return this;
        }
        
        public Builder idempotencyKey(String idempotencyKey) {
            this.idempotencyKey = idempotencyKey;
            return this;
        }
        
        public Builder schoolId(String schoolId) {
            this.schoolId = schoolId;
            return this;
        }
        
        public Builder parentId(String parentId) {
            this.parentId = parentId;
            return this;
        }
        
        public Builder studentId(String studentId) {
            this.studentId = studentId;
            return this;
        }
        
        public Builder feeStructureId(String feeStructureId) {
            this.feeStructureId = feeStructureId;
            return this;
        }
        
        public Builder amount(Money amount) {
            this.amount = amount;
            return this;
        }
        
        public Builder paymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }
        
        public Builder paymentStatus(String paymentStatus) {
            this.paymentStatus = paymentStatus;
            return this;
        }
        
        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder paymentDate(LocalDateTime paymentDate) {
            this.paymentDate = paymentDate;
            return this;
        }
        
        public Builder feeMonth(String feeMonth) {
            this.feeMonth = feeMonth;
            return this;
        }
        
        public Builder externalReference(String externalReference) {
            this.externalReference = externalReference;
            return this;
        }
        
        public Builder gatewayResponse(String gatewayResponse) {
            this.gatewayResponse = gatewayResponse;
            return this;
        }
        
        public Builder failureReason(String failureReason) {
            this.failureReason = failureReason;
            return this;
        }
        
        public Builder retryCount(int retryCount) {
            this.retryCount = retryCount;
            return this;
        }
        
        public Builder receiptId(String receiptId) {
            this.receiptId = receiptId;
            return this;
        }
        
        public Builder receiptUrl(String receiptUrl) {
            this.receiptUrl = receiptUrl;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder initiatedBy(String initiatedBy) {
            this.initiatedBy = initiatedBy;
            return this;
        }
        
        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }
        
        public Payment build() {
            return new Payment(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Payment payment = (Payment) o;
        return paymentId.equals(payment.paymentId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(paymentId);
    }
    
    @Override
    public String toString() {
        return String.format("Payment{id='%s', ref='%s', amount=%s, method='%s', status='%s', parent='%s', student='%s'}",
            paymentId, paymentReference, amount, paymentMethod, paymentStatus, parentId, studentId);
    }
}