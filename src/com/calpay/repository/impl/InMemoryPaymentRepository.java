package com.calpay.repository.impl;

import com.calpay.core.model.Payment;
import com.calpay.repository.PaymentRepository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of PaymentRepository.
 * 
 * <p><b>Use case:</b> Testing, development, demos.
 * 
 * <p><b>Thread Safety:</b> Uses ConcurrentHashMap for thread-safe operations.
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public class InMemoryPaymentRepository implements PaymentRepository {
    
    private final Map<String, Payment> payments = new ConcurrentHashMap<>();
    private final Map<String, String> idempotencyKeyIndex = new ConcurrentHashMap<>();
    private final Map<String, String> referenceIndex = new ConcurrentHashMap<>();
    
    @Override
    public Payment save(Payment payment) {
        payments.put(payment.getPaymentId(), payment);
        idempotencyKeyIndex.put(payment.getIdempotencyKey(), payment.getPaymentId());
        referenceIndex.put(payment.getPaymentReference(), payment.getPaymentId());
        return payment;
    }
    
    @Override
    public Optional<Payment> findById(String paymentId) {
        return Optional.ofNullable(payments.get(paymentId));
    }
    
    @Override
    public Optional<Payment> findByPaymentReference(String paymentReference) {
        String paymentId = referenceIndex.get(paymentReference);
        return paymentId != null ? Optional.ofNullable(payments.get(paymentId)) : Optional.empty();
    }
    
    @Override
    public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
        String paymentId = idempotencyKeyIndex.get(idempotencyKey);
        return paymentId != null ? Optional.ofNullable(payments.get(paymentId)) : Optional.empty();
    }
    
    @Override
    public List<Payment> findBySchoolId(String schoolId) {
        return payments.values().stream()
            .filter(p -> schoolId.equals(p.getSchoolId()))
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Payment> findBySchoolIdAndStatus(String schoolId, String status) {
        return payments.values().stream()
            .filter(p -> schoolId.equals(p.getSchoolId()) && status.equals(p.getPaymentStatus()))
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Payment> findBySchoolIdAndPaymentMethod(String schoolId, String paymentMethod) {
        return payments.values().stream()
            .filter(p -> schoolId.equals(p.getSchoolId()) && paymentMethod.equals(p.getPaymentMethod()))
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Payment> findBySchoolIdAndTimestampBetween(
            String schoolId, 
            LocalDateTime startDate, 
            LocalDateTime endDate) {
        return payments.values().stream()
            .filter(p -> schoolId.equals(p.getSchoolId()))
            .filter(p -> !p.getTimestamp().isBefore(startDate) && !p.getTimestamp().isAfter(endDate))
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Payment> findBySchoolIdAndYearMonth(String schoolId, int year, int month) {
        String feeMonth = String.format("%04d-%02d", year, month);
        return payments.values().stream()
            .filter(p -> schoolId.equals(p.getSchoolId()) && feeMonth.equals(p.getFeeMonth()))
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Payment> findByStudentId(String studentId) {
        return payments.values().stream()
            .filter(p -> studentId.equals(p.getStudentId()))
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Payment> findByStudentIdAndStatus(String studentId, String status) {
        return payments.values().stream()
            .filter(p -> studentId.equals(p.getStudentId()) && status.equals(p.getPaymentStatus()))
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Payment> findByParentId(String parentId) {
        return payments.values().stream()
            .filter(p -> parentId.equals(p.getParentId()))
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Payment> findByParentIdAndStatus(String parentId, String status) {
        return payments.values().stream()
            .filter(p -> parentId.equals(p.getParentId()) && status.equals(p.getPaymentStatus()))
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean existsByIdempotencyKey(String idempotencyKey) {
        return idempotencyKeyIndex.containsKey(idempotencyKey);
    }
    
    @Override
    public long countBySchoolId(String schoolId) {
        return payments.values().stream()
            .filter(p -> schoolId.equals(p.getSchoolId()))
            .count();
    }
    
    @Override
    public void deleteById(String paymentId) {
        Payment payment = payments.remove(paymentId);
        if (payment != null) {
            idempotencyKeyIndex.remove(payment.getIdempotencyKey());
            referenceIndex.remove(payment.getPaymentReference());
        }
    }
    
    @Override
    public void deleteAll() {
        payments.clear();
        idempotencyKeyIndex.clear();
        referenceIndex.clear();
    }
}