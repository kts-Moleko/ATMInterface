package com.calpay.test;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import com.calpay.util.*;

/**
 * Unit tests for validation utilities.
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
@DisplayName("Validation Utils Tests")
class ValidationUtilsTest {
    
    @Nested
    @DisplayName("Email Validation")
    class EmailValidationTests {
        
        @Test
        @DisplayName("Should validate correct email")
        void shouldValidateCorrectEmail() {
            assertTrue(ValidationUtils.isValidEmail("test@example.com"));
            assertTrue(ValidationUtils.isValidEmail("user.name@domain.co.za"));
        }
        
        @Test
        @DisplayName("Should reject invalid email")
        void shouldRejectInvalidEmail() {
            assertFalse(ValidationUtils.isValidEmail("invalid"));
            assertFalse(ValidationUtils.isValidEmail("@example.com"));
            assertFalse(ValidationUtils.isValidEmail("test@"));
            assertFalse(ValidationUtils.isValidEmail(null));
        }
    }
    
    @Nested
    @DisplayName("Phone Validation")
    class PhoneValidationTests {
        
        @Test
        @DisplayName("Should validate SA phone numbers")
        void shouldValidateSAPhone() {
            assertTrue(ValidationUtils.isValidSAPhone("+27821234567"));
            assertTrue(ValidationUtils.isValidSAPhone("0821234567"));
        }
        
        @Test
        @DisplayName("Should normalize phone numbers")
        void shouldNormalizePhone() {
            assertEquals("+27821234567", ValidationUtils.normalizePhone("0821234567"));
            assertEquals("+27821234567", ValidationUtils.normalizePhone("+27821234567"));
            assertEquals("+27821234567", ValidationUtils.normalizePhone("082 123 4567"));
        }
        
        @Test
        @DisplayName("Should reject invalid phone")
        void shouldRejectInvalidPhone() {
            assertFalse(ValidationUtils.isValidSAPhone("123"));
            assertFalse(ValidationUtils.isValidSAPhone("invalid"));
            assertFalse(ValidationUtils.isValidSAPhone(null));
        }
    }
    
    @Nested
    @DisplayName("SA ID Number Validation")
    class IdNumberValidationTests {
        
        @Test
        @DisplayName("Should validate correct SA ID number")
        void shouldValidateSAIdNumber() {
            // Valid SA ID numbers (with correct checksum)
            assertTrue(ValidationUtils.isValidSAIdNumber("0001010001088"));
            assertTrue(ValidationUtils.isValidSAIdNumber("8001015009087"));
        }
        
        @Test
        @DisplayName("Should reject invalid SA ID number")
        void shouldRejectInvalidSAIdNumber() {
            assertFalse(ValidationUtils.isValidSAIdNumber("123"));
            assertFalse(ValidationUtils.isValidSAIdNumber("0001019999999")); // Invalid date
            assertFalse(ValidationUtils.isValidSAIdNumber(null));
        }
    }
    
    @Nested
    @DisplayName("Grade Level Validation")
    class GradeLevelValidationTests {
        
        @Test
        @DisplayName("Should validate correct grade levels")
        void shouldValidateGradeLevel() {
            assertTrue(ValidationUtils.isValidGradeLevel("Grade R"));
            assertTrue(ValidationUtils.isValidGradeLevel("Grade 1"));
            assertTrue(ValidationUtils.isValidGradeLevel("Grade 10"));
            assertTrue(ValidationUtils.isValidGradeLevel("Grade 12"));
        }
        
        @Test
        @DisplayName("Should reject invalid grade levels")
        void shouldRejectInvalidGradeLevel() {
            assertFalse(ValidationUtils.isValidGradeLevel("Grade 13"));
            assertFalse(ValidationUtils.isValidGradeLevel("Grade 0"));
            assertFalse(ValidationUtils.isValidGradeLevel("10"));
            assertFalse(ValidationUtils.isValidGradeLevel(null));
        }
    }
}