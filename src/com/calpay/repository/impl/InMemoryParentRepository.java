package com.calpay.repository.impl;

import com.calpay.core.model.Parent;
import com.calpay.repository.ParentRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of ParentRepository for testing/demo.
 * 
 * <p><b>Thread Safety:</b> Uses ConcurrentHashMap for thread-safe operations.
 * 
 * <p><b>Indexing Strategy:</b>
 * <ul>
 *   <li>Primary: parentId → Parent</li>
 *   <li>Unique: (schoolId, phone) → parentId</li>
 *   <li>Unique: (schoolId, parentCode) → parentId</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public class InMemoryParentRepository implements ParentRepository {
    
    private final Map<String, Parent> parents = new ConcurrentHashMap<>();
    private final Map<String, String> phoneIndex = new ConcurrentHashMap<>(); // "schoolId:phone" -> parentId
    private final Map<String, String> codeIndex = new ConcurrentHashMap<>();  // "schoolId:code" -> parentId
    
    @Override
    public Parent save(Parent parent) {
        parents.put(parent.getParentId(), parent);
        
        // Update indexes
        String phoneKey = parent.getSchoolId() + ":" + parent.getPhone();
        phoneIndex.put(phoneKey, parent.getParentId());
        
        String codeKey = parent.getSchoolId() + ":" + parent.getParentCode();
        codeIndex.put(codeKey, parent.getParentId());
        
        return parent;
    }
    
    @Override
    public Optional<Parent> findById(String parentId) {
        return Optional.ofNullable(parents.get(parentId));
    }
    
    @Override
    public Optional<Parent> findBySchoolIdAndParentCode(String schoolId, String parentCode) {
        String indexKey = schoolId + ":" + parentCode;
        String parentId = codeIndex.get(indexKey);
        return parentId != null ? Optional.ofNullable(parents.get(parentId)) : Optional.empty();
    }
    
    @Override
    public Optional<Parent> findBySchoolIdAndPhone(String schoolId, String phone) {
        String indexKey = schoolId + ":" + phone;
        String parentId = phoneIndex.get(indexKey);
        return parentId != null ? Optional.ofNullable(parents.get(parentId)) : Optional.empty();
    }
    
    @Override
    public List<Parent> findBySchoolId(String schoolId) {
        return parents.values().stream()
            .filter(p -> p.getSchoolId().equals(schoolId))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Parent> findBySchoolIdAndIsActive(String schoolId, boolean isActive) {
        return parents.values().stream()
            .filter(p -> p.getSchoolId().equals(schoolId) && p.isActive() == isActive)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Parent> searchByName(String schoolId, String searchTerm) {
        String lowerSearch = searchTerm.toLowerCase().trim();
        return parents.values().stream()
            .filter(p -> p.getSchoolId().equals(schoolId))
            .filter(p -> p.getFirstName().toLowerCase().contains(lowerSearch) ||
                        p.getLastName().toLowerCase().contains(lowerSearch) ||
                        p.getFullName().toLowerCase().contains(lowerSearch))
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean existsBySchoolIdAndPhone(String schoolId, String phone) {
        String indexKey = schoolId + ":" + phone;
        return phoneIndex.containsKey(indexKey);
    }
    
    @Override
    public int countBySchoolId(String schoolId) {
        return (int) parents.values().stream()
            .filter(p -> p.getSchoolId().equals(schoolId))
            .count();
    }
    
    @Override
    public void deleteById(String parentId) {
        Parent parent = parents.remove(parentId);
        if (parent != null) {
            // Clean up indexes
            phoneIndex.remove(parent.getSchoolId() + ":" + parent.getPhone());
            codeIndex.remove(parent.getSchoolId() + ":" + parent.getParentCode());
        }
    }
    
    @Override
    public void deleteAll() {
        parents.clear();
        phoneIndex.clear();
        codeIndex.clear();
    }
    
    /**
     * Gets total count of all parents (for testing).
     * 
     * @return total parent count
     */
    public int size() {
        return parents.size();
    }
    
    /**
     * Gets all parents (for testing).
     * 
     * @return all parents
     */
    public Collection<Parent> getAll() {
        return new ArrayList<>(parents.values());
    }
}