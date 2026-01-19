package com.calpay.core.exceptions;

public class TenantAccessException extends CalPayException {
    
    private static final long serialVersionUID = 1L;
    
    private final String requestedTenantId;
    private final String userTenantId;
    
    public TenantAccessException(String requestedTenantId, String userTenantId) {
        super(
            String.format("Access denied. Cannot access tenant %s from tenant %s", 
                requestedTenantId, userTenantId),
            "TENANT_ACCESS_DENIED"
        );
        this.requestedTenantId = requestedTenantId;
        this.userTenantId = userTenantId;
    }
    
    public String getRequestedTenantId() { return requestedTenantId; }
    public String getUserTenantId() { return userTenantId; }
}