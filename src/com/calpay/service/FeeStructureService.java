package com.calpay.service;

import com.calpay.audit.AuditLogger;
import com.calpay.core.exceptions.InvalidRequestException;
import com.calpay.core.exceptions.ResourceNotFoundException;
import com.calpay.core.model.FeeStructure;
import com.calpay.core.model.Money;
import com.calpay.core.model.School;
import com.calpay.repository.FeeStructureRepository;
import com.calpay.repository.SchoolRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Service for managing fee structures.
 * 
 * <p><b>Key Responsibilities:</b>
 * <ul>
 *   <li>Create and update fee structures</li>
 *   <li>Manage fee schedules (monthly, quarterly, annual)</li>
 *   <li>Handle grade-specific fees</li>
 *   <li>Activate/deactivate fees</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public class FeeStructureService {
    
    private final FeeStructureRepository feeStructureRepository;
    private final SchoolRepository schoolRepository;
    private final AuditLogger auditLogger;
    
    public FeeStructureService(
            FeeStructureRepository feeStructureRepository,
            SchoolRepository schoolRepository,
            AuditLogger auditLogger) {
        this.feeStructureRepository = Objects.requireNonNull(feeStructureRepository);
        this.schoolRepository = Objects.requireNonNull(schoolRepository);
        this.auditLogger = Objects.requireNonNull(auditLogger);
    }
    
    /**
     * Creates a new fee structure.
     * 
     * @param schoolId school ID
     * @param feeType fee type (TUITION, SPORT, etc.)
     * @param feeName fee name
     * @param amount fee amount
     * @param frequency payment frequency (MONTHLY, QUARTERLY, etc.)
     * @param dueDayOfMonth due day (1-31, for monthly fees)
     * @param gradeLevel grade level (or null for all grades)
     * @return created fee structure
     * @throws ResourceNotFoundException if school not found
     * @throws InvalidRequestException if validation fails
     */
    public FeeStructure createFeeStructure(
            String schoolId,
            String feeType,
            String feeName,
            Money amount,
            String frequency,
            Integer dueDayOfMonth,
            String gradeLevel) {
        
        // Validate inputs
        Objects.requireNonNull(schoolId, "School ID cannot be null");
        Objects.requireNonNull(feeType, "Fee type cannot be null");
        Objects.requireNonNull(feeName, "Fee name cannot be null");
        Objects.requireNonNull(amount, "Amount cannot be null");
        Objects.requireNonNull(frequency, "Frequency cannot be null");
        
        if (feeName.trim().isEmpty()) {
            throw new InvalidRequestException("Fee name cannot be empty");
        }
        
        if (amount.isNegativeOrZero()) {
            throw new InvalidRequestException("Fee amount must be positive");
        }
        
        // Validate due day for monthly frequencies
        if ("MONTHLY".equals(frequency) && dueDayOfMonth != null) {
            if (dueDayOfMonth < 1 || dueDayOfMonth > 31) {
                throw new InvalidRequestException("Due day must be between 1 and 31");
            }
        }
        
        // Verify school exists
        School school = schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School", schoolId));
        
        // Create fee structure
        String feeId = UUID.randomUUID().toString();
        FeeStructure feeStructure = new FeeStructure.Builder()
                .feeId(feeId)
                .schoolId(schoolId)
                .feeType(feeType)
                .feeName(feeName.trim())
                .amount(amount)
                .frequency(frequency)
                .dueDayOfMonth(dueDayOfMonth)
                .gradeLevel(gradeLevel != null ? gradeLevel.trim() : null)
                .isMandatory(true)
                .isActive(true)
                .build();
        
        // Save to repository
        FeeStructure saved = feeStructureRepository.save(feeStructure);
        
        // Log audit event
        auditLogger.log(
            "FEE_STRUCTURE_CREATED",
            feeId,
            String.format("Fee created: %s - %s (%s)", feeName, amount, frequency),
            null,
            schoolId
        );
        
        return saved;
    }
    
    /**
     * Updates an existing fee structure.
     * 
     * @param feeStructureId fee structure ID
     * @param feeName updated fee name (optional)
     * @param amount updated amount (optional)
     * @param dueDayOfMonth updated due day (optional)
     * @return updated fee structure
     * @throws ResourceNotFoundException if fee structure not found
     * @throws InvalidRequestException if validation fails
     */
    public FeeStructure updateFeeStructure(
            String feeStructureId,
            String feeName,
            Money amount,
            Integer dueDayOfMonth) {
        
        Objects.requireNonNull(feeStructureId, "Fee structure ID cannot be null");
        
        // Find existing fee structure
        FeeStructure existing = feeStructureRepository.findById(feeStructureId)
                .orElseThrow(() -> new ResourceNotFoundException("FeeStructure", feeStructureId));
        
        // Validate and update fields
        if (feeName != null) {
            if (feeName.trim().isEmpty()) {
                throw new InvalidRequestException("Fee name cannot be empty");
            }
            existing.setFeeName(feeName.trim());
        }
        
        if (amount != null) {
            if (amount.isNegativeOrZero()) {
                throw new InvalidRequestException("Fee amount must be positive");
            }
            existing.setAmount(amount);
        }
        
        if (dueDayOfMonth != null) {
            if ("MONTHLY".equals(existing.getFrequency())) {
                if (dueDayOfMonth < 1 || dueDayOfMonth > 31) {
                    throw new InvalidRequestException("Due day must be between 1 and 31");
                }
            }
            existing.setDueDayOfMonth(dueDayOfMonth);
        }
        
        existing.markUpdated(null);
        
        // Save updates
        FeeStructure updated = feeStructureRepository.save(existing);
        
        // Log audit event
        auditLogger.log(
            "FEE_STRUCTURE_UPDATED",
            feeStructureId,
            "Fee structure updated",
            null,
            existing.getSchoolId()
        );
        
        return updated;
    }
    
    /**
     * Activates a fee structure.
     * 
     * @param feeStructureId fee structure ID
     * @return activated fee structure
     * @throws ResourceNotFoundException if fee structure not found
     */
    public FeeStructure activateFeeStructure(String feeStructureId) {
        Objects.requireNonNull(feeStructureId, "Fee structure ID cannot be null");
        
        FeeStructure feeStructure = feeStructureRepository.findById(feeStructureId)
                .orElseThrow(() -> new ResourceNotFoundException("FeeStructure", feeStructureId));
        
        if (feeStructure.isActive()) {
            return feeStructure; // Already active
        }
        
        feeStructure.activate();
        FeeStructure saved = feeStructureRepository.save(feeStructure);
        
        auditLogger.log(
            "FEE_STRUCTURE_ACTIVATED",
            feeStructureId,
            "Fee structure activated",
            null,
            feeStructure.getSchoolId()
        );
        
        return saved;
    }
    
    /**
     * Deactivates a fee structure.
     * 
     * @param feeStructureId fee structure ID
     * @return deactivated fee structure
     * @throws ResourceNotFoundException if fee structure not found
     */
    public FeeStructure deactivateFeeStructure(String feeStructureId) {
        Objects.requireNonNull(feeStructureId, "Fee structure ID cannot be null");
        
        FeeStructure feeStructure = feeStructureRepository.findById(feeStructureId)
                .orElseThrow(() -> new ResourceNotFoundException("FeeStructure", feeStructureId));
        
        if (!feeStructure.isActive()) {
            return feeStructure; // Already inactive
        }
        
        feeStructure.deactivate();
        FeeStructure saved = feeStructureRepository.save(feeStructure);
        
        auditLogger.log(
            "FEE_STRUCTURE_DEACTIVATED",
            feeStructureId,
            "Fee structure deactivated",
            null,
            feeStructure.getSchoolId()
        );
        
        return saved;
    }
    
    /**
     * Retrieves all active fee structures for a school.
     * 
     * @param schoolId school ID
     * @return list of active fee structures
     * @throws ResourceNotFoundException if school not found
     */
    public List<FeeStructure> getActiveFeeStructures(String schoolId) {
        Objects.requireNonNull(schoolId, "School ID cannot be null");
        
        // Verify school exists
        schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School", schoolId));
        
        return feeStructureRepository.findActiveBySchoolId(schoolId);
    }
    
    /**
     * Retrieves fee structures by grade level.
     * 
     * @param schoolId school ID
     * @param gradeLevel grade level
     * @return list of fee structures for the grade
     * @throws ResourceNotFoundException if school not found
     */
    public List<FeeStructure> getFeeStructuresByGrade(String schoolId, String gradeLevel) {
        Objects.requireNonNull(schoolId, "School ID cannot be null");
        Objects.requireNonNull(gradeLevel, "Grade level cannot be null");
        
        // Verify school exists
        schoolRepository.findById(schoolId)
                .orElseThrow(() -> new ResourceNotFoundException("School", schoolId));
        
        return feeStructureRepository.findBySchoolIdAndGradeLevel(schoolId, gradeLevel);
    }
    
    /**
     * Retrieves a fee structure by ID.
     * 
     * @param feeStructureId fee structure ID
     * @return fee structure
     * @throws ResourceNotFoundException if not found
     */
    public FeeStructure getFeeStructureById(String feeStructureId) {
        Objects.requireNonNull(feeStructureId, "Fee structure ID cannot be null");
        
        return feeStructureRepository.findById(feeStructureId)
                .orElseThrow(() -> new ResourceNotFoundException("FeeStructure", feeStructureId));
    }
    
    /**
     * Deletes a fee structure (soft delete by deactivation).
     * 
     * @param feeStructureId fee structure ID
     * @throws ResourceNotFoundException if not found
     */
    public void deleteFeeStructure(String feeStructureId) {
        deactivateFeeStructure(feeStructureId);
        auditLogger.log(
            "FEE_STRUCTURE_DELETED",
            feeStructureId,
            "Fee structure deleted (soft delete)",
            null,
            null
        );
    }
}