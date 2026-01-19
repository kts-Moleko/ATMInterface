package com.calpay.core.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a parent/guardian who pays school fees.
 * 
 * <p><b>Key Features:</b>
 * <ul>
 *   <li>Multi-tenancy: Belongs to a specific school</li>
 *   <li>Contact preferences: WhatsApp, SMS, or Email</li>
 *   <li>Language preferences: en, zu, xh, af</li>
 *   <li>Encrypted sensitive data: SA ID number</li>
 *   <li>Phone verification: For WhatsApp notifications</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public class Parent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Identity
    private final String parentId;
    private final String schoolId;
    private final String parentCode;  // e.g., "PAR001" (unique per school)
    private final LocalDateTime createdAt;
    
    // Personal info
    private String firstName;
    private String lastName;
    private String idNumberEncrypted;  // SA ID number (encrypted at rest)
    
    // Contact
    private String email;
    private String phone;  // Primary (WhatsApp)
    private boolean phoneVerified;
    private String alternatePhone;
    
    // Address
    private Address address;
    
    // Preferences
    private String preferredLanguage;  // en, zu, xh, af
    private String preferredNotification;  // WHATSAPP, SMS, EMAIL
    
    // Status
    private boolean isActive;
    
    private Parent(Builder builder) {
        this.parentId = Objects.requireNonNull(builder.parentId, "Parent ID cannot be null");
        this.schoolId = Objects.requireNonNull(builder.schoolId, "School ID cannot be null");
        this.parentCode = Objects.requireNonNull(builder.parentCode, "Parent code cannot be null");
        this.firstName = Objects.requireNonNull(builder.firstName, "First name cannot be null");
        this.lastName = Objects.requireNonNull(builder.lastName, "Last name cannot be null");
        this.phone = Objects.requireNonNull(builder.phone, "Phone cannot be null");
        this.email = builder.email;
        this.idNumberEncrypted = builder.idNumberEncrypted;
        this.phoneVerified = builder.phoneVerified;
        this.alternatePhone = builder.alternatePhone;
        this.address = builder.address;
        this.preferredLanguage = builder.preferredLanguage != null ? builder.preferredLanguage : "en";
        this.preferredNotification = builder.preferredNotification != null ? builder.preferredNotification : "WHATSAPP";
        this.isActive = builder.isActive;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        
        validatePhone(this.phone);
    }
    
    private void validatePhone(String phone) {
        // South African phone format: +27XXXXXXXXX or 0XXXXXXXXX
        if (!phone.matches("^(\\+27|0)[0-9]{9}$")) {
            throw new IllegalArgumentException(
                "Invalid SA phone format. Expected: +27XXXXXXXXX or 0XXXXXXXXX"
            );
        }
    }
    
    // Getters
    public String getParentId() { return parentId; }
    public String getSchoolId() { return schoolId; }
    public String getParentCode() { return parentCode; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getFullName() { return firstName + " " + lastName; }
    public String getIdNumberEncrypted() { return idNumberEncrypted; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public boolean isPhoneVerified() { return phoneVerified; }
    public String getAlternatePhone() { return alternatePhone; }
    public Address getAddress() { return address; }
    public String getPreferredLanguage() { return preferredLanguage; }
    public String getPreferredNotification() { return preferredNotification; }
    public boolean isActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    // Setters for mutable fields
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { 
        validatePhone(phone);
        this.phone = phone;
        this.phoneVerified = false; // Re-verify after change
    }
    public void setPhoneVerified(boolean phoneVerified) { this.phoneVerified = phoneVerified; }
    public void setAlternatePhone(String alternatePhone) { this.alternatePhone = alternatePhone; }
    public void setAddress(Address address) { this.address = address; }
    public void setPreferredLanguage(String preferredLanguage) { this.preferredLanguage = preferredLanguage; }
    public void setPreferredNotification(String preferredNotification) { this.preferredNotification = preferredNotification; }
    
    public void deactivate() { this.isActive = false; }
    public void activate() { this.isActive = true; }
    
    // Business methods
    public boolean canReceiveWhatsApp() {
        return phoneVerified && "WHATSAPP".equals(preferredNotification);
    }
    
    public String getNotificationChannel() {
        if (canReceiveWhatsApp()) {
            return "WHATSAPP";
        } else if (phone != null) {
            return "SMS";
        } else if (email != null) {
            return "EMAIL";
        }
        return null;
    }
    
    // Builder
    public static class Builder {
        private String parentId;
        private String schoolId;
        private String parentCode;
        private String firstName;
        private String lastName;
        private String idNumberEncrypted;
        private String email;
        private String phone;
        private boolean phoneVerified = false;
        private String alternatePhone;
        private Address address;
        private String preferredLanguage = "en";
        private String preferredNotification = "WHATSAPP";
        private boolean isActive = true;
        private LocalDateTime createdAt;
        
        public Builder parentId(String parentId) {
            this.parentId = parentId;
            return this;
        }
        
        public Builder schoolId(String schoolId) {
            this.schoolId = schoolId;
            return this;
        }
        
        public Builder parentCode(String parentCode) {
            this.parentCode = parentCode;
            return this;
        }
        
        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }
        
        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }
        
        public Builder idNumberEncrypted(String idNumberEncrypted) {
            this.idNumberEncrypted = idNumberEncrypted;
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
        
        public Builder phoneVerified(boolean phoneVerified) {
            this.phoneVerified = phoneVerified;
            return this;
        }
        
        public Builder alternatePhone(String alternatePhone) {
            this.alternatePhone = alternatePhone;
            return this;
        }
        
        public Builder address(Address address) {
            this.address = address;
            return this;
        }
        
        public Builder preferredLanguage(String preferredLanguage) {
            this.preferredLanguage = preferredLanguage;
            return this;
        }
        
        public Builder preferredNotification(String preferredNotification) {
            this.preferredNotification = preferredNotification;
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
        
        public Parent build() {
            return new Parent(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Parent parent = (Parent) o;
        return parentId.equals(parent.parentId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(parentId);
    }
    
    @Override
    public String toString() {
        return String.format("Parent{id='%s', code='%s', name='%s', school='%s', phone='%s'}",
            parentId, parentCode, getFullName(), schoolId, phone);
    }
}