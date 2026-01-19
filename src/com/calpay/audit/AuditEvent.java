package com.calpay.audit;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents an audit event in the CalPay system.
 * 
 * <p><b>Compliance Requirements:</b>
 * <ul>
 *   <li>Financial Regulations: Track all money movements</li>
 *   <li>POPIA (South Africa): Track data access/modifications</li>
 *   <li>Internal Audit: Support forensic investigation</li>
 * </ul>
 * 
 * <p><b>Retention Policy:</b>
 * <ul>
 *   <li>Financial events: 7 years</li>
 *   <li>Security events: 2 years</li>
 *   <li>User activity: 1 year</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public class AuditEvent {
    
    // Identity
    private final String eventId;
    private final LocalDateTime timestamp;
    
    // Classification
    private final String eventType;       // PAYMENT_CREATED, USER_LOGIN, etc.
    private final String eventCategory;   // FINANCIAL, SECURITY, USER_ACTIVITY
    private final String severity;        // INFO, WARNING, ERROR, CRITICAL
    
    // Context
    private final String entityId;        // ID of affected entity
    private final String entityType;      // Payment, User, School, etc.
    private final String tenantId;        // School ID (multi-tenancy)
    
    // Actor
    private final String actorId;         // User who performed action
    private final String actorType;       // PARENT, ADMIN, SYSTEM
    private final String actorIp;         // IP address (if applicable)
    
    // Details
    private final String action;          // CREATE, UPDATE, DELETE, VIEW
    private final String description;     // Human-readable description
    private final String metadata;        // JSON with additional context
    
    // Outcome
    private final boolean success;        // Was action successful?
    private final String errorMessage;    // Error details if failed
    
    private AuditEvent(Builder builder) {
        this.eventId = Objects.requireNonNull(builder.eventId, "Event ID cannot be null");
        this.timestamp = Objects.requireNonNull(builder.timestamp, "Timestamp cannot be null");
        this.eventType = Objects.requireNonNull(builder.eventType, "Event type cannot be null");
        this.eventCategory = Objects.requireNonNull(builder.eventCategory, "Event category cannot be null");
        this.severity = Objects.requireNonNull(builder.severity, "Severity cannot be null");
        this.entityId = builder.entityId;
        this.entityType = builder.entityType;
        this.tenantId = builder.tenantId;
        this.actorId = builder.actorId;
        this.actorType = builder.actorType;
        this.actorIp = builder.actorIp;
        this.action = builder.action;
        this.description = builder.description;
        this.metadata = builder.metadata;
        this.success = builder.success;
        this.errorMessage = builder.errorMessage;
    }
    
    // Getters
    public String getEventId() { return eventId; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public String getEventType() { return eventType; }
    public String getEventCategory() { return eventCategory; }
    public String getSeverity() { return severity; }
    public String getEntityId() { return entityId; }
    public String getEntityType() { return entityType; }
    public String getTenantId() { return tenantId; }
    public String getActorId() { return actorId; }
    public String getActorType() { return actorType; }
    public String getActorIp() { return actorIp; }
    public String getAction() { return action; }
    public String getDescription() { return description; }
    public String getMetadata() { return metadata; }
    public boolean isSuccess() { return success; }
    public String getErrorMessage() { return errorMessage; }
    
    // Business methods
    public boolean isFinancialEvent() {
        return "FINANCIAL".equals(eventCategory);
    }
    
    public boolean isSecurityEvent() {
        return "SECURITY".equals(eventCategory);
    }
    
    public boolean isCritical() {
        return "CRITICAL".equals(severity);
    }
    
    public boolean requiresAlert() {
        return !success && ("ERROR".equals(severity) || "CRITICAL".equals(severity));
    }
    
    // Builder
    public static class Builder {
        private String eventId;
        private LocalDateTime timestamp = LocalDateTime.now();
        private String eventType;
        private String eventCategory = "USER_ACTIVITY";
        private String severity = "INFO";
        private String entityId;
        private String entityType;
        private String tenantId;
        private String actorId;
        private String actorType;
        private String actorIp;
        private String action;
        private String description;
        private String metadata;
        private boolean success = true;
        private String errorMessage;
        
        public Builder eventId(String eventId) {
            this.eventId = eventId;
            return this;
        }
        
        public Builder timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public Builder eventType(String eventType) {
            this.eventType = eventType;
            return this;
        }
        
        public Builder eventCategory(String eventCategory) {
            this.eventCategory = eventCategory;
            return this;
        }
        
        public Builder severity(String severity) {
            this.severity = severity;
            return this;
        }
        
        public Builder entityId(String entityId) {
            this.entityId = entityId;
            return this;
        }
        
        public Builder entityType(String entityType) {
            this.entityType = entityType;
            return this;
        }
        
        public Builder tenantId(String tenantId) {
            this.tenantId = tenantId;
            return this;
        }
        
        public Builder actorId(String actorId) {
            this.actorId = actorId;
            return this;
        }
        
        public Builder actorType(String actorType) {
            this.actorType = actorType;
            return this;
        }
        
        public Builder actorIp(String actorIp) {
            this.actorIp = actorIp;
            return this;
        }
        
        public Builder action(String action) {
            this.action = action;
            return this;
        }
        
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder metadata(String metadata) {
            this.metadata = metadata;
            return this;
        }
        
        public Builder success(boolean success) {
            this.success = success;
            return this;
        }
        
        public Builder errorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }
        
        public AuditEvent build() {
            return new AuditEvent(this);
        }
    }
    
    @Override
    public String toString() {
        return String.format(
            "AuditEvent{type='%s', category='%s', severity='%s', actor='%s', entity='%s', success=%s, time=%s}",
            eventType, eventCategory, severity, actorId, entityId, success, timestamp
        );
    }
}
