package com.calpay.repository.impl;

import com.calpay.core.model.SchoolAdmin;
import com.calpay.repository.SchoolAdminRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of SchoolAdminRepository for testing/demo.
 * 
 * <p><b>Thread Safety:</b> Uses ConcurrentHashMap for thread-safe operations.
 * 
 * <p><b>Indexing Strategy:</b>
 * <ul>
 *   <li>Primary: adminId → SchoolAdmin</li>
 *   <li>Unique: userId → adminId</li>
 *   <li>Unique: email → adminId</li>
 *   <li>Composite: schoolId → List of admins</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public class InMemorySchoolAdminRepository implements SchoolAdminRepository {
    
    private final Map<String, SchoolAdmin> admins = new ConcurrentHashMap<>();
    private final Map<String, String> userIdIndex = new ConcurrentHashMap<>();  // userId -> adminId
    private final Map<String, String> emailIndex = new ConcurrentHashMap<>();   // email -> adminId
    
    @Override
    public SchoolAdmin save(SchoolAdmin admin) {
        admins.put(admin.getAdminId(), admin);
        
        // Update indexes
        if (admin.getUserId() != null) {
            userIdIndex.put(admin.getUserId(), admin.getAdminId());
        }
        emailIndex.put(admin.getEmail(), admin.getAdminId());
        
        return admin;
    }
    
    @Override
    public Optional<SchoolAdmin> findById(String adminId) {
        return Optional.ofNullable(admins.get(adminId));
    }
    
    @Override
    public Optional<SchoolAdmin> findByUserId(String userId) {
        String adminId = userIdIndex.get(userId);
        return adminId != null ? Optional.ofNullable(admins.get(adminId)) : Optional.empty();
    }
    
    @Override
    public Optional<SchoolAdmin> findByEmail(String email) {
        String adminId = emailIndex.get(email);
        return adminId != null ? Optional.ofNullable(admins.get(adminId)) : Optional.empty();
    }
    
    @Override
    public List<SchoolAdmin> findBySchoolId(String schoolId) {
        return admins.values().stream()
            .filter(a -> a.getSchoolId().equals(schoolId))
            .sorted(Comparator.comparing(SchoolAdmin::getLastName)
                             .thenComparing(SchoolAdmin::getFirstName))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<SchoolAdmin> findBySchoolIdAndIsActive(String schoolId, boolean isActive) {
        return admins.values().stream()
            .filter(a -> a.getSchoolId().equals(schoolId) && a.isActive() == isActive)
            .sorted(Comparator.comparing(SchoolAdmin::getLastName)
                             .thenComparing(SchoolAdmin::getFirstName))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<SchoolAdmin> findBySchoolIdAndRole(String schoolId, String role) {
        return admins.values().stream()
            .filter(a -> a.getSchoolId().equals(schoolId) && a.getRole().equals(role))
            .sorted(Comparator.comparing(SchoolAdmin::getLastName)
                             .thenComparing(SchoolAdmin::getFirstName))
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return emailIndex.containsKey(email);
    }
    
    @Override
    public int countBySchoolId(String schoolId) {
        return (int) admins.values().stream()
            .filter(a -> a.getSchoolId().equals(schoolId))
            .count();
    }
    
    @Override
    public void deleteById(String adminId) {
        SchoolAdmin admin = admins.remove(adminId);
        if (admin != null) {
            // Clean up indexes
            if (admin.getUserId() != null) {
                userIdIndex.remove(admin.getUserId());
            }
            emailIndex.remove(admin.getEmail());
        }
    }
    
    @Override
    public void deleteAll() {
        admins.clear();
        userIdIndex.clear();
        emailIndex.clear();
    }
    
    /**
     * Gets total count of all admins (for testing).
     * 
     * @return total admin count
     */
    public int size() {
        return admins.size();
    }
    
    /**
     * Gets all admins (for testing).
     * 
     * @return all school admins
     */
    public Collection<SchoolAdmin> getAll() {
        return new ArrayList<>(admins.values());
    }
    
    /**
     * Finds principals at a school.
     * 
     * @param schoolId school ID
     * @return list of principals
     */
    public List<SchoolAdmin> findPrincipalsBySchoolId(String schoolId) {
        return findBySchoolIdAndRole(schoolId, "PRINCIPAL");
    }
    
    /**
     * Finds accountants at a school.
     * 
     * @param schoolId school ID
     * @return list of accountants
     */
    public List<SchoolAdmin> findAccountantsBySchoolId(String schoolId) {
        return findBySchoolIdAndRole(schoolId, "ACCOUNTANT");
    }
    
    /**
     * Finds admins with specific permission.
     * 
     * @param schoolId school ID
     * @param permission permission to check
     * @return list of admins with that permission
     */
    public List<SchoolAdmin> findBySchoolIdAndPermission(String schoolId, String permission) {
        return admins.values().stream()
            .filter(a -> a.getSchoolId().equals(schoolId))
            .filter(a -> a.hasPermission(permission))
            .sorted(Comparator.comparing(SchoolAdmin::getLastName)
                             .thenComparing(SchoolAdmin::getFirstName))
            .collect(Collectors.toList());
    }
    
    /**
     * Counts active admins at a school.
     * 
     * @param schoolId school ID
     * @return count of active admins
     */
    public int countActiveBySchoolId(String schoolId) {
        return (int) admins.values().stream()
            .filter(a -> a.getSchoolId().equals(schoolId) && a.isActive())
            .count();
    }
}