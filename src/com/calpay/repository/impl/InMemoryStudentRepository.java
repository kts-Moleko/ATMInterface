package com.calpay.repository.impl;

import com.calpay.core.model.Student;
import com.calpay.repository.StudentRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of StudentRepository for testing/demo.
 * 
 * <p><b>Thread Safety:</b> Uses ConcurrentHashMap for thread-safe operations.
 * 
 * <p><b>Indexing Strategy:</b>
 * <ul>
 *   <li>Primary: studentId → Student</li>
 *   <li>Unique: (schoolId, studentCode) → studentId</li>
 *   <li>Composite: schoolId → List of students (for filtering)</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public class InMemoryStudentRepository implements StudentRepository {
    
    private final Map<String, Student> students = new ConcurrentHashMap<>();
    private final Map<String, String> codeIndex = new ConcurrentHashMap<>(); // "schoolId:code" -> studentId
    
    @Override
    public Student save(Student student) {
        students.put(student.getStudentId(), student);
        
        // Update code index
        String codeKey = student.getSchoolId() + ":" + student.getStudentCode();
        codeIndex.put(codeKey, student.getStudentId());
        
        return student;
    }
    
    @Override
    public Optional<Student> findById(String studentId) {
        return Optional.ofNullable(students.get(studentId));
    }
    
    @Override
    public Optional<Student> findBySchoolIdAndStudentCode(String schoolId, String studentCode) {
        String indexKey = schoolId + ":" + studentCode;
        String studentId = codeIndex.get(indexKey);
        return studentId != null ? Optional.ofNullable(students.get(studentId)) : Optional.empty();
    }
    
    @Override
    public List<Student> findBySchoolId(String schoolId) {
        return students.values().stream()
            .filter(s -> s.getSchoolId().equals(schoolId))
            .sorted(Comparator.comparing(Student::getStudentCode))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Student> findBySchoolIdAndIsActive(String schoolId, boolean isActive) {
        return students.values().stream()
            .filter(s -> s.getSchoolId().equals(schoolId) && s.isActive() == isActive)
            .sorted(Comparator.comparing(Student::getStudentCode))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Student> findBySchoolIdAndGradeLevel(String schoolId, String gradeLevel) {
        return students.values().stream()
            .filter(s -> s.getSchoolId().equals(schoolId) && s.getGradeLevel().equals(gradeLevel))
            .sorted(Comparator.comparing(Student::getStudentCode))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Student> findBySchoolIdAndHasScholarship(String schoolId, boolean hasScholarship) {
        return students.values().stream()
            .filter(s -> s.getSchoolId().equals(schoolId) && s.hasScholarship() == hasScholarship)
            .sorted(Comparator.comparing(Student::getStudentCode))
            .collect(Collectors.toList());
    }
    
    @Override
    public List<Student> searchByName(String schoolId, String searchTerm) {
        String lowerSearch = searchTerm.toLowerCase().trim();
        return students.values().stream()
            .filter(s -> s.getSchoolId().equals(schoolId))
            .filter(s -> s.getFirstName().toLowerCase().contains(lowerSearch) ||
                        s.getLastName().toLowerCase().contains(lowerSearch) ||
                        s.getFullName().toLowerCase().contains(lowerSearch) ||
                        s.getStudentCode().toLowerCase().contains(lowerSearch))
            .sorted(Comparator.comparing(Student::getStudentCode))
            .collect(Collectors.toList());
    }
    
    @Override
    public int countBySchoolId(String schoolId) {
        return (int) students.values().stream()
            .filter(s -> s.getSchoolId().equals(schoolId))
            .count();
    }
    
    @Override
    public int countBySchoolIdAndIsActive(String schoolId, boolean isActive) {
        return (int) students.values().stream()
            .filter(s -> s.getSchoolId().equals(schoolId) && s.isActive() == isActive)
            .count();
    }
    
    @Override
    public void deleteById(String studentId) {
        Student student = students.remove(studentId);
        if (student != null) {
            // Clean up index
            codeIndex.remove(student.getSchoolId() + ":" + student.getStudentCode());
        }
    }
    
    @Override
    public void deleteAll() {
        students.clear();
        codeIndex.clear();
    }
    
    /**
     * Gets total count of all students (for testing).
     * 
     * @return total student count
     */
    public int size() {
        return students.size();
    }
    
    /**
     * Gets all students (for testing).
     * 
     * @return all students
     */
    public Collection<Student> getAll() {
        return new ArrayList<>(students.values());
    }
    
    /**
     * Gets students by grade level across all schools (for testing).
     * 
     * @param gradeLevel grade level
     * @return students in that grade
     */
    public List<Student> findByGradeLevel(String gradeLevel) {
        return students.values().stream()
            .filter(s -> s.getGradeLevel().equals(gradeLevel))
            .collect(Collectors.toList());
    }
}