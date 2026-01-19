package com.calpay.core.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a student enrolled at a school.
 * 
 * <p><b>Relationships:</b>
 * <ul>
 *   <li>Belongs to one School (tenant)</li>
 *   <li>Has one or more Parents (via ParentStudent junction)</li>
 *   <li>Has associated Payments</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public class Student implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Identity
    private final String studentId;
    private final String schoolId;
    private final String studentCode;  // e.g., "STU001" (unique per school)
    private final LocalDateTime createdAt;
    
    // Personal info
    private String firstName;
    private String lastName;
    private String idNumberEncrypted;  // SA ID number (encrypted)
    private LocalDate dateOfBirth;
    
    // Academic
    private String gradeLevel;  // "Grade 10", "Grade R", etc.
    private LocalDate admissionDate;
    private int academicYear;
    
    // Financial
    private boolean hasScholarship;
    private int scholarshipPercentage;  // 0-100
    
    // Status
    private boolean isActive;
    
    private Student(Builder builder) {
        this.studentId = Objects.requireNonNull(builder.studentId, "Student ID cannot be null");
        this.schoolId = Objects.requireNonNull(builder.schoolId, "School ID cannot be null");
        this.studentCode = Objects.requireNonNull(builder.studentCode, "Student code cannot be null");
        this.firstName = Objects.requireNonNull(builder.firstName, "First name cannot be null");
        this.lastName = Objects.requireNonNull(builder.lastName, "Last name cannot be null");
        this.gradeLevel = Objects.requireNonNull(builder.gradeLevel, "Grade level cannot be null");
        this.admissionDate = Objects.requireNonNull(builder.admissionDate, "Admission date cannot be null");
        this.idNumberEncrypted = builder.idNumberEncrypted;
        this.dateOfBirth = builder.dateOfBirth;
        this.academicYear = builder.academicYear > 0 ? builder.academicYear : LocalDate.now().getYear();
        this.hasScholarship = builder.hasScholarship;
        this.scholarshipPercentage = builder.scholarshipPercentage;
        this.isActive = builder.isActive;
        this.createdAt = builder.createdAt != null ? builder.createdAt : LocalDateTime.now();
    }
    
    // Getters
    public String getStudentId() { return studentId; }
    public String getSchoolId() { return schoolId; }
    public String getStudentCode() { return studentCode; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getFullName() { return firstName + " " + lastName; }
    public String getIdNumberEncrypted() { return idNumberEncrypted; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getGradeLevel() { return gradeLevel; }
    public LocalDate getAdmissionDate() { return admissionDate; }
    public int getAcademicYear() { return academicYear; }
    public boolean hasScholarship() { return hasScholarship; }
    public int getScholarshipPercentage() { return scholarshipPercentage; }
    public boolean isActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    // Setters
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setGradeLevel(String gradeLevel) { this.gradeLevel = gradeLevel; }
    public void setScholarship(boolean hasScholarship, int percentage) {
        this.hasScholarship = hasScholarship;
        this.scholarshipPercentage = Math.max(0, Math.min(100, percentage));
    }
    
    public void deactivate() { this.isActive = false; }
    public void activate() { this.isActive = true; }
    
    // Business methods
    public Money applyScholarship(Money feeAmount) {
        if (!hasScholarship || scholarshipPercentage == 0) {
            return feeAmount;
        }
        
        double discountMultiplier = 1.0 - (scholarshipPercentage / 100.0);
        return feeAmount.multiply(discountMultiplier);
    }
    
    // Builder
    public static class Builder {
        private String studentId;
        private String schoolId;
        private String studentCode;
        private String firstName;
        private String lastName;
        private String idNumberEncrypted;
        private LocalDate dateOfBirth;
        private String gradeLevel;
        private LocalDate admissionDate;
        private int academicYear;
        private boolean hasScholarship = false;
        private int scholarshipPercentage = 0;
        private boolean isActive = true;
        private LocalDateTime createdAt;
        
        public Builder studentId(String studentId) {
            this.studentId = studentId;
            return this;
        }
        
        public Builder schoolId(String schoolId) {
            this.schoolId = schoolId;
            return this;
        }
        
        public Builder studentCode(String studentCode) {
            this.studentCode = studentCode;
            return this;
        }
        
        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }
        
        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }
        
        public Builder idNumberEncrypted(String idNumberEncrypted) {
            this.idNumberEncrypted = idNumberEncrypted;
            return this;
        }
        
        public Builder dateOfBirth(LocalDate dateOfBirth) {
            this.dateOfBirth = dateOfBirth;
            return this;
        }
        
        public Builder gradeLevel(String gradeLevel) {
            this.gradeLevel = gradeLevel;
            return this;
        }
        
        public Builder admissionDate(LocalDate admissionDate) {
            this.admissionDate = admissionDate;
            return this;
        }
        
        public Builder academicYear(int academicYear) {
            this.academicYear = academicYear;
            return this;
        }
        
        public Builder hasScholarship(boolean hasScholarship) {
            this.hasScholarship = hasScholarship;
            return this;
        }
        
        public Builder scholarshipPercentage(int scholarshipPercentage) {
            this.scholarshipPercentage = scholarshipPercentage;
            return this;
        }
        
        public Builder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }
        
        public Student build() {
            return new Student(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return studentId.equals(student.studentId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(studentId);
    }
    
    @Override
    public String toString() {
        return String.format("Student{id='%s', code='%s', name='%s', grade='%s', school='%s'}",
            studentId, studentCode, getFullName(), gradeLevel, schoolId);
    }
}