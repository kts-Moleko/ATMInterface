package com.calpay.repository;

import com.calpay.core.model.Student;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Student data access operations.
 * 
 * <p><b>Indexing Strategy:</b>
 * <ul>
 *   <li>studentCode (unique per school)</li>
 *   <li>schoolId + gradeLevel (for grade-based queries)</li>
 *   <li>schoolId + isActive (for filtering)</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public interface StudentRepository {
    
    /**
     * Saves a student (insert or update).
     * 
     * @param student student to save
     * @return saved student
     */
    Student save(Student student);
    
    /**
     * Finds a student by ID.
     * 
     * @param studentId student ID
     * @return optional student
     */
    Optional<Student> findById(String studentId);
    
    /**
     * Finds a student by school and code.
     * 
     * @param schoolId school ID
     * @param studentCode student code (e.g., STU001)
     * @return optional student
     */
    Optional<Student> findBySchoolIdAndStudentCode(String schoolId, String studentCode);
    
    /**
     * Finds all students for a school.
     * 
     * @param schoolId school ID
     * @return list of students
     */
    List<Student> findBySchoolId(String schoolId);
    
    /**
     * Finds students by school and active status.
     * 
     * @param schoolId school ID
     * @param isActive active status
     * @return list of students
     */
    List<Student> findBySchoolIdAndIsActive(String schoolId, boolean isActive);
    
    /**
     * Finds students by school and grade level.
     * 
     * @param schoolId school ID
     * @param gradeLevel grade level
     * @return list of students
     */
    List<Student> findBySchoolIdAndGradeLevel(String schoolId, String gradeLevel);
    
    /**
     * Finds students with scholarships.
     * 
     * @param schoolId school ID
     * @param hasScholarship scholarship flag
     * @return list of students
     */
    List<Student> findBySchoolIdAndHasScholarship(String schoolId, boolean hasScholarship);
    
    /**
     * Searches students by name (first or last name contains search term).
     * 
     * @param schoolId school ID
     * @param searchTerm search term
     * @return matching students
     */
    List<Student> searchByName(String schoolId, String searchTerm);
    
    /**
     * Counts total students for a school.
     * 
     * @param schoolId school ID
     * @return student count
     */
    int countBySchoolId(String schoolId);
    
    /**
     * Counts active students for a school.
     * 
     * @param schoolId school ID
     * @param isActive active status
     * @return active student count
     */
    int countBySchoolIdAndIsActive(String schoolId, boolean isActive);
    
    /**
     * Deletes a student by ID.
     * 
     * @param studentId student ID
     */
    void deleteById(String studentId);
    
    /**
     * Deletes all students (testing only).
     */
    void deleteAll();
}