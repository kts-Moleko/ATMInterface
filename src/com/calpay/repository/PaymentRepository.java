package com.calpay.repository;

import com.calpay.core.model.Payment;
import com.calpay.core.enums.PaymentStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Payment data access operations.
 * 
 * <p><b>Implementation Strategy:</b>
 * <ul>
 *   <li>PaymentRepositoryJdbc - JDBC-based implementation</li>
 *   <li>PaymentRepositoryJpa - JPA/Hibernate implementation</li>
 *   <li>PaymentRepositoryFile - Flat-file implementation (CSV/JSON)</li>
 * </ul>
 * 
 * <p><b>Query Optimization:</b>
 * Implementations should create indexes on:
 * <ul>
 *   <li>paymentReference (unique)</li>
 *   <li>idempotencyKey (unique)</li>
 *   <li>schoolId + timestamp (for reporting)</li>
 *   <li>parentId + studentId (for family queries)</li>
 *   <li>paymentStatus (for filtering)</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public interface PaymentRepository {
    
    /**
     * Saves a payment (insert or update).
     * 
     * @param payment payment to save
     * @return saved payment with generated fields
     */
    Payment save(Payment payment);
    
    /**
     * Finds a payment by ID.
     * 
     * @param paymentId payment ID
     * @return optional payment
     */
    Optional<Payment> findById(String paymentId);
    
    /**
     * Finds a payment by unique reference.
     * 
     * @param paymentReference payment reference (e.g., PAY-2025-001234)
     * @return optional payment
     */
    Optional<Payment> findByPaymentReference(String paymentReference);
    
    /**
     * Finds a payment by idempotency key.
     * 
     * <p><b>Use case:</b> Duplicate payment prevention.
     * 
     * @param idempotencyKey unique idempotency key
     * @return optional payment
     */
    Optional<Payment> findByIdempotencyKey(String idempotencyKey);
    
    /**
     * Finds all payments for a school.
     * 
     * @param schoolId school ID
     * @return list of payments
     */
    List<Payment> findBySchoolId(String schoolId);
    
    /**
     * Finds payments by school and status.
     * 
     * @param schoolId school ID
     * @param status payment status
     * @return list of payments
     */
    List<Payment> findBySchoolIdAndStatus(String schoolId, String status);
    
    /**
     * Finds payments by school and payment method.
     * 
     * @param schoolId school ID
     * @param paymentMethod payment method
     * @return list of payments
     */
    List<Payment> findBySchoolIdAndPaymentMethod(String schoolId, String paymentMethod);
    
    /**
     * Finds payments by school within date range.
     * 
     * @param schoolId school ID
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @return list of payments
     */
    List<Payment> findBySchoolIdAndTimestampBetween(
        String schoolId, 
        LocalDateTime startDate, 
        LocalDateTime endDate
    );
    
    /**
     * Finds payments by school, year, and month.
     * 
     * @param schoolId school ID
     * @param year year
     * @param month month (1-12)
     * @return list of payments
     */
    List<Payment> findBySchoolIdAndYearMonth(String schoolId, int year, int month);
    
    /**
     * Finds all payments for a student.
     * 
     * @param studentId student ID
     * @return list of payments
     */
    List<Payment> findByStudentId(String studentId);
    
    /**
     * Finds payments by student and status.
     * 
     * @param studentId student ID
     * @param status payment status
     * @return list of payments
     */
    List<Payment> findByStudentIdAndStatus(String studentId, String status);
    
    /**
     * Finds all payments made by a parent.
     * 
     * @param parentId parent ID
     * @return list of payments
     */
    List<Payment> findByParentId(String parentId);
    
    /**
     * Finds payments by parent and status.
     * 
     * @param parentId parent ID
     * @param status payment status
     * @return list of payments
     */
    List<Payment> findByParentIdAndStatus(String parentId, String status);
    
    /**
     * Checks if a payment exists by idempotency key.
     * 
     * @param idempotencyKey idempotency key
     * @return true if exists
     */
    boolean existsByIdempotencyKey(String idempotencyKey);
    
    /**
     * Counts total payments for a school.
     * 
     * @param schoolId school ID
     * @return payment count
     */
    long countBySchoolId(String schoolId);
    
    /**
     * Deletes a payment by ID.
     * 
     * <p><b>Note:</b> Should be used sparingly. Consider soft delete
     * by updating status to CANCELLED instead.
     * 
     * @param paymentId payment ID
     */
    void deleteById(String paymentId);
    
    /**
     * Deletes all payments (use with caution, mainly for testing).
     */
    void deleteAll();
}