package com.calpay.core.exceptions;

/**
 * Generic exception for when a requested resource cannot be found.
 * 
 * <p><b>Replaces:</b>
 * <ul>
 *   <li>AccountNotFoundException → ResourceNotFoundException("Account", id)</li>
 *   <li>UserNotFoundException → ResourceNotFoundException("User", id)</li>
 *   <li>Future: SchoolNotFoundException → ResourceNotFoundException("School", id)</li>
 * </ul>
 * 
 * <p><b>Usage Examples:</b>
 * <pre>{@code
 * // Generic - works for any entity
 * throw new ResourceNotFoundException("Account", accountId);
 * throw new ResourceNotFoundException("User", userId);
 * throw new ResourceNotFoundException("School", schoolId);
 * throw new ResourceNotFoundException("Parent", parentCode);
 * throw new ResourceNotFoundException("Payment", paymentRef);
 * }</pre>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public class ResourceNotFoundException extends CalPayException {
    
    private static final long serialVersionUID = 1L;
    
    private final String resourceType;
    private final String resourceId;
    
    /**
     * Constructs exception with resource type and ID.
     * 
     * @param resourceType the type of resource (e.g., "Account", "User", "School")
     * @param resourceId the identifier that wasn't found
     */
    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(
            String.format("%s not found: %s", resourceType, resourceId),
            "RESOURCE_NOT_FOUND"
        );
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    /**
     * Constructs exception with custom message.
     * 
     * @param resourceType the type of resource
     * @param resourceId the identifier
     * @param message custom error message
     */
    public ResourceNotFoundException(String resourceType, String resourceId, String message) {
        super(message, "RESOURCE_NOT_FOUND");
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    /**
     * Constructs exception with cause.
     * 
     * @param resourceType the type of resource
     * @param resourceId the identifier
     * @param cause the underlying cause
     */
    public ResourceNotFoundException(String resourceType, String resourceId, Throwable cause) {
        super(
            String.format("%s not found: %s", resourceType, resourceId),
            "RESOURCE_NOT_FOUND",
            cause
        );
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public String getResourceId() {
        return resourceId;
    }
    
    @Override
    public String toString() {
        return String.format("ResourceNotFoundException[type=%s, id=%s, message=%s]",
            resourceType, resourceId, getMessage());
    }
}