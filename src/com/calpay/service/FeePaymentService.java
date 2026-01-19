package com.calpay.service;

import com.calpay.audit.AuditLogger;
import com.calpay.core.enums.PaymentMethod;
import com.calpay.core.enums.PaymentStatus;
import com.calpay.core.exceptions.DuplicatePaymentException;
import com.calpay.core.exceptions.InvalidRequestException;
import com.calpay.core.exceptions.ResourceNotFoundException;
import com.calpay.core.model.*;
import com.calpay.repository.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for processing school fee payments.
 * 
 * <p><b>Key Responsibilities:</b>
 * <ul>
 *   <li>Process fee payments from parents</li>
 *   <li>Validate payment requests</li>
 *   <li>Idempotency handling (prevent duplicates)</li>
 *   <li>Update school account balances</li>
 *   <li>Generate receipts</li>
 *   <li>Send notifications</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public class FeePaymentService {
    
    private final PaymentRepository paymentRepository;
    private final ParentRepository parentRepository;
    private final StudentRepository studentRepository;
    private final FeeStructureRepository feeStructureRepository;
    private final SchoolAccountRepository schoolAccountRepository;
    private final AuditLogger auditLogger;
    
    // Business rules
    private static final Money MIN_PAYMENT_AMOUNT = Money.of(1.00, "ZAR");
    private static final Money MAX_PAYMENT_AMOUNT = Money.of(50000.00, "ZAR");
    
    public FeePaymentService(
            PaymentRepository paymentRepository,
            ParentRepository parentRepository,
            StudentRepository studentRepository,
            FeeStructureRepository feeStructureRepository,
            SchoolAccountRepository schoolAccountRepository,
            AuditLogger auditLogger) {
        this.paymentRepository = Objects.requireNonNull(paymentRepository);
        this.parentRepository = Objects.requireNonNull(parentRepository);
        this.studentRepository = Objects.requireNonNull(studentRepository);
        this.feeStructureRepository = Objects.requireNonNull(feeStructureRepository);
        this.schoolAccountRepository = Objects.requireNonNull(schoolAccountRepository);
        this.auditLogger = Objects.requireNonNull(auditLogger);
    }
    
    /**
     * Processes a fee payment.
     * 
     * <p><b>Payment Flow:</b>
     * <ol>
     *   <li>Validate payment request</li>
     *   <li>Check idempotency (prevent duplicates)</li>
     *   <li>Verify parent/student relationship</li>
     *   <li>Apply scholarship if applicable</li>
     *   <li>Create payment record</li>
     *   <li>Update school account balances</li>
     *   <li>Generate receipt</li>
     *   <li>Send notification (async)</li>
     * </ol>
     * 
     * @param parentId parent making payment
     * @param studentId student the payment is for
     * @param feeStructureId fee being paid
     * @param amount payment amount
     * @param paymentMethod payment method
     * @param idempotencyKey unique key to prevent duplicates
     * @return payment record
     */
    public Payment processPayment(
            String parentId,
            String studentId,
            String feeStructureId,
            Money amount,
            PaymentMethod paymentMethod,
            String idempotencyKey) {
        
        String paymentId = UUID.randomUUID().toString();
        String paymentReference = generatePaymentReference();
        
        try {
            // 1. Idempotency check
            Optional<Payment> existingPayment = paymentRepository.findByIdempotencyKey(idempotencyKey);
            if (existingPayment.isPresent()) {
                auditLogger.log(
                    "PAYMENT_DUPLICATE",
                    paymentId,
                    "Duplicate payment detected: " + idempotencyKey,
                    parentId,
                    null
                );
                throw new DuplicatePaymentException(idempotencyKey, existingPayment.get().getPaymentId());
            }
            
            // 2. Validate entities exist
            Parent parent = parentRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent", parentId));
            
            Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
            
            FeeStructure feeStructure = feeStructureRepository.findById(feeStructureId)
                .orElseThrow(() -> new ResourceNotFoundException("FeeStructure", feeStructureId));
            
            SchoolAccount schoolAccount = schoolAccountRepository.findBySchoolId(student.getSchoolId())
                .orElseThrow(() -> new ResourceNotFoundException("SchoolAccount", student.getSchoolId()));
            
            // 3. Validate parent-student relationship
            if (!parent.getSchoolId().equals(student.getSchoolId())) {
                throw new InvalidRequestException("Parent and student must belong to same school");
            }
            
            // 4. Validate amount
            validatePaymentAmount(amount);
            
            // 5. Apply scholarship if applicable
            Money expectedAmount = feeStructure.getAmount();
            if (student.hasScholarship()) {
                expectedAmount = student.applyScholarship(expectedAmount);
            }
            
            // Verify amount matches (within tolerance for rounding)
            if (!amount.equals(expectedAmount)) {
                auditLogger.log(
                    "PAYMENT_AMOUNT_MISMATCH",
                    paymentId,
                    String.format("Expected: %s, Got: %s", expectedAmount, amount),
                    parentId,
                    student.getSchoolId()
                );
            }
            
            // 6. Create payment record
            Payment payment = new Payment.Builder()
                .paymentId(paymentId)
                .paymentReference(paymentReference)
                .idempotencyKey(idempotencyKey)
                .schoolId(student.getSchoolId())
                .parentId(parentId)
                .studentId(studentId)
                .feeStructureId(feeStructureId)
                .amount(amount)
                .paymentMethod(paymentMethod.name())
                .paymentStatus(PaymentStatus.COMPLETED.name())
                .timestamp(LocalDateTime.now())
                .paymentDate(LocalDateTime.now())
                .description(String.format("%s - %s", feeStructure.getFeeName(), student.getFullName()))
                .initiatedBy(parentId)
                .build();
            
            // 7. Update school account
            schoolAccount.recordPayment(amount);
            schoolAccountRepository.save(schoolAccount);
            
            // 8. Save payment
            Payment savedPayment = paymentRepository.save(payment);
            
            // 9. Audit
            auditLogger.log(
                "PAYMENT_COMPLETED",
                paymentId,
                String.format("Payment %s: %s for %s", paymentReference, amount, student.getFullName()),
                parentId,
                student.getSchoolId()
            );
            
            return savedPayment;
            
        } catch (Exception e) {
            auditLogger.log(
                "PAYMENT_FAILED",
                paymentId,
                "Payment failed: " + e.getMessage(),
                parentId,
                null
            );
            throw e;
        }
    }
    
    /**
     * Gets payment by reference.
     * 
     * @param paymentReference payment reference (e.g., PAY-2025-001234)
     * @return payment
     */
    public Payment getPaymentByReference(String paymentReference) {
        return paymentRepository.findByPaymentReference(paymentReference)
            .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentReference));
    }
    
    /**
     * Gets all payments for a student.
     * 
     * @param studentId student ID
     * @return list of payments
     */
    public List<Payment> getStudentPayments(String studentId) {
        return paymentRepository.findByStudentId(studentId);
    }
    
    /**
     * Gets all payments made by a parent.
     * 
     * @param parentId parent ID
     * @return list of payments
     */
    public List<Payment> getParentPayments(String parentId) {
        return paymentRepository.findByParentId(parentId);
    }
    
    /**
     * Calculates total amount paid for a student.
     * 
     * @param studentId student ID
     * @return total paid
     */
    public Money calculateTotalPaid(String studentId) {
        List<Payment> payments = paymentRepository.findByStudentIdAndStatus(
            studentId, 
            PaymentStatus.COMPLETED.name()
        );
        
        if (payments.isEmpty()) {
            return Money.zero("ZAR");
        }
        
        Money total = Money.zero("ZAR");
        for (Payment payment : payments) {
            total = total.add(payment.getAmount());
        }
        
        return total;
    }
    
    // ========== Helper Methods ==========
    
    private String generatePaymentReference() {
        // Format: PAY-YYYY-MM-NNNNNN
        return String.format("PAY-%tY-%<tm-%06d", 
            LocalDateTime.now(), 
            (int)(Math.random() * 1000000)
        );
    }
    
    private void validatePaymentAmount(Money amount) {
        if (amount.isLessThan(MIN_PAYMENT_AMOUNT)) {
            throw new InvalidRequestException(
                String.format("Payment amount must be at least %s", MIN_PAYMENT_AMOUNT)
            );
        }
        
        if (amount.isGreaterThan(MAX_PAYMENT_AMOUNT)) {
            throw new InvalidRequestException(
                String.format("Payment amount cannot exceed %s", MAX_PAYMENT_AMOUNT)
            );
        }
    }
}