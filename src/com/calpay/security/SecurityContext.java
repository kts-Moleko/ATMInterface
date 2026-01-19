package com.calpay.security;

import com.calpay.core.enums.UserRole;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;

/**
 * Represents an authenticated user session.
 * 
 * <p><b>Session Management:</b>
 * <ul>
 *   <li>Session timeout: 30 minutes inactivity</li>
 *   <li>Absolute timeout: 8 hours</li>
 *   <li>Renewable: Yes (within absolute timeout)</li>
 * </ul>
 * 
 * <p><b>Security Features:</b>
 * <ul>
 *   <li>IP binding (optional)</li>
 *   <li>Device fingerprinting (future)</li>
 *   <li>Multi-factor authentication flag</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public class SecurityContext {
    
    private final String sessionId;
    private final String userId;
    private final String username;
    private final UserRole role;
    private final String tenantId;  // School ID (for multi-tenancy)
    
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private LocalDateTime lastAccessedAt;
    
    private final String ipAddress;
    private final boolean mfaEnabled;
    private final boolean mfaVerified;
    
    private final Set<String> permissions;
    
    private SecurityContext(Builder builder) {
        this.sessionId = Objects.requireNonNull(builder.sessionId, "Session ID cannot be null");
        this.userId = Objects.requireNonNull(builder.userId, "User ID cannot be null");
        this.username = Objects.requireNonNull(builder.username, "Username cannot be null");
        this.role = Objects.requireNonNull(builder.role, "Role cannot be null");
        this.tenantId = builder.tenantId;
        this.createdAt = Objects.requireNonNull(builder.createdAt, "Created at cannot be null");
        this.expiresAt = Objects.requireNonNull(builder.expiresAt, "Expires at cannot be null");
        this.lastAccessedAt = builder.lastAccessedAt != null ? builder.lastAccessedAt : builder.createdAt;
        this.ipAddress = builder.ipAddress;
        this.mfaEnabled = builder.mfaEnabled;
        this.mfaVerified = builder.mfaVerified;
        this.permissions = new HashSet<>(builder.permissions);
    }
    
    // Getters
    public String getSessionId() { return sessionId; }
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public UserRole getRole() { return role; }
    public String getTenantId() { return tenantId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public LocalDateTime getLastAccessedAt() { return lastAccessedAt; }
    public String getIpAddress() { return ipAddress; }
    public boolean isMfaEnabled() { return mfaEnabled; }
    public boolean isMfaVerified() { return mfaVerified; }
    public Set<String> getPermissions() { return new HashSet<>(permissions); }
    
    /**
     * Updates last accessed timestamp.
     */
    public void updateLastAccessed() {
        this.lastAccessedAt = LocalDateTime.now();
    }
    
    /**
     * Checks if session has expired.
     * 
     * @return true if expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    /**
     * Checks if user has a specific permission.
     * 
     * @param permission permission to check
     * @return true if has permission
     */
    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }
    
    /**
     * Checks if user has any of the specified permissions.
     * 
     * @param permissions permissions to check
     * @return true if has any permission
     */
    public boolean hasAnyPermission(String... permissions) {
        for (String permission : permissions) {
            if (this.permissions.contains(permission)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Checks if user has all specified permissions.
     * 
     * @param permissions permissions to check
     * @return true if has all permissions
     */
    public boolean hasAllPermissions(String... permissions) {
        for (String permission : permissions) {
            if (!this.permissions.contains(permission)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Checks if user belongs to a specific tenant.
     * 
     * @param tenantId tenant ID to check
     * @return true if same tenant
     */
    public boolean belongsToTenant(String tenantId) {
        return this.tenantId != null && this.tenantId.equals(tenantId);
    }
    
    /**
     * Checks if user is an admin.
     * 
     * @return true if admin role
     */
    public boolean isAdmin() {
        return role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN;
    }
    
    /**
     * Checks if MFA is required but not verified.
     * 
     * @return true if MFA pending
     */
    public boolean requiresMfaVerification() {
        return mfaEnabled && !mfaVerified;
    }
    
    // Builder
    public static class Builder {
        private String sessionId;
        private String userId;
        private String username;
        private UserRole role;
        private String tenantId;
        private LocalDateTime createdAt = LocalDateTime.now();
        private LocalDateTime expiresAt;
        private LocalDateTime lastAccessedAt;
        private String ipAddress;
        private boolean mfaEnabled = false;
        private boolean mfaVerified = false;
        private Set<String> permissions = new HashSet<>();
        
        public Builder sessionId(String sessionId) {
            this.sessionId = sessionId;
            return this;
        }
        
        public Builder userId(String userId) {
            this.userId = userId;
            return this;
        }
        
        public Builder username(String username) {
            this.username = username;
            return this;
        }
        
        public Builder role(UserRole role) {
            this.role = role;
            return this;
        }
        
        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }
        
        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }
        
        public Builder expiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }
        
        public Builder lastAccessedAt(LocalDateTime lastAccessedAt) {
            this.lastAccessedAt = lastAccessedAt;
            return this;
        }
        
        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }
        
        public Builder mfaEnabled(boolean mfaEnabled) {
            this.mfaEnabled = mfaEnabled;
            return this;
        }
        
        public Builder mfaVerified(boolean mfaVerified) {
            this.mfaVerified = mfaVerified;
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
        
        public SecurityContext build() {
            return new SecurityContext(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format(
            "SecurityContext{sessionId='%s', userId='%s', username='%s', role=%s, tenantId='%s', expired=%s}",
            sessionId, userId, username, role, tenantId, isExpired()
        );
    }
}