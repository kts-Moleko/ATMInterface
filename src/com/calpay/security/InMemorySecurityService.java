package com.calpay.security;

import com.calpay.core.enums.UserRole;
import com.calpay.core.exceptions.AuthenticationException;
import com.calpay.core.exceptions.AuthorizationException;
import com.calpay.audit.AuditLogger;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple in-memory implementation of SecurityService.
 * 
 * <p><b>Use case:</b> Testing, development, demos.
 * 
 * <p><b>Production Note:</b> Replace with database-backed implementation
 * that persists users, sessions, and failed login attempts.
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public class InMemorySecurityService implements SecurityService {
    
    private final AuditLogger auditLogger;
    
    // In-memory storage
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, SecurityContext> sessions = new ConcurrentHashMap<>();
    private final Map<String, Integer> failedAttempts = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> lockedAccounts = new ConcurrentHashMap<>();
    
    // Configuration
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 15;
    private static final int SESSION_TIMEOUT_MINUTES = 30;
    
    /**
     * Simple user record for in-memory storage.
     */
    private static class User {
        String userId;
        String username;
        String passwordHash;
        UserRole role;
        String tenantId;
        boolean isActive;
        Set<String> permissions;
        
        User(String userId, String username, String passwordHash, UserRole role, 
             String tenantId, boolean isActive, Set<String> permissions) {
            this.userId = userId;
            this.username = username;
            this.passwordHash = passwordHash;
            this.role = role;
            this.tenantId = tenantId;
            this.isActive = isActive;
            this.permissions = permissions;
        }
    }
    
    public InMemorySecurityService(AuditLogger auditLogger) {
        this.auditLogger = Objects.requireNonNull(auditLogger);
        initializeDefaultUsers();
    }
    
    /**
     * Initializes some default users for testing.
     */
    private void initializeDefaultUsers() {
        // Admin user
        addUser("admin", "admin123", UserRole.ADMIN, null, 
                Set.of("all_permissions"));
        
        // School admin
        addUser("school_admin", "school123", UserRole.ADMIN, "SCH001", 
                Set.of("manage_fees", "view_payments", "manage_students"));
        
        // Parent user
        addUser("parent1", "parent123", UserRole.CUSTOMER, "SCH001", 
                Set.of("view_payments", "make_payment"));
    }
    
    /**
     * Adds a user (for testing).
     */
    public void addUser(String username, String password, UserRole role, 
                       String tenantId, Set<String> permissions) {
        String userId = UUID.randomUUID().toString();
        String passwordHash = hashPassword(password);
        
        users.put(username, new User(
            userId, username, passwordHash, role, tenantId, true, permissions
        ));
    }
    
    @Override
    public SecurityContext authenticate(String username, String password, String ipAddress) {
        // Check if account is locked
        if (isAccountLocked(username)) {
            auditLogger.logSecurity("LOGIN_FAILED", username, 
                "Account locked due to too many failed attempts", ipAddress, false);
            throw new AuthenticationException("Account is locked. Please try again later.");
        }
        
        // Find user
        User user = users.get(username);
        if (user == null || !verifyPassword(password, user.passwordHash)) {
            recordFailedLogin(username, ipAddress);
            auditLogger.logSecurity("LOGIN_FAILED", username, 
                "Invalid credentials", ipAddress, false);
            throw new AuthenticationException("Invalid credentials");
        }
        
        // Check if account is active
        if (!user.isActive) {
            auditLogger.logSecurity("LOGIN_FAILED", user.userId, 
                "Account is not active", ipAddress, false);
            throw new AuthenticationException("Account is not active");
        }
        
        // Clear failed attempts
        failedAttempts.remove(username);
        
        // Create session
        String sessionId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        
        SecurityContext context = new SecurityContext.Builder()
            .sessionId(sessionId)
            .userId(user.userId)
            .username(user.username)
            .role(user.role)
            .tenantId(user.tenantId)
            .createdAt(now)
            .expiresAt(now.plusMinutes(SESSION_TIMEOUT_MINUTES))
            .ipAddress(ipAddress)
            .permissions(user.permissions)
            .build();
        
        sessions.put(sessionId, context);
        
        // Audit log
        auditLogger.logSecurity("LOGIN_SUCCESS", user.userId, 
            "User logged in successfully", ipAddress, true);
        
        return context;
    }
    
    @Override
    public SecurityContext authenticateWithApiKey(String apiKey) {
        throw new UnsupportedOperationException("API key authentication not implemented in demo");
    }
    
    @Override
    public SecurityContext verifyMfa(String sessionId, String mfaCode) {
        throw new UnsupportedOperationException("MFA not implemented in demo");
    }
    
    @Override
    public SecurityContext validateSession(String sessionId) {
        SecurityContext context = sessions.get(sessionId);
        
        if (context == null) {
            throw new AuthenticationException("Invalid session");
        }
        
        if (context.isExpired()) {
            sessions.remove(sessionId);
            throw new AuthenticationException("Session expired");
        }
        
        // Update last accessed
        context.updateLastAccessed();
        
        return context;
    }
    
    @Override
    public SecurityContext refreshSession(String sessionId) {
        SecurityContext oldContext = validateSession(sessionId);
        
        LocalDateTime now = LocalDateTime.now();
        SecurityContext newContext = new SecurityContext.Builder()
            .sessionId(sessionId)
            .userId(oldContext.getUserId())
            .username(oldContext.getUsername())
            .role(oldContext.getRole())
            .tenantId(oldContext.getTenantId())
            .createdAt(oldContext.getCreatedAt())
            .expiresAt(now.plusMinutes(SESSION_TIMEOUT_MINUTES))
            .lastAccessedAt(now)
            .ipAddress(oldContext.getIpAddress())
            .permissions(oldContext.getPermissions())
            .build();
        
        sessions.put(sessionId, newContext);
        
        return newContext;
    }
    
    @Override
    public void logout(String sessionId) {
        SecurityContext context = sessions.remove(sessionId);
        
        if (context != null) {
            auditLogger.logSecurity("LOGOUT", context.getUserId(), 
                "User logged out", context.getIpAddress(), true);
        }
    }
    
    @Override
    public void checkPermission(SecurityContext context, String permission) {
        if (!context.hasPermission(permission) && !context.hasPermission("all_permissions")) {
            auditLogger.logSecurity("PERMISSION_DENIED", context.getUserId(), 
                "Missing permission: " + permission, context.getIpAddress(), false);
            throw new AuthorizationException(context.getUserId(), permission);
        }
    }
    
    @Override
    public void checkTenantAccess(SecurityContext context, String tenantId) {
        // Super admin can access all tenants
        if (context.getRole() == UserRole.SUPER_ADMIN) {
            return;
        }
        
        // Regular users must belong to the same tenant
        if (!context.belongsToTenant(tenantId)) {
            auditLogger.logSecurity("TENANT_ACCESS_DENIED", context.getUserId(), 
                "Attempted access to tenant: " + tenantId, context.getIpAddress(), false);
            throw new AuthorizationException(context.getUserId(), "access_tenant_" + tenantId);
        }
    }
    
    @Override
    public String hashPassword(String plainPassword) {
        // Simple hash for demo (use BCrypt in production)
        return "hashed_" + plainPassword;
    }
    
    @Override
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        // Simple verification for demo
        return hashedPassword.equals("hashed_" + plainPassword);
    }
    
    @Override
    public String generateApiKey() {
        return "api_" + UUID.randomUUID().toString().replace("-", "");
    }
    
    @Override
    public void recordFailedLogin(String username, String ipAddress) {
        int attempts = failedAttempts.getOrDefault(username, 0) + 1;
        failedAttempts.put(username, attempts);
        
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            lockedAccounts.put(username, LocalDateTime.now().plusMinutes(LOCKOUT_DURATION_MINUTES));
            auditLogger.logSecurity("ACCOUNT_LOCKED", username, 
                "Account locked due to " + attempts + " failed attempts", ipAddress, false);
        }
    }
    
    @Override
    public boolean isAccountLocked(String username) {
        LocalDateTime lockExpiry = lockedAccounts.get(username);
        
        if (lockExpiry == null) {
            return false;
        }
        
        if (LocalDateTime.now().isAfter(lockExpiry)) {
            lockedAccounts.remove(username);
            failedAttempts.remove(username);
            return false;
        }
        
        return true;
    }
    
    @Override
    public void unlockAccount(String username) {
        lockedAccounts.remove(username);
        failedAttempts.remove(username);
        
        auditLogger.logSecurity("ACCOUNT_UNLOCKED", username, 
            "Account manually unlocked", null, true);
    }
    
    /**
     * Gets all active sessions (for testing).
     */
    public int getActiveSessionCount() {
        return sessions.size();
    }
    
    /**
     * Clears all sessions (for testing).
     */
    public void clearSessions() {
        sessions.clear();
    }
}