package com.calpay.repository;

import com.calpay.core.model.Parent;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Parent data access operations.
 * 
 * <p><b>Indexing Strategy:</b>
 * <ul>
 *   <li>parentCode (unique per school)</li>
 *   <li>schoolId + phone (unique, for lookup)</li>
 *   <li>schoolId + isActive (for filtering)</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public interface ParentRepository {
    
    /**
     * Saves a parent (insert or update).
     * 
     * @param parent parent to save
     * @return saved parent
     */
    Parent save(Parent parent);
    
    /**
     * Finds a parent by ID.
     * 
     * @param parentId parent ID
     * @return optional parent
     */
    Optional<Parent> findById(String parentId);
    
    /**
     * Finds a parent by school and code.
     * 
     * @param schoolId school ID
     * @param parentCode parent code (e.g., PAR001)
     * @return optional parent
     */
    Optional<Parent> findBySchoolIdAndParentCode(String schoolId, String parentCode);
    
    /**
     * Finds a parent by school and phone number.
     * 
     * @param schoolId school ID
     * @param phone phone number
     * @return optional parent
     */
    Optional<Parent> findBySchoolIdAndPhone(String schoolId, String phone);
    
    /**
     * Finds all parents for a school.
     * 
     * @param schoolId school ID
     * @return list of parents
     */
    List<Parent> findBySchoolId(String schoolId);
    
    /**
     * Finds parents by school and active status.
     * 
     * @param schoolId school ID
     * @param isActive active status
     * @return list of parents
     */
    List<Parent> findBySchoolIdAndIsActive(String schoolId, boolean isActive);
    
    /**
     * Searches parents by name (first or last name contains search term).
     * 
     * @param schoolId school ID
     * @param searchTerm search term
     * @return matching parents
     */
    List<Parent> searchByName(String schoolId, String searchTerm);
    
    /**
     * Checks if a parent exists by school and phone.
     * 
     * @param schoolId school ID
     * @param phone phone number
     * @return true if exists
     */
    boolean existsBySchoolIdAndPhone(String schoolId, String phone);
    
    /**
     * Counts parents for a school.
     * 
     * @param schoolId school ID
     * @return parent count
     */
    int countBySchoolId(String schoolId);
    
    /**
     * Deletes a parent by ID.
     * 
     * @param parentId parent ID
     */
    void deleteById(String parentId);
    
    /**
     * Deletes all parents (testing only).
     */
    void deleteAll();
}
