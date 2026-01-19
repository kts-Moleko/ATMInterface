package com.calpay.core.exceptions;

/**
 * Thrown when user lacks permission for an operation.
 * 
 * <p><b>Authentication vs Authorization:</b>
 * <ul>
 *   <li>Authentication: "Who are you?" (identity verification)</li>
 *   <li>Authorization: "What can you do?" (permission checking)</li>
 * </ul>
 * 
 * @author CalPay Team
 * @since 2.0
 */
public class AuthorizationException extends CalPayException {
    
    private static final long serialVersionUID = 1L;
    
    private final String userId;
    private final String requiredPermission;
    
    public AuthorizationException(String userId, String requiredPermission) {
        super(
            String.format("User %s lacks permission: %s", userId, requiredPermission),
            "AUTHORIZATION_FAILED"
        );
        this.userId = userId;
        this.requiredPermission = requiredPermission;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getRequiredPermission() {
        return requiredPermission;
    }
}