package com.calpay.repository.impl;

import com.calpay.core.model.FeeStructure;
import com.calpay.repository.FeeStructureRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of FeeStructureRepository for testing/demo.
 * 
 * <p><b>Thread Safety:</b> Uses ConcurrentHashMap for thread-safe operations.
 * 
 * <p><b>Indexing Strategy:</b>
 * <ul>
 *   <li>Primary: feeId → FeeStructure</li>
 *   <li>Composite: schoolId → List of fee structures</li>
 *   <li>Filter indices for active, mandatory, and grade-specific fees</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public class InMemoryFeeStructureRepository implements FeeStructureRepository {
    
    private final Map<String, FeeStructure> feeStructures = new ConcurrentHashMap<>();
    
    @Override
    public FeeStructure save(FeeStructure feeStructure) {
        feeStructures.put(feeStructure.getFeeId(), feeStructure);
        return feeStructure;
    }
    
    @Override
    public Optional<FeeStructure> findById(String feeId) {
        return Optional.ofNullable(feeStructures.get(feeId));
    }
    
    @Override
    public List<FeeStructure> findBySchoolId(String schoolId) {
        return feeStructures.values().stream()
            .filter(f -> f.getSchoolId().equals(schoolId))
            .sorted(Comparator.comparing(FeeStructure::getFeeName))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<FeeStructure> findActiveBySchoolId(String schoolId) {
        return feeStructures.values().stream()
            .filter(f -> f.getSchoolId().equals(schoolId) && f.isActive())
            .sorted(Comparator.comparing(FeeStructure::getFeeName))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<FeeStructure> findBySchoolIdAndGradeLevel(String schoolId, String gradeLevel) {
        return feeStructures.values().stream()
            .filter(f -> f.getSchoolId().equals(schoolId))
            .filter(f -> f.appliesTo(gradeLevel)) // Uses FeeStructure's appliesTo() method
            .sorted(Comparator.comparing(FeeStructure::getFeeName))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<FeeStructure> findBySchoolIdAndFeeType(String schoolId, String feeType) {
        return feeStructures.values().stream()
            .filter(f -> f.getSchoolId().equals(schoolId) && f.getFeeType().equals(feeType))
            .sorted(Comparator.comparing(FeeStructure::getFeeName))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<FeeStructure> findMandatoryBySchoolId(String schoolId) {
        return feeStructures.values().stream()
            .filter(f -> f.getSchoolId().equals(schoolId) && f.isMandatory())
            .sorted(Comparator.comparing(FeeStructure::getFeeName))
            .collect(Collectors.toList());
    }
    
    @Override
    public int countBySchoolId(String schoolId) {
        return (int) feeStructures.values().stream()
            .filter(f -> f.getSchoolId().equals(schoolId))
            .count();
    }
    
    @Override
    public void deleteById(String feeId) {
        feeStructures.remove(feeId);
    }
    
    @Override
    public void deleteAll() {
        feeStructures.clear();
    }
    
    /**
     * Gets total count of all fee structures (for testing).
     * 
     * @return total fee structure count
     */
    public int size() {
        return feeStructures.size();
    }
    
    /**
     * Gets all fee structures (for testing).
     * 
     * @return all fee structures
     */
    public Collection<FeeStructure> getAll() {
        return new ArrayList<>(feeStructures.values());
    }
    
    /**
     * Finds recurring fees (monthly, quarterly, annually) for a school.
     * 
     * @param schoolId school ID
     * @return list of recurring fees
     */
    public List<FeeStructure> findRecurringBySchoolId(String schoolId) {
        return feeStructures.values().stream()
            .filter(f -> f.getSchoolId().equals(schoolId) && f.isRecurring())
            .sorted(Comparator.comparing(FeeStructure::getFeeName))
            .collect(Collectors.toList());
    }
    
    /**
     * Finds one-time fees for a school.
     * 
     * @param schoolId school ID
     * @return list of one-time fees
     */
    public List<FeeStructure> findOneTimeBySchoolId(String schoolId) {
        return feeStructures.values().stream()
            .filter(f -> f.getSchoolId().equals(schoolId) && f.isOneTime())
            .sorted(Comparator.comparing(FeeStructure::getFeeName))
            .collect(Collectors.toList());
    }
    
    /**
     * Finds fees by frequency.
     * 
     * @param schoolId school ID
     * @param frequency frequency (MONTHLY, QUARTERLY, ANNUALLY, ONE_TIME)
     * @return list of fees with that frequency
     */
    public List<FeeStructure> findBySchoolIdAndFrequency(String schoolId, String frequency) {
        return feeStructures.values().stream()
            .filter(f -> f.getSchoolId().equals(schoolId) && f.getFrequency().equals(frequency))
            .sorted(Comparator.comparing(FeeStructure::getFeeName))
            .collect(Collectors.toList());
    }
    
    /**
     * Finds fees due on a specific day of the month.
     * 
     * @param schoolId school ID
     * @param dueDayOfMonth day of month (1-31)
     * @return list of fees due on that day
     */
    public List<FeeStructure> findBySchoolIdAndDueDay(String schoolId, int dueDayOfMonth) {
        return feeStructures.values().stream()
            .filter(f -> f.getSchoolId().equals(schoolId))
            .filter(f -> f.getDueDayOfMonth() != null && f.getDueDayOfMonth() == dueDayOfMonth)
            .sorted(Comparator.comparing(FeeStructure::getFeeName))
            .collect(Collectors.toList());
    }
}