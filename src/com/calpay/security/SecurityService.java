package com.calpay.security;

import com.calpay.core.exceptions.AuthenticationException;
import com.calpay.core.exceptions.AuthorizationException;

/**
 * Service for authentication and authorization operations.
 * 
 * <p><b>Authentication Methods:</b>
 * <ul>
 *   <li>Username/Password (primary)</li>
 *   <li>API Key (for system integration)</li>
 *   <li>OAuth2 (future: Google, Microsoft)</li>
 * </ul>
 * 
 * <p><b>Security Features:</b>
 * <ul>
 *   <li>Password hashing: BCrypt (cost factor 12)</li>
 *   <li>Brute force protection: Max 5 failed attempts</li>
 *   <li>Session management: In-memory or Redis</li>
 *   <li>MFA support: TOTP (Google Authenticator)</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public interface SecurityService {
    
    /**
     * Authenticates a user with username and password.
     * 
     * <p><b>Process:</b>
     * <ol>
     *   <li>Check if account is locked (brute force protection)</li>
     *   <li>Validate credentials</li>
     *   <li>Check if account is active</li>
     *   <li>Create security context</li>
     *   <li>Log authentication event</li>
     * </ol>
     * 
     * @param username username or email
     * @param password password (plain text, will be hashed)
     * @param ipAddress client IP address
     * @return security context
     * @throws AuthenticationException if authentication fails
     */
    SecurityContext authenticate(String username, String password, String ipAddress);
    
    /**
     * Authenticates using an API key.
     * 
     * <p><b>Use case:</b> System-to-system integration.
     * 
     * @param apiKey API key
     * @return security context
     * @throws AuthenticationException if invalid key
     */
    SecurityContext authenticateWithApiKey(String apiKey);
    
    /**
     * Verifies MFA code.
     * 
     * @param sessionId session ID
     * @param mfaCode 6-digit TOTP code
     * @return updated security context
     * @throws AuthenticationException if invalid code
     */
    SecurityContext verifyMfa(String sessionId, String mfaCode);
    
    /**
     * Validates an existing session.
     * 
     * @param sessionId session ID
     * @return security context
     * @throws AuthenticationException if session invalid or expired
     */
    SecurityContext validateSession(String sessionId);
    
    /**
     * Refreshes a session (extends expiration).
     * 
     * @param sessionId session ID
     * @return updated security context
     */
    SecurityContext refreshSession(String sessionId);
    
    /**
     * Logs out a user (invalidates session).
     * 
     * @param sessionId session ID
     */
    void logout(String sessionId);
    
    /**
     * Checks if user has permission to access a resource.
     * 
     * @param context security context
     * @param permission required permission
     * @throws AuthorizationException if permission denied
     */
    void checkPermission(SecurityContext context, String permission);
    
    /**
     * Checks if user can access a tenant's resources.
     * 
     * @param context security context
     * @param tenantId tenant ID to access
     * @throws AuthorizationException if access denied
     */
    void checkTenantAccess(SecurityContext context, String tenantId);
    
    /**
     * Hashes a password using BCrypt.
     * 
     * @param plainPassword plain text password
     * @return hashed password
     */
    String hashPassword(String plainPassword);
    
    /**
     * Verifies a password against a hash.
     * 
     * @param plainPassword plain text password
     * @param hashedPassword hashed password
     * @return true if password matches
     */
    boolean verifyPassword(String plainPassword, String hashedPassword);
    
    /**
     * Generates a secure random API key.
     * 
     * @return API key
     */
    String generateApiKey();
    
    /**
     * Records a failed login attempt.
     * 
     * @param username username
     * @param ipAddress IP address
     */
    void recordFailedLogin(String username, String ipAddress);
    
    /**
     * Checks if an account is locked due to failed attempts.
     * 
     * @param username username
     * @return true if locked
     */
    boolean isAccountLocked(String username);
    
    /**
     * Unlocks an account.
     * 
     * @param username username
     */
    void unlockAccount(String username);
}