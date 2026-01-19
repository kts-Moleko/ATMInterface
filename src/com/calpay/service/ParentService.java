package com.calpay.service;

import com.calpay.audit.AuditLogger;
import com.calpay.core.exceptions.InvalidRequestException;
import com.calpay.core.exceptions.ResourceNotFoundException;
import com.calpay.core.model.Address;
import com.calpay.core.model.Parent;
import com.calpay.core.model.School;
import com.calpay.repository.ParentRepository;
import com.calpay.repository.SchoolRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Service for managing parents/guardians.
 * 
 * <p><b>Key Responsibilities:</b>
 * <ul>
 *   <li>Parent registration</li>
 *   <li>Parent profile updates</li>
 *   <li>Bulk parent import (CSV)</li>
 *   <li>Phone verification</li>
 *   <li>Communication preferences</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public class ParentService {
    
    private final ParentRepository parentRepository;
    private final SchoolRepository schoolRepository;
    private final AuditLogger auditLogger;
    
    public ParentService(
            ParentRepository parentRepository,
            SchoolRepository schoolRepository,
            AuditLogger auditLogger) {
        this.parentRepository = Objects.requireNonNull(parentRepository);
        this.schoolRepository = Objects.requireNonNull(schoolRepository);
        this.auditLogger = Objects.requireNonNull(auditLogger);
    }
    
    /**
     * Registers a new parent.
     * 
     * <p><b>Process:</b>
     * <ol>
     *   <li>Validate school exists</li>
     *   <li>Generate parent code (e.g., PAR001)</li>
     *   <li>Validate phone number format</li>
     *   <li>Check for duplicate phone within school</li>
     *   <li>Create parent entity</li>
     *   <li>Audit log</li>
     * </ol>
     * 
     * @param schoolId school ID
     * @param firstName first name
     * @param lastName last name
     * @param phone phone number (+27XXXXXXXXX)
     * @param email email (optional)
     * @return created parent
     */
    public Parent registerParent(
            String schoolId,
            String firstName,
            String lastName,
            String phone,
            String email) {
        
        // Validate school exists
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("School", schoolId));
        
        // Validate inputs
        validateName(firstName, "First name");
        validateName(lastName, "Last name");
        validatePhone(phone);
        
        // Check for duplicate phone in same school
        if (parentRepository.existsBySchoolIdAndPhone(schoolId, phone)) {
            throw new InvalidRequestException(
                "Parent with this phone number already exists at this school: " + phone
            );
        }
        
        // Generate parent code
        String parentCode = generateParentCode(schoolId);
        String parentId = UUID.randomUUID().toString();
        
        // Create parent
        Parent parent = new Parent.Builder()
            .parentId(parentId)
            .schoolId(schoolId)
            .parentCode(parentCode)
            .firstName(firstName)
            .lastName(lastName)
            .phone(normalizePhone(phone))
            .email(email)
            .preferredLanguage("en")
            .preferredNotification("WHATSAPP")
            .isActive(true)
            .build();
        
        // Save
        Parent savedParent = parentRepository.save(parent);
        
        // Audit
        auditLogger.log(
            "PARENT_REGISTERED",
            parentId,
            String.format("Parent registered: %s (Code: %s)", parent.getFullName(), parentCode),
            null,
            schoolId
        );
        
        return savedParent;
    }
    
    /**
     * Updates parent profile.
     * 
     * @param parentId parent ID
     * @param firstName new first name (optional)
     * @param lastName new last name (optional)
     * @param email new email (optional)
     * @param alternatePhone new alternate phone (optional)
     */
    public void updateParent(
            String parentId,
            String firstName,
            String lastName,
            String email,
            String alternatePhone) {
        
        Parent parent = findParentById(parentId);
        
        if (firstName != null) {
            validateName(firstName, "First name");
            parent.setFirstName(firstName);
        }
        
        if (lastName != null) {
            validateName(lastName, "Last name");
            parent.setLastName(lastName);
        }
        
        if (email != null) {
            parent.setEmail(email);
        }
        
        if (alternatePhone != null) {
            validatePhone(alternatePhone);
            parent.setAlternatePhone(normalizePhone(alternatePhone));
        }
        
        parentRepository.save(parent);
        
        auditLogger.log(
            "PARENT_UPDATED",
            parentId,
            "Parent profile updated",
            null,
            parent.getSchoolId()
        );
    }
    
    /**
     * Updates parent address.
     * 
     * @param parentId parent ID
     * @param address new address
     */
    public void updateAddress(String parentId, Address address) {
        Parent parent = findParentById(parentId);
        parent.setAddress(address);
        parentRepository.save(parent);
        
        auditLogger.log(
            "PARENT_ADDRESS_UPDATED",
            parentId,
            "Parent address updated",
            null,
            parent.getSchoolId()
        );
    }
    
    /**
     * Updates parent communication preferences.
     * 
     * @param parentId parent ID
     * @param language preferred language (en, zu, xh, af)
     * @param notificationChannel preferred channel (WHATSAPP, SMS, EMAIL)
     */
    public void updatePreferences(
            String parentId,
            String language,
            String notificationChannel) {
        
        Parent parent = findParentById(parentId);
        
        if (language != null) {
            parent.setPreferredLanguage(language);
        }
        
        if (notificationChannel != null) {
            parent.setPreferredNotification(notificationChannel);
        }
        
        parentRepository.save(parent);
        
        auditLogger.log(
            "PARENT_PREFERENCES_UPDATED",
            parentId,
            String.format("Preferences updated: language=%s, channel=%s", language, notificationChannel),
            null,
            parent.getSchoolId()
        );
    }
    
    /**
     * Marks parent phone as verified.
     * 
     * <p><b>Use case:</b> After SMS/WhatsApp OTP verification.
     * 
     * @param parentId parent ID
     */
    public void verifyPhone(String parentId) {
        Parent parent = findParentById(parentId);
        parent.setPhoneVerified(true);
        parentRepository.save(parent);
        
        auditLogger.log(
            "PARENT_PHONE_VERIFIED",
            parentId,
            "Phone number verified",
            null,
            parent.getSchoolId()
        );
    }
    
    /**
     * Deactivates a parent.
     * 
     * @param parentId parent ID
     * @param reason deactivation reason
     */
    public void deactivateParent(String parentId, String reason) {
        Parent parent = findParentById(parentId);
        parent.deactivate();
        parentRepository.save(parent);
        
        auditLogger.log(
            "PARENT_DEACTIVATED",
            parentId,
            "Parent deactivated: " + reason,
            null,
            parent.getSchoolId()
        );
    }
    
    /**
     * Reactivates a parent.
     * 
     * @param parentId parent ID
     */
    public void reactivateParent(String parentId) {
        Parent parent = findParentById(parentId);
        parent.activate();
        parentRepository.save(parent);
        
        auditLogger.log(
            "PARENT_REACTIVATED",
            parentId,
            "Parent reactivated",
            null,
            parent.getSchoolId()
        );
    }
    
    /**
     * Gets parent by ID.
     * 
     * @param parentId parent ID
     * @return parent
     */
    public Parent getParent(String parentId) {
        return findParentById(parentId);
    }
    
    /**
     * Gets parent by phone number.
     * 
     * @param schoolId school ID
     * @param phone phone number
     * @return parent
     */
    public Parent getParentByPhone(String schoolId, String phone) {
        return parentRepository.findBySchoolIdAndPhone(schoolId, normalizePhone(phone))
            .orElseThrow(() -> new ResourceNotFoundException("Parent", phone));
    }
    
    /**
     * Gets all parents for a school.
     * 
     * @param schoolId school ID
     * @return list of parents
     */
    public List<Parent> getSchoolParents(String schoolId) {
        return parentRepository.findBySchoolId(schoolId);
    }
    
    /**
     * Gets all active parents for a school.
     * 
     * @param schoolId school ID
     * @return list of active parents
     */
    public List<Parent> getActiveParents(String schoolId) {
        return parentRepository.findBySchoolIdAndIsActive(schoolId, true);
    }
    
    /**
     * Searches parents by name.
     * 
     * @param schoolId school ID
     * @param searchTerm search term
     * @return matching parents
     */
    public List<Parent> searchParents(String schoolId, String searchTerm) {
        return parentRepository.searchByName(schoolId, searchTerm);
    }
    
    /**
     * Bulk imports parents from CSV data.
     * 
     * <p><b>CSV Format:</b>
     * firstName,lastName,phone,email,preferredLanguage
     * 
     * @param schoolId school ID
     * @param csvData CSV data
     * @return number of parents imported
     */
    public int bulkImportParents(String schoolId, List<String[]> csvData) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("School", schoolId));
        
        int importedCount = 0;
        
        for (String[] row : csvData) {
            try {
                if (row.length < 3) continue; // Skip invalid rows
                
                String firstName = row[0].trim();
                String lastName = row[1].trim();
                String phone = row[2].trim();
                String email = row.length > 3 ? row[3].trim() : null;
                String language = row.length > 4 ? row[4].trim() : "en";
                
                // Skip if parent already exists
                if (parentRepository.existsBySchoolIdAndPhone(schoolId, normalizePhone(phone))) {
                    continue;
                }
                
                registerParent(schoolId, firstName, lastName, phone, email);
                importedCount++;
                
            } catch (Exception e) {
                // Log error but continue with next row
                auditLogger.log(
                    "PARENT_IMPORT_ERROR",
                    null,
                    "Failed to import parent: " + e.getMessage(),
                    null,
                    schoolId
                );
            }
        }
        
        auditLogger.log(
            "PARENT_BULK_IMPORT",
            null,
            String.format("Imported %d parents", importedCount),
            null,
            schoolId
        );
        
        return importedCount;
    }
    
    // ========== Helper Methods ==========
    
    private Parent findParentById(String parentId) {
        return parentRepository.findById(parentId)
            .orElseThrow(() -> new ResourceNotFoundException("Parent", parentId));
    }
    
    private String generateParentCode(String schoolId) {
        // Generate: PAR + 3-digit number (e.g., PAR001)
        int count = parentRepository.countBySchoolId(schoolId) + 1;
        return String.format("PAR%03d", count);
    }
    
    private void validateName(String name, String fieldName) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidRequestException(fieldName + " cannot be empty");
        }
        
        if (name.length() < 2) {
            throw new InvalidRequestException(fieldName + " must be at least 2 characters");
        }
    }
    
    private void validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new InvalidRequestException("Phone number cannot be empty");
        }
        
        String normalized = normalizePhone(phone);
        
        // SA phone format: +27XXXXXXXXX or 0XXXXXXXXX
        if (!normalized.matches("^(\\+27|0)[0-9]{9}$")) {
            throw new InvalidRequestException(
                "Invalid SA phone format. Expected: +27XXXXXXXXX or 0XXXXXXXXX"
            );
        }
    }
    
    private String normalizePhone(String phone) {
        // Normalize to +27XXXXXXXXX format
        String cleaned = phone.replaceAll("[\\s-()]", "");
        
        if (cleaned.startsWith("0")) {
            return "+27" + cleaned.substring(1);
        }
        
        if (!cleaned.startsWith("+")) {
            return "+" + cleaned;
        }
        
        return cleaned;
    }
}