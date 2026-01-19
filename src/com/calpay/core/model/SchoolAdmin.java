package com.calpay.core.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a school administrator/staff member who manages the school account.
 * 
 * <p><b>Roles:</b>
 * <ul>
 *   <li>ADMIN: Full access to school management</li>
 *   <li>PRINCIPAL: Full access + approval authority</li>
 *   <li>ACCOUNTANT: Financial operations only</li>
 *   <li>TEACHER: Read-only access to student data</li>
 * </ul>
 * 
 * <p><b>Permissions:</b> Granular permissions stored as Set for flexibility.
 * Examples: "can_approve_refunds", "can_manage_fees", "can_export_data"
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public class SchoolAdmin implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Identity
    private final String adminId;
    private final String schoolId;
    private final String userId;  // Links to User entity for authentication
    private final LocalDateTime createdAt;
    
    // Personal info
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    
    // Role & Permissions
    private String role;  // ADMIN, PRINCIPAL, ACCOUNTANT, TEACHER
    private Set<String> permissions;  // Granular permissions
    
    // Status
    private boolean isActive;
    private LocalDateTime lastLoginAt;
    
    private SchoolAdmin(Builder builder) {
        this.adminId = Objects.requireNonNull(builder.adminId, "Admin ID cannot be null");
        this.schoolId = Objects.requireNonNull(builder.schoolId, "School ID cannot be null");
        this.userId = builder.userId;
        this.firstName = Objects.requireNonNull(builder.firstName, "First name cannot be null");
        this.lastName = Objects.requireNonNull(builder.lastName, "Last name cannot be null");
        this.email = Objects.requireNonNull(builder.email, "Email cannot be null");
        this.phone = builder.phone;
        this.role = builder.role != null ? builder.role : "ADMIN";
        this.permissions = new HashSet<>(builder.permissions);
        this.isActive = builder.isActive;
        this.lastLoginAt = builder.lastLoginAt;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
        
        validateEmail(this.email);
    }
    
    private void validateEmail(String email) {
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
    }
    
    // Getters
    public String getAdminId() { return adminId; }
    public String getSchoolId() { return schoolId; }
    public String getUserId() { return userId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getFullName() { return firstName + " " + lastName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public Set<String> getPermissions() { return new HashSet<>(permissions); }
    public boolean isActive() { return isActive; }
    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    // Setters
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setEmail(String email) {
        validateEmail(email);
        this.email = email;
    }
    public void setPhone(String phone) { this.phone = phone; }
    public void setRole(String role) { this.role = role; }
    public void updateLastLogin() { this.lastLoginAt = LocalDateTime.now(); }
    
    public void activate() { this.isActive = true; }
    public void deactivate() { this.isActive = false; }
    
    // Permission management
    public void grantPermission(String permission) {
        this.permissions.add(permission);
    }
    
    public void revokePermission(String permission) {
        this.permissions.remove(permission);
    }
    
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }
    
    public boolean hasAnyPermission(String... permissions) {
        for (String permission : permissions) {
            if (this.permissions.contains(permission)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasAllPermissions(String... permissions) {
        for (String permission : permissions) {
            if (!this.permissions.contains(permission)) {
                return false;
            }
        }
        return true;
    }
    
    // Role checks
    public boolean isPrincipal() {
        return "PRINCIPAL".equals(role);
    }
    
    public boolean isAccountant() {
        return "ACCOUNTANT".equals(role);
    }
    
    public boolean canApproveRefunds() {
        return hasPermission("can_approve_refunds") || isPrincipal();
    }
    
    public boolean canManageFees() {
        return hasPermission("can_manage_fees") || isPrincipal();
    }
    
    public boolean canExportData() {
        return hasPermission("can_export_data") || isPrincipal();
    }
    
    // Builder
    public static class Builder {
        private String adminId;
        private String schoolId;
        private String userId;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String role = "ADMIN";
        private Set<String> permissions = new HashSet<>();
        private boolean isActive = true;
        private LocalDateTime lastLoginAt;
        private LocalDateTime createdAt;
        
        public Builder adminId(String adminId) {
            this.adminId = adminId;
            return this;
        }
        
        public Builder schoolId(String schoolId) {
            this.schoolId = schoolId;
            return this;
        }
        
        public Builder userId(String userId) {
            this.userId = userId;
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
        
        public Builder email(String email) {
            this.email = email;
            return this;
        }
        
        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }
        
        public Builder role(String role) {
            this.role = role;
            return this;
        }
        
        public Builder permissions(Set<String> permissions) {
            this.permissions = new HashSet<>(permissions);
            return this;
        }
        
        public Builder addPermission(String permission) {
            this.permissions.add(permission);
            return this;
        }
        
        public Builder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }
        
        public Builder lastLoginAt(LocalDateTime lastLoginAt) {
            this.lastLoginAt = lastLoginAt;
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public SchoolAdmin build() {
            return new SchoolAdmin(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SchoolAdmin that = (SchoolAdmin) o;
        return adminId.equals(that.adminId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(adminId);
    }
    
    @Override
    public String toString() {
        return String.format("SchoolAdmin{id='%s', name='%s', role='%s', school='%s', active=%s}",
            adminId, getFullName(), role, schoolId, isActive);
    }
}