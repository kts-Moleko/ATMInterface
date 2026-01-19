package com.calpay.repository.impl;

import com.calpay.core.model.School;
import com.calpay.repository.SchoolRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of SchoolRepository for testing/demo.
 * 
 * <p><b>Thread Safety:</b> Uses ConcurrentHashMap for thread-safe operations.
 * 
 * <p><b>Indexing Strategy:</b>
 * <ul>
 *   <li>Primary: schoolId → School</li>
 *   <li>Unique: schoolCode → schoolId</li>
 *   <li>Unique: email → schoolId</li>
 * </ul>
 * 
 * <p><b>NOTE:</b> This class is PUBLIC to be accessible from CLI and other packages.
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public class InMemorySchoolRepository implements SchoolRepository {
    
    private final Map<String, School> schools = new ConcurrentHashMap<>();
    private final Map<String, String> codeIndex = new ConcurrentHashMap<>();  // schoolCode -> schoolId
    private final Map<String, String> emailIndex = new ConcurrentHashMap<>(); // email -> schoolId
    
    @Override
    public School save(School school) {
        schools.put(school.getSchoolId(), school);
        
        // Update indexes
        codeIndex.put(school.getSchoolCode(), school.getSchoolId());
        emailIndex.put(school.getEmail(), school.getSchoolId());
        
        return school;
    }
    
    @Override
    public Optional<School> findById(String schoolId) {
        return Optional.ofNullable(schools.get(schoolId));
    }
    
    @Override
    public Optional<School> findBySchoolCode(String schoolCode) {
        String schoolId = codeIndex.get(schoolCode);
        return schoolId != null ? Optional.ofNullable(schools.get(schoolId)) : Optional.empty();
    }
    
    @Override
    public Optional<School> findByEmail(String email) {
        String schoolId = emailIndex.get(email);
        return schoolId != null ? Optional.ofNullable(schools.get(schoolId)) : Optional.empty();
    }
    
    @Override
    public List<School> findAll() {
        return new ArrayList<>(schools.values());
    }
    
    @Override
    public List<School> findByIsActive(boolean isActive) {
        return schools.values().stream()
            .filter(s -> s.isActive() == isActive)
            .sorted(Comparator.comparing(School::getName))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<School> findBySubscriptionStatus(String subscriptionStatus) {
        return schools.values().stream()
            .filter(s -> subscriptionStatus.equals(s.getSubscriptionStatus()))
            .sorted(Comparator.comparing(School::getName))
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean existsByEmail(String email) {
        return emailIndex.containsKey(email);
    }
    
    @Override
    public int count() {
        return schools.size();
    }
    
    @Override
    public void deleteById(String schoolId) {
        School school = schools.remove(schoolId);
        if (school != null) {
            // Clean up indexes
            codeIndex.remove(school.getSchoolCode());
            emailIndex.remove(school.getEmail());
        }
    }
    
    @Override
    public void deleteAll() {
        schools.clear();
        codeIndex.clear();
        emailIndex.clear();
    }
    
    /**
     * Gets total count of all schools (for testing).
     * 
     * @return total school count
     */
    public int size() {
        return schools.size();
    }
    
    /**
     * Gets all schools (for testing).
     * 
     * @return all schools
     */
    public Collection<School> getAll() {
        return new ArrayList<>(schools.values());
    }
    
    /**
     * Finds trial schools.
     * 
     * @return list of schools with trial subscription
     */
    public List<School> findTrialSchools() {
        return findBySubscriptionStatus("TRIAL");
    }
    
    /**
     * Finds active subscriptions.
     * 
     * @return list of schools with active subscriptions
     */
    public List<School> findActiveSubscriptions() {
        return schools.values().stream()
            .filter(School::isSubscriptionActive)
            .sorted(Comparator.comparing(School::getName))
            .collect(Collectors.toList());
    }
    
    /**
     * Finds expired trial schools.
     * 
     * @return list of schools with expired trials
     */
    public List<School> findExpiredTrials() {
        return schools.values().stream()
            .filter(School::isTrialExpired)
            .sorted(Comparator.comparing(School::getName))
            .collect(Collectors.toList());
    }
}