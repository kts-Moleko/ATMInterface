package com.calpay.core.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a fee structure/definition at a school.
 * 
 * <p><b>Examples:</b>
 * <ul>
 *   <li>Grade 10 Tuition - R2500/month</li>
 *   <li>Soccer Kit - R450 one-time</li>
 *   <li>School Transport - R600/month</li>
 * </ul>
 * 
 * <p><b>Frequency Options:</b>
 * <ul>
 *   <li>MONTHLY: Recurring monthly (e.g., tuition)</li>
 *   <li>QUARTERLY: Every 3 months</li>
 *   <li>ANNUALLY: Once per year</li>
 *   <li>ONE_TIME: One-off payment (e.g., uniform)</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public class FeeStructure implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Identity
    private final String feeId;
    private final String schoolId;
    private final LocalDateTime createdAt;
    
    // Fee details
    private String feeType;  // TUITION, SPORT, UNIFORM, TRANSPORT, MEALS
    private String feeName;  // "Grade 10 Tuition", "Soccer Kit"
    private String description;
    
    // Amount
    private Money amount;
    
    // Recurrence
    private String frequency;  // MONTHLY, QUARTERLY, ANNUALLY, ONE_TIME
    private Integer dueDayOfMonth;  // 1-31 for monthly fees
    
    // Applicability
    private String gradeLevel;  // "Grade 10", "All Grades", null = all
    private Integer academicYear;  // 2025
    
    // Status
    private boolean isMandatory;
    private boolean isActive;
    
    // Metadata
    private LocalDateTime updatedAt;
    private String updatedBy;
    
    private FeeStructure(Builder builder) {
        this.feeId = Objects.requireNonNull(builder.feeId, "Fee ID cannot be null");
        this.schoolId = Objects.requireNonNull(builder.schoolId, "School ID cannot be null");
        this.feeType = Objects.requireNonNull(builder.feeType, "Fee type cannot be null");
        this.feeName = Objects.requireNonNull(builder.feeName, "Fee name cannot be null");
        this.amount = Objects.requireNonNull(builder.amount, "Amount cannot be null");
        this.frequency = Objects.requireNonNull(builder.frequency, "Frequency cannot be null");
        this.description = builder.description;
        this.dueDayOfMonth = builder.dueDayOfMonth;
        this.gradeLevel = builder.gradeLevel;
        this.academicYear = builder.academicYear;
        this.isMandatory = builder.isMandatory;
        this.isActive = builder.isActive;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        this.updatedAt = builder.updatedAt;
        this.updatedBy = builder.updatedBy;
        
        validateFrequency();
    }
    
    private void validateFrequency() {
        if ("MONTHLY".equals(frequency) && dueDayOfMonth == null) {
            throw new IllegalArgumentException("Monthly fees must have dueDayOfMonth set");
        }
        
        if (dueDayOfMonth != null && (dueDayOfMonth < 1 || dueDayOfMonth > 31)) {
            throw new IllegalArgumentException("dueDayOfMonth must be between 1 and 31");
        }
    }
    
    // Getters
    public String getFeeId() { return feeId; }
    public String getSchoolId() { return schoolId; }
    public String getFeeType() { return feeType; }
    public String getFeeName() { return feeName; }
    public String getDescription() { return description; }
    public Money getAmount() { return amount; }
    public String getFrequency() { return frequency; }
    public Integer getDueDayOfMonth() { return dueDayOfMonth; }
    public String getGradeLevel() { return gradeLevel; }
    public Integer getAcademicYear() { return academicYear; }
    public boolean isMandatory() { return isMandatory; }
    public boolean isActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getUpdatedBy() { return updatedBy; }
    
    // Setters
    public void setFeeName(String feeName) { 
        this.feeName = feeName;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setDescription(String description) { 
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setAmount(Money amount) { 
        this.amount = amount;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setFrequency(String frequency) { 
        this.frequency = frequency;
        validateFrequency();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setDueDayOfMonth(Integer dueDayOfMonth) { 
        this.dueDayOfMonth = dueDayOfMonth;
        validateFrequency();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setMandatory(boolean mandatory) { 
        this.isMandatory = mandatory;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void activate() { 
        this.isActive = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void deactivate() { 
        this.isActive = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void markUpdated(String updatedBy) {
        this.updatedAt = LocalDateTime.now();
        this.updatedBy = updatedBy;
    }
    
    // Business methods
    public boolean appliesTo(String gradeLevel) {
        return this.gradeLevel == null || 
               "All Grades".equals(this.gradeLevel) || 
               gradeLevel.equals(this.gradeLevel);
    }
    
    public boolean isRecurring() {
        return "MONTHLY".equals(frequency) || 
               "QUARTERLY".equals(frequency) || 
               "ANNUALLY".equals(frequency);
    }
    
    public boolean isOneTime() {
        return "ONE_TIME".equals(frequency);
    }
    
    // Builder
    public static class Builder {
        private String feeId;
        private String schoolId;
        private String feeType;
        private String feeName;
        private String description;
        private Money amount;
        private String frequency;
        private Integer dueDayOfMonth;
        private String gradeLevel;
        private Integer academicYear;
        private boolean isMandatory = true;
        private boolean isActive = true;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String updatedBy;
        
        public Builder feeId(String feeId) {
            this.feeId = feeId;
            return this;
        }
        
        public Builder schoolId(String schoolId) {
            this.schoolId = schoolId;
            return this;
        }
        
        public Builder feeType(String feeType) {
            this.feeType = feeType;
            return this;
        }
        
        public Builder feeName(String feeName) {
            this.feeName = feeName;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder amount(Money amount) {
            this.amount = amount;
            return this;
        }
        
        public Builder frequency(String frequency) {
            this.frequency = frequency;
            return this;
        }
        
        public Builder dueDayOfMonth(Integer dueDayOfMonth) {
            this.dueDayOfMonth = dueDayOfMonth;
            return this;
        }
        
        public Builder gradeLevel(String gradeLevel) {
            this.gradeLevel = gradeLevel;
            return this;
        }
        
        public Builder academicYear(Integer academicYear) {
            this.academicYear = academicYear;
            return this;
        }
        
        public Builder isMandatory(boolean isMandatory) {
            this.isMandatory = isMandatory;
            return this;
        }
        
        public Builder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public FeeStructure build() {
            return new FeeStructure(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeeStructure that = (FeeStructure) o;
        return feeId.equals(that.feeId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(feeId);
    }
    
    @Override
    public String toString() {
        return String.format("FeeStructure{id='%s', name='%s', amount=%s, frequency='%s', grade='%s'}",
            feeId, feeName, amount, frequency, gradeLevel != null ? gradeLevel : "All");
    }
}