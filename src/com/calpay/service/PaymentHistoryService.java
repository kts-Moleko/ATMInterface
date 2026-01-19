package com.calpay.service;

import com.calpay.core.enums.PaymentStatus;
import com.calpay.core.enums.PaymentMethod;
import com.calpay.core.enums.FeeType;
import com.calpay.core.exceptions.ResourceNotFoundException;
import com.calpay.core.model.Payment;
import com.calpay.core.model.Money;
import com.calpay.repository.PaymentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Service for querying and analyzing payment history.
 * 
 * <p><b>Key Responsibilities:</b>
 * <ul>
 *   <li>Filter payments by various criteria</li>
 *   <li>Calculate payment statistics</li>
 *   <li>Generate payment reports</li>
 *   <li>Search payments</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public class PaymentHistoryService {
    
    private final PaymentRepository paymentRepository;
    
    public PaymentHistoryService(PaymentRepository paymentRepository) {
        this.paymentRepository = Objects.requireNonNull(paymentRepository);
    }
    
    /**
     * Gets all payments for a school.
     * 
     * @param schoolId school ID
     * @return list of payments
     */
    public List<Payment> getSchoolPayments(String schoolId) {
        return paymentRepository.findBySchoolId(schoolId);
    }
    
    /**
     * Gets payments for a specific student.
     * 
     * @param studentId student ID
     * @return list of payments
     */
    public List<Payment> getStudentPayments(String studentId) {
        return paymentRepository.findByStudentId(studentId);
    }
    
    /**
     * Gets payments made by a specific parent.
     * 
     * @param parentId parent ID
     * @return list of payments
     */
    public List<Payment> getParentPayments(String parentId) {
        return paymentRepository.findByParentId(parentId);
    }
    
    /**
     * Filters payments by status.
     * 
     * @param schoolId school ID
     * @param status payment status
     * @return filtered payments
     */
    public List<Payment> getPaymentsByStatus(String schoolId, PaymentStatus status) {
        Objects.requireNonNull(status, "Payment status cannot be null");
        return paymentRepository.findBySchoolIdAndStatus(schoolId, status.name());
    }
    
    /**
     * Gets completed payments for a school.
     * 
     * @param schoolId school ID
     * @return list of completed payments
     */
    public List<Payment> getCompletedPayments(String schoolId) {
        return getPaymentsByStatus(schoolId, PaymentStatus.COMPLETED);
    }
    
    /**
     * Gets pending payments for a school.
     * 
     * @param schoolId school ID
     * @return list of pending payments
     */
    public List<Payment> getPendingPayments(String schoolId) {
        return getPaymentsByStatus(schoolId, PaymentStatus.PENDING);
    }
    
    /**
     * Gets failed payments for a school.
     * 
     * @param schoolId school ID
     * @return list of failed payments
     */
    public List<Payment> getFailedPayments(String schoolId) {
        return getPaymentsByStatus(schoolId, PaymentStatus.FAILED);
    }
    
    /**
     * Filters payments by payment method.
     * 
     * @param schoolId school ID
     * @param method payment method
     * @return filtered payments
     */
    public List<Payment> getPaymentsByMethod(String schoolId, PaymentMethod method) {
        Objects.requireNonNull(method, "Payment method cannot be null");
        return paymentRepository.findBySchoolIdAndPaymentMethod(schoolId, method.name());
    }
    
    /**
     * Gets cash payments requiring manual verification.
     * 
     * @param schoolId school ID
     * @return list of cash payments
     */
    public List<Payment> getCashPayments(String schoolId) {
        return getPaymentsByMethod(schoolId, PaymentMethod.CASH);
    }
    
    /**
     * Filters payments by date range.
     * 
     * @param schoolId school ID
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @return payments within date range
     */
    public List<Payment> getPaymentsByDateRange(
            String schoolId,
            LocalDateTime startDate,
            LocalDateTime endDate) {
        
        Objects.requireNonNull(startDate, "Start date cannot be null");
        Objects.requireNonNull(endDate, "End date cannot be null");
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date must be before end date");
        }
        
        return paymentRepository.findBySchoolIdAndTimestampBetween(schoolId, startDate, endDate);
    }
    
    /**
     * Gets payments for current month.
     * 
     * @param schoolId school ID
     * @return list of payments this month
     */
    public List<Payment> getCurrentMonthPayments(String schoolId) {
        LocalDate now = LocalDate.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = now.plusMonths(1).withDayOfMonth(1).atStartOfDay().minusNanos(1);
        
        return getPaymentsByDateRange(schoolId, startOfMonth, endOfMonth);
    }
    
    /**
     * Gets payments for a specific month.
     * 
     * @param schoolId school ID
     * @param year year
     * @param month month (1-12)
     * @return list of payments for that month
     */
    public List<Payment> getPaymentsByMonth(String schoolId, int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        
        LocalDate date = LocalDate.of(year, month, 1);
        LocalDateTime startOfMonth = date.atStartOfDay();
        LocalDateTime endOfMonth = date.plusMonths(1).withDayOfMonth(1).atStartOfDay().minusNanos(1);
        
        return getPaymentsByDateRange(schoolId, startOfMonth, endOfMonth);
    }
    
    /**
     * Filters payments by minimum amount.
     * 
     * @param schoolId school ID
     * @param minAmount minimum amount (inclusive)
     * @return payments >= minAmount
     */
    public List<Payment> getPaymentsByMinAmount(String schoolId, Money minAmount) {
        Objects.requireNonNull(minAmount, "Minimum amount cannot be null");
        
        return paymentRepository.findBySchoolId(schoolId).stream()
            .filter(payment -> payment.getAmount().isGreaterThanOrEqualTo(minAmount))
            .collect(Collectors.toList());
    }
    
    /**
     * Filters payments by amount range.
     * 
     * @param schoolId school ID
     * @param minAmount minimum amount (inclusive)
     * @param maxAmount maximum amount (inclusive)
     * @return payments within range
     */
    public List<Payment> getPaymentsByAmountRange(
            String schoolId,
            Money minAmount,
            Money maxAmount) {
        
        Objects.requireNonNull(minAmount, "Minimum amount cannot be null");
        Objects.requireNonNull(maxAmount, "Maximum amount cannot be null");
        
        if (minAmount.isGreaterThan(maxAmount)) {
            throw new IllegalArgumentException("Min amount cannot be greater than max amount");
        }
        
        return paymentRepository.findBySchoolId(schoolId).stream()
            .filter(payment -> payment.getAmount().isGreaterThanOrEqualTo(minAmount))
            .filter(payment -> payment.getAmount().isLessThanOrEqualTo(maxAmount))
            .collect(Collectors.toList());
    }
    
    /**
     * Searches payments by reference or description.
     * 
     * @param schoolId school ID
     * @param searchTerm search term
     * @return matching payments
     */
    public List<Payment> searchPayments(String schoolId, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return List.of();
        }
        
        String term = searchTerm.toLowerCase().trim();
        
        return paymentRepository.findBySchoolId(schoolId).stream()
            .filter(payment -> 
                (payment.getPaymentReference() != null && 
                 payment.getPaymentReference().toLowerCase().contains(term)) ||
                (payment.getDescription() != null && 
                 payment.getDescription().toLowerCase().contains(term))
            )
            .collect(Collectors.toList());
    }
    
    /**
     * Gets recent payments (most recent first).
     * 
     * @param schoolId school ID
     * @param limit maximum number of payments
     * @return list of recent payments
     */
    public List<Payment> getRecentPayments(String schoolId, int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        
        return paymentRepository.findBySchoolId(schoolId).stream()
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    // ========== Statistics & Analytics ==========
    
    /**
     * Calculates total amount collected for a school.
     * 
     * @param schoolId school ID
     * @return total collected amount
     */
    public Money calculateTotalCollected(String schoolId) {
        List<Payment> completedPayments = getCompletedPayments(schoolId);
        
        if (completedPayments.isEmpty()) {
            return Money.zero("ZAR");
        }
        
        Money total = Money.zero("ZAR");
        for (Payment payment : completedPayments) {
            total = total.add(payment.getAmount());
        }
        
        return total;
    }
    
    /**
     * Calculates total collected for current month.
     * 
     * @param schoolId school ID
     * @return total collected this month
     */
    public Money calculateMonthlyCollection(String schoolId) {
        List<Payment> monthlyPayments = getCurrentMonthPayments(schoolId);
        
        Money total = Money.zero("ZAR");
        for (Payment payment : monthlyPayments) {
            if (PaymentStatus.COMPLETED.name().equals(payment.getPaymentStatus())) {
                total = total.add(payment.getAmount());
            }
        }
        
        return total;
    }
    
    /**
     * Calculates total collected for a specific month.
     * 
     * @param schoolId school ID
     * @param year year
     * @param month month (1-12)
     * @return total collected
     */
    public Money calculateMonthlyCollection(String schoolId, int year, int month) {
        List<Payment> monthlyPayments = getPaymentsByMonth(schoolId, year, month);
        
        Money total = Money.zero("ZAR");
        for (Payment payment : monthlyPayments) {
            if (PaymentStatus.COMPLETED.name().equals(payment.getPaymentStatus())) {
                total = total.add(payment.getAmount());
            }
        }
        
        return total;
    }
    
    /**
     * Calculates total paid by a parent.
     * 
     * @param parentId parent ID
     * @return total amount paid
     */
    public Money calculateParentTotalPaid(String parentId) {
        List<Payment> payments = paymentRepository.findByParentIdAndStatus(
            parentId, 
            PaymentStatus.COMPLETED.name()
        );
        
        Money total = Money.zero("ZAR");
        for (Payment payment : payments) {
            total = total.add(payment.getAmount());
        }
        
        return total;
    }
    
    /**
     * Calculates total paid for a student.
     * 
     * @param studentId student ID
     * @return total amount paid
     */
    public Money calculateStudentTotalPaid(String studentId) {
        List<Payment> payments = paymentRepository.findByStudentIdAndStatus(
            studentId, 
            PaymentStatus.COMPLETED.name()
        );
        
        Money total = Money.zero("ZAR");
        for (Payment payment : payments) {
            total = total.add(payment.getAmount());
        }
        
        return total;
    }
    
    /**
     * Gets payment statistics for a school.
     * 
     * @param schoolId school ID
     * @return payment statistics
     */
    public PaymentStatistics getStatistics(String schoolId) {
        List<Payment> allPayments = paymentRepository.findBySchoolId(schoolId);
        
        long totalCount = allPayments.size();
        long completedCount = allPayments.stream()
            .filter(p -> PaymentStatus.COMPLETED.name().equals(p.getPaymentStatus()))
            .count();
        long pendingCount = allPayments.stream()
            .filter(p -> PaymentStatus.PENDING.name().equals(p.getPaymentStatus()))
            .count();
        long failedCount = allPayments.stream()
            .filter(p -> PaymentStatus.FAILED.name().equals(p.getPaymentStatus()))
            .count();
        
        Money totalCollected = calculateTotalCollected(schoolId);
        Money monthlyCollection = calculateMonthlyCollection(schoolId);
        
        return new PaymentStatistics(
            totalCount,
            completedCount,
            pendingCount,
            failedCount,
            totalCollected,
            monthlyCollection
        );
    }
    
    /**
     * Payment statistics value object.
     */
    public static class PaymentStatistics {
        private final long totalPayments;
        private final long completedPayments;
        private final long pendingPayments;
        private final long failedPayments;
        private final Money totalCollected;
        private final Money monthlyCollection;
        
        public PaymentStatistics(
                long totalPayments,
                long completedPayments,
                long pendingPayments,
                long failedPayments,
                Money totalCollected,
                Money monthlyCollection) {
            this.totalPayments = totalPayments;
            this.completedPayments = completedPayments;
            this.pendingPayments = pendingPayments;
            this.failedPayments = failedPayments;
            this.totalCollected = totalCollected;
            this.monthlyCollection = monthlyCollection;
        }
        
        public long getTotalPayments() { return totalPayments; }
        public long getCompletedPayments() { return completedPayments; }
        public long getPendingPayments() { return pendingPayments; }
        public long getFailedPayments() { return failedPayments; }
        public Money getTotalCollected() { return totalCollected; }
        public Money getMonthlyCollection() { return monthlyCollection; }
        
        public double getSuccessRate() {
            return totalPayments > 0 
                ? (completedPayments * 100.0) / totalPayments 
                : 0.0;
        }
        
        @Override
        public String toString() {
            return String.format(
                "PaymentStatistics{total=%d, completed=%d, pending=%d, failed=%d, " +
                "collected=%s, monthly=%s, successRate=%.1f%%}",
                totalPayments, completedPayments, pendingPayments, failedPayments,
                totalCollected, monthlyCollection, getSuccessRate()
            );
        }
    }
}