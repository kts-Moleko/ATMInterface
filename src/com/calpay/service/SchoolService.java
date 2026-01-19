package com.calpay.service;

import com.calpay.audit.AuditLogger;
import com.calpay.core.enums.AccountStatus;
import com.calpay.core.exceptions.InvalidRequestException;
import com.calpay.core.exceptions.ResourceNotFoundException;
import com.calpay.core.model.School;
import com.calpay.core.model.SchoolAccount;
import com.calpay.repository.SchoolAccountRepository;
import com.calpay.repository.SchoolRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Service for managing schools in the CalPay platform.
 * 
 * <p><b>Key Responsibilities:</b>
 * <ul>
 *   <li>School registration and onboarding</li>
 *   <li>Subscription management (TRIAL → ACTIVE)</li>
 *   <li>School account creation</li>
 *   <li>School profile updates</li>
 *   <li>School deactivation/suspension</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public class SchoolService {
    
    private final SchoolRepository schoolRepository;
    private final SchoolAccountRepository schoolAccountRepository;
    private final AuditLogger auditLogger;
    
    // Business rules
    private static final int FREE_TIER_MAX_STUDENTS = 5;
    private static final int TRIAL_DURATION_DAYS = 30;
    
    public SchoolService(
            SchoolRepository schoolRepository,
            SchoolAccountRepository schoolAccountRepository,
            AuditLogger auditLogger) {
        this.schoolRepository = Objects.requireNonNull(schoolRepository);
        this.schoolAccountRepository = Objects.requireNonNull(schoolAccountRepository);
        this.auditLogger = Objects.requireNonNull(auditLogger);
    }
    
    /**
     * Registers a new school with trial subscription.
     * 
     * <p><b>Process:</b>
     * <ol>
     *   <li>Validate school data</li>
     *   <li>Generate unique school code (e.g., SCH001)</li>
     *   <li>Create school entity</li>
     *   <li>Create school account</li>
     *   <li>Set 30-day trial period</li>
     *   <li>Audit log</li>
     * </ol>
     * 
     * @param name school name
     * @param email school email
     * @param phone school phone
     * @return created school
     */
    public School registerSchool(String name, String email, String phone) {
        // Validation
        validateSchoolName(name);
        validateEmail(email);
        
        // Check if email already exists
        if (schoolRepository.existsByEmail(email)) {
            throw new InvalidRequestException(
                "School with this email already exists: " + email
            );
        }
        
        // Generate unique school code
        String schoolCode = generateSchoolCode();
        String schoolId = UUID.randomUUID().toString();
        
        // Create school
        School school = new School.Builder()
            .schoolId(schoolId)
            .schoolCode(schoolCode)
            .name(name)
            .email(email)
            .phone(phone)
            .subscriptionStatus("TRIAL")
            .subscriptionTier("TIER_1")
            .subscriptionStartsAt(LocalDateTime.now())
            .subscriptionExpiresAt(LocalDateTime.now().plusDays(TRIAL_DURATION_DAYS))
            .build();
        
        // Create school account
        SchoolAccount account = new SchoolAccount.Builder()
            .accountId(UUID.randomUUID().toString())
            .schoolId(schoolId)
            .accountNumber(generateAccountNumber())
            .subscriptionStatus(SchoolAccount.SubscriptionStatus.TRIAL)
            .subscriptionExpiresAt(LocalDateTime.now().plusDays(TRIAL_DURATION_DAYS))
            .status(AccountStatus.ACTIVE)
            .build();
        
        // Save
        School savedSchool = schoolRepository.save(school);
        schoolAccountRepository.save(account);
        
        // Audit
        auditLogger.log(
            "SCHOOL_REGISTERED",
            schoolId,
            String.format("School registered: %s (Code: %s)", name, schoolCode),
            null,
            schoolId
        );
        
        return savedSchool;
    }
    
    /**
     * Activates school subscription (after trial or payment).
     * 
     * @param schoolId school ID
     * @param tier subscription tier (TIER_1, TIER_2, TIER_3)
     * @param durationMonths subscription duration in months
     */
    public void activateSubscription(String schoolId, String tier, int durationMonths) {
        School school = findSchoolById(schoolId);
        SchoolAccount account = schoolAccountRepository.findBySchoolId(schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("SchoolAccount", schoolId));
        
        // Calculate expiration date
        LocalDateTime expiresAt = LocalDateTime.now().plusMonths(durationMonths);
        
        // Activate school (this sets status to ACTIVE and subscriptionStatus to "ACTIVE")
        school.activate();
        
        // Update account
        account.setSubscriptionStatus(SchoolAccount.SubscriptionStatus.ACTIVE);
        account.setSubscriptionTier(tier);
        account.setSubscriptionExpiresAt(expiresAt);
        account.activate();
        
        // Save
        schoolRepository.save(school);
        schoolAccountRepository.save(account);
        
        // Audit
        auditLogger.log(
            "SUBSCRIPTION_ACTIVATED",
            schoolId,
            String.format("Subscription activated: %s for %d months", tier, durationMonths),
            null,
            schoolId
        );
    }
    
    /**
     * Suspends a school (e.g., non-payment, policy violation).
     * 
     * @param schoolId school ID
     * @param reason suspension reason
     */
    public void suspendSchool(String schoolId, String reason) {
        School school = findSchoolById(schoolId);
        SchoolAccount account = schoolAccountRepository.findBySchoolId(schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("SchoolAccount", schoolId));
        
        school.suspend(reason);
        account.suspend(reason);
        
        schoolRepository.save(school);
        schoolAccountRepository.save(account);
        
        auditLogger.log(
            "SCHOOL_SUSPENDED",
            schoolId,
            String.format("School suspended: %s", reason),
            null,
            schoolId
        );
    }
    
    /**
     * Updates school student count.
     * 
     * @param schoolId school ID
     * @param count new student count
     */
    public void updateStudentCount(String schoolId, int count) {
        School school = findSchoolById(schoolId);
        SchoolAccount account = schoolAccountRepository.findBySchoolId(schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("SchoolAccount", schoolId));
        
        school.updateStudentCount(count);
        account.updateStudentCount(count);
        
        schoolRepository.save(school);
        schoolAccountRepository.save(account);
    }
    
    /**
     * Checks if school can add more students based on subscription tier.
     * 
     * @param schoolId school ID
     * @return true if can add students
     */
    public boolean canAddStudent(String schoolId) {
        School school = findSchoolById(schoolId);
        SchoolAccount account = schoolAccountRepository.findBySchoolId(schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("SchoolAccount", schoolId));
        
        // Free tier: max 5 students
        if (SchoolAccount.SubscriptionStatus.FREE.equals(account.getSubscriptionStatus())) {
            return school.getTotalStudents() < FREE_TIER_MAX_STUDENTS;
        }
        
        // Trial/Active: check if subscription is active
        return account.isSubscriptionActive();
    }
    
    /**
     * Gets school by ID.
     * 
     * @param schoolId school ID
     * @return school
     */
    public School getSchool(String schoolId) {
        return findSchoolById(schoolId);
    }
    
    /**
     * Gets school by code.
     * 
     * @param schoolCode school code (e.g., SCH001)
     * @return school
     */
    public School getSchoolByCode(String schoolCode) {
        return schoolRepository.findBySchoolCode(schoolCode)
            .orElseThrow(() -> new ResourceNotFoundException("School", schoolCode));
    }
    
    /**
     * Gets all active schools.
     * 
     * @return list of active schools
     */
    public List<School> getAllActiveSchools() {
        return schoolRepository.findByIsActive(true);
    }
    
    /**
     * Updates school basic info.
     * 
     * @param schoolId school ID
     * @param name new name (optional)
     * @param email new email (optional)
     * @param phone new phone (optional)
     */
    public void updateSchoolInfo(String schoolId, String name, String email, String phone) {
        School school = findSchoolById(schoolId);
        
        boolean updated = false;
        
        if (name != null && !name.trim().isEmpty()) {
            validateSchoolName(name);
            school = updateSchoolName(school, name);
            updated = true;
        }
        
        if (email != null && !email.trim().isEmpty()) {
            validateEmail(email);
            school = updateSchoolEmail(school, email);
            updated = true;
        }
        
        if (phone != null && !phone.trim().isEmpty()) {
            school = updateSchoolPhone(school, phone);
            updated = true;
        }
        
        if (updated) {
            schoolRepository.save(school);
            
            auditLogger.log(
                "SCHOOL_UPDATED",
                schoolId,
                "School information updated",
                null,
                schoolId
            );
        }
    }
    
    // ========== Helper Methods ==========
    
    private School findSchoolById(String schoolId) {
        return schoolRepository.findById(schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("School", schoolId));
    }
    
    private String generateSchoolCode() {
        // Generate: SCH + 3-digit number (e.g., SCH001)
        int count = schoolRepository.count() + 1;
        return String.format("SCH%03d", count);
    }
    
    private String generateAccountNumber() {
        // Generate: 10-digit account number
        return String.format("%010d", System.currentTimeMillis() % 10000000000L);
    }
    
    private void validateSchoolName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidRequestException("School name cannot be empty");
        }
        
        if (name.length() < 3) {
            throw new InvalidRequestException("School name must be at least 3 characters");
        }
    }
    
    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidRequestException("Email cannot be empty");
        }
        
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new InvalidRequestException("Invalid email format: " + email);
        }
    }
    
    /**
     * Creates a new School instance with updated name.
     * Since School properties are mutable, we update the existing object.
     * 
     * @param school existing school
     * @param name new name
     * @return updated school (same instance)
     */
    private School updateSchoolName(School school, String name) {
        // Note: Based on the School class, properties appear to be mutable via setters
        // If School has a setName() method, use it. Otherwise, we'd need to rebuild.
        // Assuming setters exist based on the service logic:
        try {
            java.lang.reflect.Method setName = school.getClass().getMethod("setName", String.class);
            setName.invoke(school, name);
        } catch (Exception e) {
            // If setters don't exist, we cannot update
            throw new UnsupportedOperationException("School name cannot be updated - immutable field");
        }
        return school;
    }
    
    /**
     * Updates school email.
     * 
     * @param school existing school
     * @param email new email
     * @return updated school
     */
    private School updateSchoolEmail(School school, String email) {
        try {
            java.lang.reflect.Method setEmail = school.getClass().getMethod("setEmail", String.class);
            setEmail.invoke(school, email);
        } catch (Exception e) {
            throw new UnsupportedOperationException("School email cannot be updated - immutable field");
        }
        return school;
    }
    
    /**
     * Updates school phone.
     * 
     * @param school existing school
     * @param phone new phone
     * @return updated school
     */
    private School updateSchoolPhone(School school, String phone) {
        try {
            java.lang.reflect.Method setPhone = school.getClass().getMethod("setPhone", String.class);
            setPhone.invoke(school, phone);
        } catch (Exception e) {
            throw new UnsupportedOperationException("School phone cannot be updated - immutable field");
        }
        return school;
    }
}