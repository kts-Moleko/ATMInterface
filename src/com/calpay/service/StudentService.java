package com.calpay.service;

import com.calpay.audit.AuditLogger;
import com.calpay.core.exceptions.InvalidRequestException;
import com.calpay.core.exceptions.ResourceNotFoundException;
import com.calpay.core.model.Student;
import com.calpay.core.model.School;
import com.calpay.repository.StudentRepository;
import com.calpay.repository.SchoolRepository;
import com.calpay.repository.SchoolAccountRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Service for managing students.
 * 
 * <p><b>Key Responsibilities:</b>
 * <ul>
 *   <li>Student enrollment</li>
 *   <li>Student profile updates</li>
 *   <li>Grade progression</li>
 *   <li>Scholarship management</li>
 *   <li>Student deactivation (graduation/withdrawal)</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public class StudentService {
    
    private final StudentRepository studentRepository;
    private final SchoolRepository schoolRepository;
    private final SchoolAccountRepository schoolAccountRepository;
    private final SchoolService schoolService;
    private final AuditLogger auditLogger;
    
    public StudentService(
            StudentRepository studentRepository,
            SchoolRepository schoolRepository,
            SchoolAccountRepository schoolAccountRepository,
            SchoolService schoolService,
            AuditLogger auditLogger) {
        this.studentRepository = Objects.requireNonNull(studentRepository);
        this.schoolRepository = Objects.requireNonNull(schoolRepository);
        this.schoolAccountRepository = Objects.requireNonNull(schoolAccountRepository);
        this.schoolService = Objects.requireNonNull(schoolService);
        this.auditLogger = Objects.requireNonNull(auditLogger);
    }
    
    /**
     * Enrolls a new student.
     * 
     * <p><b>Process:</b>
     * <ol>
     *   <li>Validate school exists and can add students</li>
     *   <li>Generate student code (e.g., STU001)</li>
     *   <li>Create student entity</li>
     *   <li>Update school student count</li>
     *   <li>Audit log</li>
     * </ol>
     * 
     * @param schoolId school ID
     * @param firstName first name
     * @param lastName last name
     * @param gradeLevel grade level (e.g., "Grade 10")
     * @param dateOfBirth date of birth
     * @return created student
     */
    public Student enrollStudent(
            String schoolId,
            String firstName,
            String lastName,
            String gradeLevel,
            LocalDate dateOfBirth) {
        
        // Validate school exists
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("School", schoolId));
        
        // Check if school can add more students (subscription limits)
        if (!schoolService.canAddStudent(schoolId)) {
            throw new InvalidRequestException(
                "School has reached student limit for current subscription tier"
            );
        }
        
        // Validate inputs
        validateName(firstName, "First name");
        validateName(lastName, "Last name");
        validateGradeLevel(gradeLevel);
        
        // Generate student code
        String studentCode = generateStudentCode(schoolId);
        String studentId = UUID.randomUUID().toString();
        
        // Create student
        Student student = new Student.Builder()
            .studentId(studentId)
            .schoolId(schoolId)
            .studentCode(studentCode)
            .firstName(firstName)
            .lastName(lastName)
            .gradeLevel(gradeLevel)
            .dateOfBirth(dateOfBirth)
            .admissionDate(LocalDate.now())
            .academicYear(LocalDate.now().getYear())
            .isActive(true)
            .build();
        
        // Save
        Student savedStudent = studentRepository.save(student);
        
        // Update school student count
        int currentCount = studentRepository.countBySchoolIdAndIsActive(schoolId, true);
        schoolService.updateStudentCount(schoolId, currentCount);
        
        // Audit
        auditLogger.log(
            "STUDENT_ENROLLED",
            studentId,
            String.format("Student enrolled: %s (Code: %s, Grade: %s)", 
                student.getFullName(), studentCode, gradeLevel),
            null,
            schoolId
        );
        
        return savedStudent;
    }
    
    /**
     * Updates student profile.
     * 
     * @param studentId student ID
     * @param firstName new first name (optional)
     * @param lastName new last name (optional)
     * @param gradeLevel new grade level (optional)
     */
    public void updateStudent(
            String studentId,
            String firstName,
            String lastName,
            String gradeLevel) {
        
        Student student = findStudentById(studentId);
        
        if (firstName != null) {
            validateName(firstName, "First name");
            student.setFirstName(firstName);
        }
        
        if (lastName != null) {
            validateName(lastName, "Last name");
            student.setLastName(lastName);
        }
        
        if (gradeLevel != null) {
            validateGradeLevel(gradeLevel);
            student.setGradeLevel(gradeLevel);
        }
        
        studentRepository.save(student);
        
        auditLogger.log(
            "STUDENT_UPDATED",
            studentId,
            "Student profile updated",
            null,
            student.getSchoolId()
        );
    }
    
    /**
     * Applies or updates scholarship for a student.
     * 
     * @param studentId student ID
     * @param percentage scholarship percentage (0-100)
     */
    public void applyScholarship(String studentId, int percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new InvalidRequestException("Scholarship percentage must be between 0 and 100");
        }
        
        Student student = findStudentById(studentId);
        student.setScholarship(percentage > 0, percentage);
        studentRepository.save(student);
        
        auditLogger.log(
            "SCHOLARSHIP_APPLIED",
            studentId,
            String.format("Scholarship applied: %d%%", percentage),
            null,
            student.getSchoolId()
        );
    }
    
    /**
     * Removes scholarship from a student.
     * 
     * @param studentId student ID
     */
    public void removeScholarship(String studentId) {
        Student student = findStudentById(studentId);
        student.setScholarship(false, 0);
        studentRepository.save(student);
        
        auditLogger.log(
            "SCHOLARSHIP_REMOVED",
            studentId,
            "Scholarship removed",
            null,
            student.getSchoolId()
        );
    }
    
    /**
     * Promotes student to next grade.
     * 
     * <p><b>Use case:</b> End of academic year grade progression.
     * 
     * @param studentId student ID
     * @param newGradeLevel new grade level
     */
    public void promoteStudent(String studentId, String newGradeLevel) {
        validateGradeLevel(newGradeLevel);
        
        Student student = findStudentById(studentId);
        String oldGrade = student.getGradeLevel();
        student.setGradeLevel(newGradeLevel);
        studentRepository.save(student);
        
        auditLogger.log(
            "STUDENT_PROMOTED",
            studentId,
            String.format("Student promoted: %s → %s", oldGrade, newGradeLevel),
            null,
            student.getSchoolId()
        );
    }
    
    /**
     * Deactivates a student (graduation/withdrawal).
     * 
     * @param studentId student ID
     * @param reason deactivation reason
     */
    public void deactivateStudent(String studentId, String reason) {
        Student student = findStudentById(studentId);
        student.deactivate();
        studentRepository.save(student);
        
        // Update school student count
        int currentCount = studentRepository.countBySchoolIdAndIsActive(student.getSchoolId(), true);
        schoolService.updateStudentCount(student.getSchoolId(), currentCount);
        
        auditLogger.log(
            "STUDENT_DEACTIVATED",
            studentId,
            "Student deactivated: " + reason,
            null,
            student.getSchoolId()
        );
    }
    
    /**
     * Reactivates a student.
     * 
     * @param studentId student ID
     */
    public void reactivateStudent(String studentId) {
        Student student = findStudentById(studentId);
        
        // Check if school can add students
        if (!schoolService.canAddStudent(student.getSchoolId())) {
            throw new InvalidRequestException(
                "School has reached student limit for current subscription tier"
            );
        }
        
        student.activate();
        studentRepository.save(student);
        
        // Update school student count
        int currentCount = studentRepository.countBySchoolIdAndIsActive(student.getSchoolId(), true);
        schoolService.updateStudentCount(student.getSchoolId(), currentCount);
        
        auditLogger.log(
            "STUDENT_REACTIVATED",
            studentId,
            "Student reactivated",
            null,
            student.getSchoolId()
        );
    }
    
    /**
     * Gets student by ID.
     * 
     * @param studentId student ID
     * @return student
     */
    public Student getStudent(String studentId) {
        return findStudentById(studentId);
    }
    
    /**
     * Gets student by code.
     * 
     * @param schoolId school ID
     * @param studentCode student code (e.g., STU001)
     * @return student
     */
    public Student getStudentByCode(String schoolId, String studentCode) {
        return studentRepository.findBySchoolIdAndStudentCode(schoolId, studentCode)
            .orElseThrow(() -> new ResourceNotFoundException("Student", studentCode));
    }
    
    /**
     * Gets all students for a school.
     * 
     * @param schoolId school ID
     * @return list of students
     */
    public List<Student> getSchoolStudents(String schoolId) {
        return studentRepository.findBySchoolId(schoolId);
    }
    
    /**
     * Gets all active students for a school.
     * 
     * @param schoolId school ID
     * @return list of active students
     */
    public List<Student> getActiveStudents(String schoolId) {
        return studentRepository.findBySchoolIdAndIsActive(schoolId, true);
    }
    
    /**
     * Gets students by grade level.
     * 
     * @param schoolId school ID
     * @param gradeLevel grade level
     * @return list of students
     */
    public List<Student> getStudentsByGrade(String schoolId, String gradeLevel) {
        return studentRepository.findBySchoolIdAndGradeLevel(schoolId, gradeLevel);
    }
    
    /**
     * Gets students with scholarships.
     * 
     * @param schoolId school ID
     * @return list of students with scholarships
     */
    public List<Student> getScholarshipStudents(String schoolId) {
        return studentRepository.findBySchoolIdAndHasScholarship(schoolId, true);
    }
    
    /**
     * Searches students by name.
     * 
     * @param schoolId school ID
     * @param searchTerm search term
     * @return matching students
     */
    public List<Student> searchStudents(String schoolId, String searchTerm) {
        return studentRepository.searchByName(schoolId, searchTerm);
    }
    
    // ========== Helper Methods ==========
    
    private Student findStudentById(String studentId) {
        return studentRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
    }
    
    private String generateStudentCode(String schoolId) {
        // Generate: STU + 3-digit number (e.g., STU001)
        int count = studentRepository.countBySchoolId(schoolId) + 1;
        return String.format("STU%03d", count);
    }
    
    private void validateName(String name, String fieldName) {
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidRequestException(fieldName + " cannot be empty");
        }
        
        if (name.length() < 2) {
            throw new InvalidRequestException(fieldName + " must be at least 2 characters");
        }
    }
    
    private void validateGradeLevel(String gradeLevel) {
        if (gradeLevel == null || gradeLevel.trim().isEmpty()) {
            throw new InvalidRequestException("Grade level cannot be empty");
        }
        
        // Valid formats: "Grade R", "Grade 1", "Grade 10", "Grade 12"
        if (!gradeLevel.matches("Grade (R|[1-9]|1[0-2])")) {
            throw new InvalidRequestException(
                "Invalid grade level format. Expected: 'Grade R' or 'Grade 1-12'"
            );
        }
    }
}