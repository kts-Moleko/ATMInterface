package com.calpay.core.model;

import com.calpay.core.enums.AccountStatus;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a school in the CalPay platform.
 * 
 * <p>This is the tenant entity. All other entities (parents, students, payments)
 * belong to a school. Multi-tenancy isolation ensures School A cannot access
 * School B's data.
 * 
 * <p><b>Key Features:</b>
 * <ul>
 *   <li>Subscription management (TRIAL → ACTIVE → SUSPENDED)</li>
 *   <li>Financial tracking (total revenue, outstanding fees)</li>
 *   <li>Student count tracking</li>
 *   <li>Contact information</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public class School implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Identity
    private final String schoolId;
    private final String schoolCode;  // e.g., "SCH001"
    private final LocalDateTime createdAt;
    
    // Basic info
    private String name;
    private String email;
    private String phone;
    private Address address;
    
    // Subscription
    private String subscriptionStatus;  // TRIAL, ACTIVE, SUSPENDED, CANCELLED
    private String subscriptionTier;    // TIER_1, TIER_2, TIER_3
    private LocalDateTime subscriptionStartsAt;
    private LocalDateTime subscriptionExpiresAt;
    
    // Business metrics
    private int totalStudents;
    private Money monthlyRevenue;
    private Money outstandingFees;
    
    // Status
    private AccountStatus status;
    private boolean isActive;
    
    private School(Builder builder) {
        this.schoolId = Objects.requireNonNull(builder.schoolId, "School ID cannot be null");
        this.schoolCode = Objects.requireNonNull(builder.schoolCode, "School code cannot be null");
        this.name = Objects.requireNonNull(builder.name, "School name cannot be null");
        this.email = builder.email;
        this.phone = builder.phone;
        this.address = builder.address;
        this.subscriptionStatus = builder.subscriptionStatus != null ? builder.subscriptionStatus : "TRIAL";
        this.subscriptionTier = builder.subscriptionTier != null ? builder.subscriptionTier : "TIER_1";
        this.subscriptionStartsAt = builder.subscriptionStartsAt;
        this.subscriptionExpiresAt = builder.subscriptionExpiresAt;
        this.totalStudents = builder.totalStudents;
        this.monthlyRevenue = builder.monthlyRevenue != null ? builder.monthlyRevenue : Money.zero("ZAR");
        this.outstandingFees = builder.outstandingFees != null ? builder.outstandingFees : Money.zero("ZAR");
        this.status = builder.status != null ? builder.status : AccountStatus.PENDING;
        this.isActive = builder.isActive;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        
        validateSchoolCode(this.schoolCode);
    }
    
    private void validateSchoolCode(String schoolCode) {
        if (!schoolCode.matches("SCH\\d{3,6}")) {
            throw new IllegalArgumentException(
                "Invalid school code format. Expected: SCH### (e.g., SCH001)"
            );
        }
    }
    
    // Getters
    public String getSchoolId() { return schoolId; }
    public String getSchoolCode() { return schoolCode; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public Address getAddress() { return address; }
    public String getSubscriptionStatus() { return subscriptionStatus; }
    public String getSubscriptionTier() { return subscriptionTier; }
    public LocalDateTime getSubscriptionStartsAt() { return subscriptionStartsAt; }
    public LocalDateTime getSubscriptionExpiresAt() { return subscriptionExpiresAt; }
    public int getTotalStudents() { return totalStudents; }
    public Money getMonthlyRevenue() { return monthlyRevenue; }
    public Money getOutstandingFees() { return outstandingFees; }
    public AccountStatus getStatus() { return status; }
    public boolean isActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    // Business methods
    public boolean isSubscriptionActive() {
        return "ACTIVE".equals(subscriptionStatus) && 
               (subscriptionExpiresAt == null || subscriptionExpiresAt.isAfter(LocalDateTime.now()));
    }
    
    public boolean isTrialExpired() {
        return "TRIAL".equals(subscriptionStatus) && 
               subscriptionExpiresAt != null && 
               subscriptionExpiresAt.isBefore(LocalDateTime.now());
    }
    
    public void activate() {
        this.subscriptionStatus = "ACTIVE";
        this.status = AccountStatus.ACTIVE;
        this.isActive = true;
    }
    
    public void suspend(String reason) {
        this.subscriptionStatus = "SUSPENDED";
        this.status = AccountStatus.SUSPENDED;
        this.isActive = false;
    }
    
    public void updateStudentCount(int count) {
        this.totalStudents = count;
    }
    
    public void addRevenue(Money amount) {
        this.monthlyRevenue = this.monthlyRevenue.add(amount);
    }
    
    public void addOutstandingFees(Money amount) {
        this.outstandingFees = this.outstandingFees.add(amount);
    }
    
    public void reduceOutstandingFees(Money amount) {
        this.outstandingFees = this.outstandingFees.subtract(amount);
    }
    
    // Builder
    public static class Builder {
        private String schoolId;
        private String schoolCode;
        private String name;
        private String email;
        private String phone;
        private Address address;
        private String subscriptionStatus = "TRIAL";
        private String subscriptionTier = "TIER_1";
        private LocalDateTime subscriptionStartsAt;
        private LocalDateTime subscriptionExpiresAt;
        private int totalStudents = 0;
        private Money monthlyRevenue;
        private Money outstandingFees;
        private AccountStatus status = AccountStatus.PENDING;
        private boolean isActive = true;
        private LocalDateTime createdAt;
        
        public Builder schoolId(String schoolId) {
            this.schoolId = schoolId;
            return this;
        }
        
        public Builder schoolCode(String schoolCode) {
            this.schoolCode = schoolCode;
            return this;
        }
        
        public Builder name(String name) {
            this.name = name;
            return this;
        }
        
        public Builder email(String email) {
            this.email = email;
            return this;
        }
        
        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }
        
        public Builder address(Address address) {
            this.address = address;
            return this;
        }
        
        public Builder subscriptionStatus(String subscriptionStatus) {
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
        
        public Builder status(AccountStatus status) {
            this.status = status;
            return this;
        }
        
        public School build() {
            return new School(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        School school = (School) o;
        return schoolId.equals(school.schoolId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(schoolId);
    }
    
    @Override
    public String toString() {
        return String.format("School{id='%s', code='%s', name='%s', subscription=%s, students=%d}",
            schoolId, schoolCode, name, subscriptionStatus, totalStudents);
    }
}