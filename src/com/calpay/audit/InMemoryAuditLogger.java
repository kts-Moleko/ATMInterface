package com.calpay.audit;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * Simple in-memory implementation of AuditLogger.
 * 
 * <p><b>Use case:</b> Testing, development, and demos.
 * 
 * <p><b>Production Note:</b> Replace with database-backed implementation
 * (AuditLoggerJdbc, AuditLoggerJpa) for production use.
 * 
 * <p><b>Thread Safety:</b> Uses ConcurrentLinkedQueue for thread-safe
 * append operations.
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public class InMemoryAuditLogger implements AuditLogger {
    
    private final ConcurrentLinkedQueue<AuditEvent> events = new ConcurrentLinkedQueue<>();
    private final int maxEvents;
    
    /**
     * Creates an in-memory audit logger with default capacity.
     */
    public InMemoryAuditLogger() {
        this(10000); // Default: keep last 10,000 events
    }
    
    /**
     * Creates an in-memory audit logger with specified capacity.
     * 
     * @param maxEvents maximum events to retain
     */
    public InMemoryAuditLogger(int maxEvents) {
        this.maxEvents = maxEvents;
    }
    
    @Override
    public void log(String eventType, String entityId, String description, 
                   String actorId, String tenantId) {
        AuditEvent event = new AuditEvent.Builder()
            .eventId(UUID.randomUUID().toString())
            .timestamp(LocalDateTime.now())
            .eventType(eventType)
            .eventCategory("USER_ACTIVITY")
            .severity("INFO")
            .entityId(entityId)
            .actorId(actorId)
            .tenantId(tenantId)
            .description(description)
            .success(true)
            .build();
        
        log(event);
    }
    
    @Override
    public void log(AuditEvent event) {
        events.add(event);
        
        // Trim if exceeds max size
        while (events.size() > maxEvents) {
            events.poll();
        }
        
        // Print critical events to console
        if (event.isCritical() || !event.isSuccess()) {
            System.err.println("[AUDIT] " + event);
        }
    }
    
    @Override
    public void logFinancial(String eventType, String paymentId, String description, 
                            String actorId, String tenantId) {
        AuditEvent event = new AuditEvent.Builder()
            .eventId(UUID.randomUUID().toString())
            .timestamp(LocalDateTime.now())
            .eventType(eventType)
            .eventCategory("FINANCIAL")
            .severity("INFO")
            .entityId(paymentId)
            .entityType("Payment")
            .actorId(actorId)
            .tenantId(tenantId)
            .description(description)
            .success(true)
            .build();
        
        log(event);
    }
    
    @Override
    public void logSecurity(String eventType, String userId, String description, 
                           String ipAddress, boolean success) {
        AuditEvent event = new AuditEvent.Builder()
            .eventId(UUID.randomUUID().toString())
            .timestamp(LocalDateTime.now())
            .eventType(eventType)
            .eventCategory("SECURITY")
            .severity(success ? "INFO" : "WARNING")
            .entityId(userId)
            .entityType("User")
            .actorId(userId)
            .actorIp(ipAddress)
            .description(description)
            .success(success)
            .build();
        
        log(event);
    }
    
    @Override
    public void logDataAccess(String entityType, String entityId, String action, 
                             String actorId, String tenantId) {
        AuditEvent event = new AuditEvent.Builder()
            .eventId(UUID.randomUUID().toString())
            .timestamp(LocalDateTime.now())
            .eventType("DATA_ACCESS")
            .eventCategory("DATA_ACCESS")
            .severity("INFO")
            .entityType(entityType)
            .entityId(entityId)
            .action(action)
            .actorId(actorId)
            .tenantId(tenantId)
            .description(String.format("%s accessed %s: %s", actorId, entityType, entityId))
            .success(true)
            .build();
        
        log(event);
    }
    
    @Override
    public void logError(String eventType, String description, Throwable exception, String severity) {
        String errorDetails = exception != null ? 
            exception.getClass().getName() + ": " + exception.getMessage() : 
            "No exception details";
        
        AuditEvent event = new AuditEvent.Builder()
            .eventId(UUID.randomUUID().toString())
            .timestamp(LocalDateTime.now())
            .eventType(eventType)
            .eventCategory("ERROR")
            .severity(severity)
            .description(description)
            .errorMessage(errorDetails)
            .success(false)
            .build();
        
        log(event);
    }
    
    @Override
    public List<AuditEvent> getEntityHistory(String entityType, String entityId, int limit) {
        return events.stream()
            .filter(e -> entityType.equals(e.getEntityType()) && entityId.equals(e.getEntityId()))
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<AuditEvent> getTenantEvents(String tenantId, int limit) {
        return events.stream()
            .filter(e -> tenantId.equals(e.getTenantId()))
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<AuditEvent> getFailedEvents(int limit) {
        return events.stream()
            .filter(e -> !e.isSuccess())
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<AuditEvent> getCriticalSecurityEvents(int limit) {
        return events.stream()
            .filter(AuditEvent::isSecurityEvent)
            .filter(AuditEvent::isCritical)
            .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    /**
     * Gets all events (for testing).
     * 
     * @return all audit events
     */
    public List<AuditEvent> getAllEvents() {
        return new ArrayList<>(events);
    }
    
    /**
     * Clears all events (for testing).
     */
    public void clear() {
        events.clear();
    }
    
    /**
     * Gets event count.
     * 
     * @return number of events stored
     */
    public int size() {
        return events.size();
    }
}