package com.calpay.core.model;

import com.calpay.core.enums.AccountStatus;
import com.calpay.core.exceptions.InsufficientFundsException;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a school's financial account in CalPay.
 * 
 * <p><b>Changes from BankAccount v1.0:</b>
 * <ul>
 *   <li>Removed banking concepts (interest, account types)</li>
 *   <li>Added school-specific fields (subscriptionStatus, feeBalance)</li>
 *   <li>Added subscription tracking (expiresAt, totalStudents)</li>
 *   <li>Simplified to focus on school fee collection</li>
 * </ul>
 * 
 * <p><b>Core Responsibilities:</b>
 * <ul>
 *   <li>Track school's collected revenue (balance)</li>
 *   <li>Track outstanding fees owed by parents (feeBalance)</li>
 *   <li>Manage subscription lifecycle (TRIAL → ACTIVE → SUSPENDED)</li>
 *   <li>Store basic school metadata</li>
 * </ul>
 * 
 * <p><b>Not Responsible For:</b>
 * <ul>
 *   <li>Individual parent/student balances (handled by Payment records)</li>
 *   <li>Fee structure definitions (handled by FeeStructure)</li>
 *   <li>Payment processing (handled by PaymentService)</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.1
 * @since 2.0
 */
public class SchoolAccount implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Subscription lifecycle status for a school.
     * 
     * <p><b>Flow:</b> FREE → TRIAL → ACTIVE → (EXPIRED or CANCELLED)
     */
    public enum SubscriptionStatus {
        FREE("Free (Limited)", 0),           // Free tier: 5 students max
        TRIAL("Trial", 30),                  // 30-day trial
        ACTIVE("Active", 365),               // Paid subscription
        EXPIRED("Expired", 0),               // Trial/subscription ended
        CANCELLED("Cancelled", 0);           // Manually cancelled
        
        private final String displayName;
        private final int defaultDurationDays;
        
        SubscriptionStatus(String displayName, int defaultDurationDays) {
            this.displayName = displayName;
            this.defaultDurationDays = defaultDurationDays;
        }
        
        public String getDisplayName() { return displayName; }
        public int getDefaultDurationDays() { return defaultDurationDays; }
    }
    
    // Immutable identity fields
    private final String accountId;
    private final String schoolId;
    private final String accountNumber;
    private final LocalDateTime createdAt;
    
    // Financial state
    private Money balance;          // Total revenue collected
    private Money feeBalance;       // Outstanding fees owed to school
    private Money monthlyRevenue;   // Revenue this month
    
    // Account status
    private AccountStatus status;
    
    // Subscription tracking
    private SubscriptionStatus subscriptionStatus;
    private LocalDateTime subscriptionStartsAt;
    private LocalDateTime subscriptionExpiresAt;
    private String subscriptionTier;  // TIER_1, TIER_2, TIER_3
    
    // Business metrics
    private int totalStudents;
    private LocalDateTime lastActivityAt;
    
    /**
     * Private constructor to enforce builder pattern.
     */
    private SchoolAccount(Builder builder) {
        // Required immutable fields
        this.accountId = Objects.requireNonNull(builder.accountId, "Account ID cannot be null");
        this.schoolId = Objects.requireNonNull(builder.schoolId, "School ID cannot be null");
        this.accountNumber = Objects.requireNonNull(builder.accountNumber, "Account number cannot be null");
        
        // Optional fields with defaults
        this.balance = builder.balance != null ? builder.balance : Money.zero("ZAR");
        this.feeBalance = builder.feeBalance != null ? builder.feeBalance : Money.zero("ZAR");
        this.monthlyRevenue = builder.monthlyRevenue != null ? builder.monthlyRevenue : Money.zero("ZAR");
        this.status = builder.status != null ? builder.status : AccountStatus.PENDING;
        this.subscriptionStatus = builder.subscriptionStatus != null ? builder.subscriptionStatus : SubscriptionStatus.FREE;
        this.subscriptionTier = builder.subscriptionTier != null ? builder.subscriptionTier : "TIER_1";
        this.subscriptionStartsAt = builder.subscriptionStartsAt;
        this.subscriptionExpiresAt = builder.subscriptionExpiresAt;
        this.totalStudents = builder.totalStudents;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.lastActivityAt = builder.lastActivityAt;
        
        // Validation
        validateAccountNumber(this.accountNumber);
    }
    
    private void validateAccountNumber(String accountNumber) {
        if (!accountNumber.matches("\\d{10}")) {
            throw new IllegalArgumentException(
                "Invalid account number format. Must be 10 digits: " + accountNumber
            );
        }
    }
    
    // ========== Getters ==========
    
    public String getAccountId() { return accountId; }
    public String getSchoolId() { return schoolId; }
    public String getAccountNumber() { return accountNumber; }
    public Money getBalance() { return balance; }
    public Money getFeeBalance() { return feeBalance; }
    public Money getMonthlyRevenue() { return monthlyRevenue; }
    public AccountStatus getStatus() { return status; }
    public SubscriptionStatus getSubscriptionStatus() { return subscriptionStatus; }
    public String getSubscriptionTier() { return subscriptionTier; }
    public LocalDateTime getSubscriptionStartsAt() { return subscriptionStartsAt; }
    public LocalDateTime getSubscriptionExpiresAt() { return subscriptionExpiresAt; }
    public int getTotalStudents() { return totalStudents; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getLastActivityAt() { return lastActivityAt; }
    
    // ========== Setters (for mutable state only) ==========
    
    public void setStatus(AccountStatus status) {
        this.status = Objects.requireNonNull(status, "Account status cannot be null");
    }
    
    public void setSubscriptionStatus(SubscriptionStatus subscriptionStatus) {
        this.subscriptionStatus = Objects.requireNonNull(subscriptionStatus, "Subscription status cannot be null");
    }
    
    public void setSubscriptionTier(String subscriptionTier) {
        this.subscriptionTier = subscriptionTier;
    }
    
    public void setSubscriptionExpiresAt(LocalDateTime subscriptionExpiresAt) {
        this.subscriptionExpiresAt = subscriptionExpiresAt;
    }
    
    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }
    
    // ========== Business Logic Methods ==========
    
    /**
     * Records a payment received from a parent.
     * 
     * <p><b>Effects:</b>
     * <ul>
     *   <li>Increases balance (collected revenue)</li>
     *   <li>Decreases feeBalance (outstanding fees)</li>
     *   <li>Increases monthlyRevenue</li>
     *   <li>Updates lastActivityAt</li>
     * </ul>
     * 
     * @param amount payment amount
     */
    public void recordPayment(Money amount) {
        requirePositiveAmount(amount);
        
        this.balance = this.balance.add(amount);
        this.feeBalance = this.feeBalance.subtract(amount);
        this.monthlyRevenue = this.monthlyRevenue.add(amount);
        this.lastActivityAt = LocalDateTime.now();
        
        // Ensure feeBalance never goes negative
        if (this.feeBalance.isNegative()) {
            this.feeBalance = Money.zero(this.balance.getCurrency());
        }
    }
    
    /**
     * Adds outstanding fees to the school's receivables.
     * 
     * <p><b>Use case:</b> When a new fee is created for students.
     * 
     * @param amount fee amount
     */
    public void addOutstandingFees(Money amount) {
        requirePositiveAmount(amount);
        this.feeBalance = this.feeBalance.add(amount);
        this.lastActivityAt = LocalDateTime.now();
    }
    
    /**
     * Reduces outstanding fees (e.g., scholarship, refund).
     * 
     * @param amount amount to reduce
     */
    public void reduceOutstandingFees(Money amount) {
        requirePositiveAmount(amount);
        this.feeBalance = this.feeBalance.subtract(amount);
        this.lastActivityAt = LocalDateTime.now();
        
        // Ensure feeBalance never goes negative
        if (this.feeBalance.isNegative()) {
            this.feeBalance = Money.zero(this.balance.getCurrency());
        }
    }
    
    /**
     * Withdraws funds from the school account (e.g., payout to school).
     * 
     * @param amount withdrawal amount
     * @throws InsufficientFundsException if balance insufficient
     */
    public void withdraw(Money amount) {
        requireActive();
        requirePositiveAmount(amount);
        
        if (!hasSufficientFunds(amount)) {
            throw new InsufficientFundsException(
                this.accountId,
                this.balance,
                amount
            );
        }
        
        this.balance = this.balance.subtract(amount);
        this.lastActivityAt = LocalDateTime.now();
    }
    
    /**
     * Deposits funds into the school account.
     * 
     * @param amount deposit amount
     */
    public void deposit(Money amount) {
        requireActive();
        requirePositiveAmount(amount);
        
        this.balance = this.balance.add(amount);
        this.lastActivityAt = LocalDateTime.now();
    }
    
    /**
     * Checks if account has sufficient funds.
     * 
     * @param amount amount to check
     * @return true if balance >= amount
     */
    public boolean hasSufficientFunds(Money amount) {
        return this.balance.isGreaterThanOrEqualTo(amount);
    }
    
    /**
     * Resets monthly revenue (called at start of new month).
     */
    public void resetMonthlyRevenue() {
        this.monthlyRevenue = Money.zero(this.balance.getCurrency());
    }
    
    /**
     * Updates student count.
     * 
     * @param count new student count
     */
    public void updateStudentCount(int count) {
        if (count < 0) {
            throw new IllegalArgumentException("Student count cannot be negative");
        }
        this.totalStudents = count;
    }
    
    /**
     * Activates the account.
     */
    public void activate() {
        if (status == AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is already active");
        }
        this.status = AccountStatus.ACTIVE;
    }
    
    /**
     * Suspends the account.
     * 
     * @param reason suspension reason (for audit)
     */
    public void suspend(String reason) {
        this.status = AccountStatus.SUSPENDED;
        // TODO: Log suspension event for audit trail
    }
    
    /**
     * Closes the account.
     * 
     * <p><b>Business rule:</b> Balance must be zero before closing.
     */
    public void close() {
        if (!balance.isZero()) {
            throw new IllegalStateException(
                "Cannot close account with non-zero balance: " + balance
            );
        }
        this.status = AccountStatus.CLOSED;
    }
    
    /**
     * Checks if account can perform transactions.
     * 
     * @return true if ACTIVE
     */
    public boolean canTransact() {
        return status == AccountStatus.ACTIVE;
    }
    
    /**
     * Checks if subscription is active and not expired.
     * 
     * @return true if active subscription
     */
    public boolean isSubscriptionActive() {
        return subscriptionStatus == SubscriptionStatus.ACTIVE && 
               (subscriptionExpiresAt == null || subscriptionExpiresAt.isAfter(LocalDateTime.now()));
    }
    
    /**
     * Checks if trial period has expired.
     * 
     * @return true if trial expired
     */
    public boolean isTrialExpired() {
        return subscriptionStatus == SubscriptionStatus.TRIAL && 
               subscriptionExpiresAt != null && 
               subscriptionExpiresAt.isBefore(LocalDateTime.now());
    }
    
    /**
     * Returns account summary string.
     * 
     * @return formatted summary
     */
    public String getSummary() {
        return String.format(
            "SchoolAccount[%s] Number=%s School=%s Balance=%s Outstanding=%s Status=%s Subscription=%s Students=%d",
            accountId, accountNumber, schoolId, balance, feeBalance, 
            status.getDisplayName(), subscriptionStatus.getDisplayName(), totalStudents
        );
    }
    
    // ========== Validation Helpers ==========
    
    private void requireActive() {
        if (!canTransact()) {
            throw new IllegalStateException(
                "Account is not active. Current status: " + status.getDisplayName()
            );
        }
    }
    
    private void requirePositiveAmount(Money amount) {
        if (amount.isNegativeOrZero()) {
            throw new IllegalArgumentException(
                "Amount must be positive: " + amount
            );
        }
    }
    
    // ========== Object Methods ==========
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchoolAccount that = (SchoolAccount) o;
        return accountId.equals(that.accountId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(accountId);
    }
    
    @Override
    public String toString() {
        return String.format(
            "SchoolAccount{id='%s', school='%s', balance=%s, outstanding=%s, subscription=%s}",
            accountId, schoolId, balance, feeBalance, subscriptionStatus
        );
    }
    
    // ========== Builder Pattern ==========
    
    public static class Builder {
        // Required fields
        private String accountId;
        private String schoolId;
        private String accountNumber;
        
        // Optional fields with defaults
        private Money balance = Money.zero("ZAR");
        private Money feeBalance = Money.zero("ZAR");
        private Money monthlyRevenue = Money.zero("ZAR");
        private AccountStatus status = AccountStatus.PENDING;
        private SubscriptionStatus subscriptionStatus = SubscriptionStatus.FREE;
        private String subscriptionTier = "TIER_1";
        private LocalDateTime subscriptionStartsAt;
        private LocalDateTime subscriptionExpiresAt;
        private int totalStudents = 0;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime lastActivityAt;
        
        public Builder accountId(String accountId) {
            this.accountId = accountId;
            return this;
        }
        
        public Builder schoolId(String schoolId) {
            this.schoolId = schoolId;
            return this;
        }
        
        public Builder accountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
            return this;
        }
        
        public Builder balance(Money balance) {
            this.balance = balance;
            return this;
        }
        
        public Builder feeBalance(Money feeBalance) {
            this.feeBalance = feeBalance;
            return this;
        }
        
        public Builder monthlyRevenue(Money monthlyRevenue) {
            this.monthlyRevenue = monthlyRevenue;
            return this;
        }
        
        public Builder status(AccountStatus status) {
            this.status = status;
            return this;
        }
        
        public Builder subscriptionStatus(SubscriptionStatus subscriptionStatus) {
            this.subscriptionStatus = subscriptionStatus;
            return this;
        }
        
        public Builder subscriptionTier(String subscriptionTier) {
            this.subscriptionTier = subscriptionTier;
            return this;
        }
        
        public Builder subscriptionStartsAt(LocalDateTime subscriptionStartsAt) {
            this.subscriptionStartsAt = subscriptionStartsAt;
            return this;
        }
        
        public Builder subscriptionExpiresAt(LocalDateTime subscriptionExpiresAt) {
            this.subscriptionExpiresAt = subscriptionExpiresAt;
            return this;
        }
        
        public Builder totalStudents(int totalStudents) {
            this.totalStudents = totalStudents;
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder lastActivityAt(LocalDateTime lastActivityAt) {
            this.lastActivityAt = lastActivityAt;
            return this;
        }
        
        public SchoolAccount build() {
            return new SchoolAccount(this);
        }
    }
}