package com.calpay.repository;

import com.calpay.core.model.School;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for School data access operations.
 * 
 * <p><b>Indexing Strategy:</b>
 * <ul>
 *   <li>schoolCode (unique)</li>
 *   <li>email (unique)</li>
 *   <li>subscriptionStatus (for filtering)</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public interface SchoolRepository {
    
    /**
     * Saves a school (insert or update).
     * 
     * @param school school to save
     * @return saved school
     */
    School save(School school);
    
    /**
     * Finds a school by ID.
     * 
     * @param schoolId school ID
     * @return optional school
     */
    Optional<School> findById(String schoolId);
    
    /**
     * Finds a school by unique code.
     * 
     * @param schoolCode school code (e.g., SCH001)
     * @return optional school
     */
    Optional<School> findBySchoolCode(String schoolCode);
    
    /**
     * Finds a school by email.
     * 
     * @param email school email
     * @return optional school
     */
    Optional<School> findByEmail(String email);
    
    /**
     * Finds all schools.
     * 
     * @return list of all schools
     */
    List<School> findAll();
    
    /**
     * Finds schools by active status.
     * 
     * @param isActive active status
     * @return list of schools
     */
    List<School> findByIsActive(boolean isActive);
    
    /**
     * Finds schools by subscription status.
     * 
     * @param subscriptionStatus subscription status
     * @return list of schools
     */
    List<School> findBySubscriptionStatus(String subscriptionStatus);
    
    /**
     * Checks if a school exists by email.
     * 
     * @param email email
     * @return true if exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Counts total schools.
     * 
     * @return school count
     */
    int count();
    
    /**
     * Deletes a school by ID.
     * 
     * @param schoolId school ID
     */
    void deleteById(String schoolId);
    
    /**
     * Deletes all schools (testing only).
     */
    void deleteAll();
}