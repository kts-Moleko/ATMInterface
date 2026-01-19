package com.calpay.audit;

import java.util.List;
import java.util.UUID;

/**
 * Service for logging and querying audit events.
 * 
 * <p><b>Implementation Strategy:</b>
 * <ul>
 *   <li>Primary: Write to dedicated audit database</li>
 *   <li>Backup: Write to append-only log files</li>
 *   <li>Real-time: Stream to SIEM system (future)</li>
 * </ul>
 * 
 * <p><b>Performance Considerations:</b>
 * <ul>
 *   <li>Async writes to avoid blocking business logic</li>
 *   <li>Batch writes for high-volume events</li>
 *   <li>Separate read/write databases</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public interface AuditLogger {
    
    /**
     * Logs a simple audit event.
     * 
     * <p><b>Use case:</b> Quick logging without complex metadata.
     * 
     * @param eventType event type
     * @param entityId affected entity ID
     * @param description human-readable description
     * @param actorId user who performed action
     * @param tenantId school ID
     */
    void log(String eventType, String entityId, String description, String actorId, String tenantId);
    
    /**
     * Logs a detailed audit event.
     * 
     * @param event audit event with full details
     */
    void log(AuditEvent event);
    
    /**
     * Logs a financial transaction event.
     * 
     * <p><b>Special handling:</b> Financial events have stricter
     * compliance requirements and longer retention.
     * 
     * @param eventType event type
     * @param paymentId payment ID
     * @param description description
     * @param actorId actor ID
     * @param tenantId tenant ID
     */
    void logFinancial(String eventType, String paymentId, String description, 
                     String actorId, String tenantId);
    
    /**
     * Logs a security event (authentication, authorization).
     * 
     * <p><b>Use case:</b> Login attempts, permission denials, etc.
     * 
     * @param eventType event type
     * @param userId user ID
     * @param description description
     * @param ipAddress IP address
     * @param success was action successful
     */
    void logSecurity(String eventType, String userId, String description, 
                    String ipAddress, boolean success);
    
    /**
     * Logs a data access event (POPIA compliance).
     * 
     * @param entityType entity type accessed
     * @param entityId entity ID
     * @param action action performed (VIEW, EXPORT)
     * @param actorId user who accessed
     * @param tenantId tenant ID
     */
    void logDataAccess(String entityType, String entityId, String action, 
                      String actorId, String tenantId);
    
    /**
     * Logs a system error for investigation.
     * 
     * @param eventType event type
     * @param description error description
     * @param exception exception details
     * @param severity severity level
     */
    void logError(String eventType, String description, Throwable exception, String severity);
    
    /**
     * Gets recent audit events for an entity.
     * 
     * @param entityType entity type
     * @param entityId entity ID
     * @param limit maximum number of events
     * @return list of audit events
     */
    List<AuditEvent> getEntityHistory(String entityType, String entityId, int limit);
    
    /**
     * Gets audit events by tenant (school).
     * 
     * @param tenantId tenant ID
     * @param limit maximum number of events
     * @return list of audit events
     */
    List<AuditEvent> getTenantEvents(String tenantId, int limit);
    
    /**
     * Gets failed events requiring investigation.
     * 
     * @param limit maximum number of events
     * @return list of failed events
     */
    List<AuditEvent> getFailedEvents(int limit);
    
    /**
     * Gets critical security events.
     * 
     * @param limit maximum number of events
     * @return list of critical events
     */
    List<AuditEvent> getCriticalSecurityEvents(int limit);
}