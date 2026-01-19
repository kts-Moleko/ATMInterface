package com.calpay.core.enums;

import java.util.EnumSet;
import java.util.Set;

/**
 * Defines user roles for role-based access control (RBAC).
 * 
 * <p><b>Role Hierarchy:</b>
 * <pre>
 * SUPER_ADMIN (all permissions)
 *     ↓
 * ADMIN (most permissions)
 *     ↓
 * SUPPORT (read + assist)
 *     ↓
 * CUSTOMER (basic operations)
 *     ↓
 * SYSTEM (automated processes)
 * </pre>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public enum UserRole {
    
    /**
     * Regular customer with basic account operations.
     * Can view balance, transfer money, make payments.
     * Default role for new users.
     */
    CUSTOMER(1, "Customer"),
    
    /**
     * Customer support representative.
     * Can view customer accounts (read-only), search users,
     * view KYC status, add notes, initiate password resets.
     * Cannot modify balances or perform transfers.
     */
    SUPPORT(2, "Support Agent"),
    
    /**
     * System administrator with elevated privileges.
     * Can suspend accounts, approve KYC, make adjustments,
     * view audit logs, generate reports, configure limits.
     * Cannot create other admins or change core system config.
     */
    ADMIN(3, "Administrator"),
    
    /**
     * Super administrator with full system access.
     * Can manage other admins, change system configuration,
     * access production database, override security restrictions.
     * Strictly limited to 2-3 people maximum.
     */
    SUPER_ADMIN(4, "Super Administrator"),
    
    /**
     * System service account for automated processes.
     * Used for scheduled jobs (interest calculation, reports),
     * external API integrations (KYC, payment gateways).
     * Cannot log in via user interface.
     */
    SYSTEM(0, "System");
    
    private final int level;
    private final String displayName;
    
    /**
     * Constructs a UserRole enum value.
     * 
     * @param level numeric level for hierarchy comparison (higher = more privileges)
     * @param displayName user-friendly name for UI display
     */
    UserRole(int level, String displayName) {
        this.level = level;
        this.displayName = displayName;
    }
    
    /**
     * Returns the privilege level of this role.
     * Higher numbers = more privileges.
     * 
     * @return privilege level (0 = SYSTEM, 1 = CUSTOMER, ... 4 = SUPER_ADMIN)
     */
    public int getLevel() {
        return level;
    }
    
    /**
     * Returns user-friendly display name.
     * 
     * @return display name suitable for showing to users
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Checks if this role has at least the privileges of another role.
     * 
     * @param otherRole the role to compare against
     * @return true if this role's level >= other role's level
     */
    public boolean hasPrivilegeLevel(UserRole otherRole) {
        return this.level >= otherRole.level;
    }
    
    /**
     * Checks if this is an administrative role.
     * 
     * @return true if role is ADMIN or SUPER_ADMIN
     */
    public boolean isAdmin() {
        return this == ADMIN || this == SUPER_ADMIN;
    }
    
    /**
     * Checks if this is a customer-facing role.
     * 
     * @return true if role is CUSTOMER or SUPPORT
     */
    public boolean isCustomerFacing() {
        return this == CUSTOMER || this == SUPPORT;
    }
    
    /**
     * Checks if this is a staff role (not customer).
     * 
     * @return true if role is SUPPORT, ADMIN, or SUPER_ADMIN
     */
    public boolean isStaff() {
        return this == SUPPORT || this == ADMIN || this == SUPER_ADMIN;
    }
    
    /**
     * Checks if this is an automated system account.
     * 
     * @return true if role is SYSTEM
     */
    public boolean isSystem() {
        return this == SYSTEM;
    }
    
    /**
     * Returns all roles that this role can manage.
     * 
     * <p><b>Management rules:</b>
     * <ul>
     *   <li>CUSTOMER: Cannot manage any roles</li>
     *   <li>SUPPORT: Cannot manage any roles</li>
     *   <li>ADMIN: Can manage CUSTOMER and SUPPORT</li>
     *   <li>SUPER_ADMIN: Can manage all roles</li>
     *   <li>SYSTEM: Cannot manage any roles</li>
     * </ul>
     * 
     * @return set of roles this role can manage
     */
    public Set<UserRole> getManageableRoles() {
        switch (this) {
            case SUPER_ADMIN:
                return EnumSet.allOf(UserRole.class);
            case ADMIN:
                return EnumSet.of(CUSTOMER, SUPPORT);
            default:
                return EnumSet.noneOf(UserRole.class);
        }
    }
}