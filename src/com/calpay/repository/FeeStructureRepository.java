package com.calpay.repository;

import com.calpay.core.model.FeeStructure;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for FeeStructure data access operations.
 * 
 * <p><b>Indexing Strategy:</b>
 * <ul>
 *   <li>schoolId + isActive (for active fee queries)</li>
 *   <li>schoolId + gradeLevel (for grade-specific fees)</li>
 *   <li>schoolId + feeType (for fee type filtering)</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public interface FeeStructureRepository {
    
    /**
     * Saves a fee structure (insert or update).
     * 
     * @param feeStructure fee structure to save
     * @return saved fee structure
     */
    FeeStructure save(FeeStructure feeStructure);
    
    /**
     * Finds a fee structure by ID.
     * 
     * @param feeId fee structure ID
     * @return optional fee structure
     */
    Optional<FeeStructure> findById(String feeId);
    
    /**
     * Finds all fee structures for a school.
     * 
     * @param schoolId school ID
     * @return list of fee structures
     */
    List<FeeStructure> findBySchoolId(String schoolId);
    
    /**
     * Finds active fee structures for a school.
     * 
     * @param schoolId school ID
     * @return list of active fee structures
     */
    List<FeeStructure> findActiveBySchoolId(String schoolId);
    
    /**
     * Finds fee structures by school and grade level.
     * 
     * @param schoolId school ID
     * @param gradeLevel grade level
     * @return list of fee structures
     */
    List<FeeStructure> findBySchoolIdAndGradeLevel(String schoolId, String gradeLevel);
    
    /**
     * Finds fee structures by school and fee type.
     * 
     * @param schoolId school ID
     * @param feeType fee type
     * @return list of fee structures
     */
    List<FeeStructure> findBySchoolIdAndFeeType(String schoolId, String feeType);
    
    /**
     * Finds mandatory fees for a school.
     * 
     * @param schoolId school ID
     * @return list of mandatory fee structures
     */
    List<FeeStructure> findMandatoryBySchoolId(String schoolId);
    
    /**
     * Counts fee structures for a school.
     * 
     * @param schoolId school ID
     * @return fee structure count
     */
    int countBySchoolId(String schoolId);
    
    /**
     * Deletes a fee structure by ID.
     * 
     * @param feeId fee structure ID
     */
    void deleteById(String feeId);
    
    /**
     * Deletes all fee structures (testing only).
     */
    void deleteAll();
}