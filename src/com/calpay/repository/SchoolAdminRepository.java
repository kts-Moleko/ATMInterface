package com.calpay.repository;

import com.calpay.core.model.SchoolAdmin;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for SchoolAdmin data access operations.
 * 
 * <p><b>Indexing Strategy:</b>
 * <ul>
 *   <li>schoolId + userId (unique)</li>
 *   <li>email (unique)</li>
 *   <li>schoolId + isActive (for filtering)</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public interface SchoolAdminRepository {
    
    /**
     * Saves a school admin (insert or update).
     * 
     * @param admin school admin to save
     * @return saved school admin
     */
    SchoolAdmin save(SchoolAdmin admin);
    
    /**
     * Finds a school admin by ID.
     * 
     * @param adminId admin ID
     * @return optional school admin
     */
    Optional<SchoolAdmin> findById(String adminId);
    
    /**
     * Finds a school admin by user ID.
     * 
     * @param userId user ID
     * @return optional school admin
     */
    Optional<SchoolAdmin> findByUserId(String userId);
    
    /**
     * Finds a school admin by email.
     * 
     * @param email email
     * @return optional school admin
     */
    Optional<SchoolAdmin> findByEmail(String email);
    
    /**
     * Finds all admins for a school.
     * 
     * @param schoolId school ID
     * @return list of school admins
     */
    List<SchoolAdmin> findBySchoolId(String schoolId);
    
    /**
     * Finds active admins for a school.
     * 
     * @param schoolId school ID
     * @param isActive active status
     * @return list of school admins
     */
    List<SchoolAdmin> findBySchoolIdAndIsActive(String schoolId, boolean isActive);
    
    /**
     * Finds admins by role.
     * 
     * @param schoolId school ID
     * @param role admin role
     * @return list of school admins
     */
    List<SchoolAdmin> findBySchoolIdAndRole(String schoolId, String role);
    
    /**
     * Checks if an admin exists by email.
     * 
     * @param email email
     * @return true if exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Counts admins for a school.
     * 
     * @param schoolId school ID
     * @return admin count
     */
    int countBySchoolId(String schoolId);
    
    /**
     * Deletes a school admin by ID.
     * 
     * @param adminId admin ID
     */
    void deleteById(String adminId);
    
    /**
     * Deletes all school admins (testing only).
     */
    void deleteAll();
}