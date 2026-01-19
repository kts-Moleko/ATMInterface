package com.calpay.test;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import com.calpay.audit.AuditLogger;
import com.calpay.audit.InMemoryAuditLogger;
import com.calpay.core.enums.*;
import com.calpay.core.exceptions.*;
import com.calpay.core.model.*;
import com.calpay.repository.*;
import com.calpay.repository.impl.*;
import com.calpay.security.*;
import com.calpay.service.*;
import com.calpay.util.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Complete test suite for CalPay Platform v2.0.
 * 
 * <p><b>Test Coverage:</b>
 * <ul>
 *   <li>Repository operations (CRUD, queries, indexes)</li>
 *   <li>Service layer business logic</li>
 *   <li>Payment processing workflows</li>
 *   <li>Security and multi-tenancy</li>
 *   <li>Idempotency and error handling</li>
 *   <li>Audit trail generation</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
@DisplayName("CalPay Platform Complete Test Suite")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CalPayCompleteTestSuite {
    
    // Infrastructure
    private static AuditLogger auditLogger;
    private static SecurityService securityService;
    
    // Repositories
    private static SchoolRepository schoolRepo;
    private static SchoolAccountRepository schoolAccountRepo;
    private static StudentRepository studentRepo;
    private static ParentRepository parentRepo;
    private static FeeStructureRepository feeStructureRepo;
    private static PaymentRepository paymentRepo;
    private static SchoolAdminRepository adminRepo;
    
    // Services
    private static SchoolService schoolService;
    private static StudentService studentService;
    private static ParentService parentService;
    private static FeeStructureService feeStructureService;
    private static FeePaymentService feePaymentService;
    private static PaymentHistoryService paymentHistoryService;
    private static ReceiptService receiptService;
    
    // Test data
    private static School testSchool;
    private static Student testStudent;
    private static Parent testParent;
    private static FeeStructure testFee;
    
    @BeforeAll
    static void setupAll() {
        // Initialize infrastructure
        auditLogger = new InMemoryAuditLogger();
        securityService = new InMemorySecurityService(auditLogger);
        
        // Initialize repositories
        schoolRepo = new InMemorySchoolRepository();
        schoolAccountRepo = new InMemorySchoolAccountRepository();
        studentRepo = new InMemoryStudentRepository();
        parentRepo = new InMemoryParentRepository();
        feeStructureRepo = new InMemoryFeeStructureRepository();
        paymentRepo = new InMemoryPaymentRepository();
        adminRepo = new InMemorySchoolAdminRepository();
        
        // Initialize services
        schoolService = new SchoolService(schoolRepo, schoolAccountRepo, auditLogger);
        studentService = new StudentService(
            studentRepo, schoolRepo, schoolAccountRepo, schoolService, auditLogger
        );
        parentService = new ParentService(parentRepo, schoolRepo, auditLogger);
        feeStructureService = new FeeStructureService(feeStructureRepo, schoolRepo, auditLogger);
        feePaymentService = new FeePaymentService(
            paymentRepo, parentRepo, studentRepo, feeStructureRepo, schoolAccountRepo, auditLogger
        );
        paymentHistoryService = new PaymentHistoryService(paymentRepo);
        receiptService = new ReceiptService(paymentRepo, parentRepo, studentRepo, schoolRepo);
    }
    
    @BeforeEach
    void setupEach() {
        // Clean up repositories before each test
        schoolRepo.deleteAll();
        schoolAccountRepo.deleteAll();
        studentRepo.deleteAll();
        parentRepo.deleteAll();
        feeStructureRepo.deleteAll();
        paymentRepo.deleteAll();
        adminRepo.deleteAll();
        
        // Create fresh test data
        createTestData();
    }
    
    private void createTestData() {
        // Register school
        testSchool = schoolService.registerSchool(
            "Test Primary School",
            "test@school.co.za",
            "+27821111111"
        );
        
        // Enroll student
        testStudent = studentService.enrollStudent(
            testSchool.getSchoolId(),
            "Test",
            "Student",
            "Grade 10",
            LocalDate.of(2010, 1, 1)
        );
        
        // Register parent
        testParent = parentService.registerParent(
            testSchool.getSchoolId(),
            "Test",
            "Parent",
            "+27822222222",
            "test.parent@email.com"
        );
        
        // Create fee structure
        testFee = new FeeStructure.Builder()
            .feeId(UUID.randomUUID().toString())
            .schoolId(testSchool.getSchoolId())
            .feeType("TUITION")
            .feeName("Test Tuition")
            .amount(Money.of(1000.00, "ZAR"))
            .frequency("MONTHLY")
            .dueDayOfMonth(5)
            .gradeLevel("Grade 10")
            .isMandatory(true)
            .isActive(true)
            .build();
        feeStructureRepo.save(testFee);
    }
    
    // ========== Repository Tests ==========
    
    @Nested
    @DisplayName("Repository Tests")
    class RepositoryTests {
        
        @Test
        @DisplayName("Should save and retrieve school")
        void shouldSaveAndRetrieveSchool() {
            School retrieved = schoolRepo.findById(testSchool.getSchoolId()).orElse(null);
            
            assertNotNull(retrieved);
            assertEquals(testSchool.getSchoolId(), retrieved.getSchoolId());
            assertEquals(testSchool.getName(), retrieved.getName());
            assertEquals(testSchool.getEmail(), retrieved.getEmail());
        }
        
        @Test
        @DisplayName("Should enforce unique school email")
        void shouldEnforceUniqueSchoolEmail() {
            assertTrue(schoolRepo.existsByEmail(testSchool.getEmail()));
            
            assertThrows(Exception.class, () -> {
                schoolService.registerSchool(
                    "Another School",
                    testSchool.getEmail(), // Duplicate email
                    "+27829999999"
                );
            });
        }
        
        @Test
        @DisplayName("Should save and retrieve student with indexes")
        void shouldSaveAndRetrieveStudentWithIndexes() {
            // By ID
            Student byId = studentRepo.findById(testStudent.getStudentId()).orElse(null);
            assertNotNull(byId);
            assertEquals(testStudent.getStudentId(), byId.getStudentId());
            
            // By school and code
            Student byCode = studentRepo.findBySchoolIdAndStudentCode(
                testSchool.getSchoolId(), 
                testStudent.getStudentCode()
            ).orElse(null);
            assertNotNull(byCode);
            assertEquals(testStudent.getStudentId(), byCode.getStudentId());
        }
        
        @Test
        @DisplayName("Should filter students by grade level")
        void shouldFilterStudentsByGradeLevel() {
            List<Student> grade10Students = studentRepo.findBySchoolIdAndGradeLevel(
                testSchool.getSchoolId(), 
                "Grade 10"
            );
            
            assertEquals(1, grade10Students.size());
            assertEquals(testStudent.getStudentId(), grade10Students.get(0).getStudentId());
        }
        
        @Test
        @DisplayName("Should save and retrieve parent with phone index")
        void shouldSaveAndRetrieveParentWithPhoneIndex() {
            Parent byPhone = parentRepo.findBySchoolIdAndPhone(
                testSchool.getSchoolId(),
                testParent.getPhone()
            ).orElse(null);
            
            assertNotNull(byPhone);
            assertEquals(testParent.getParentId(), byPhone.getParentId());
        }
        
        @Test
        @DisplayName("Should prevent duplicate parent phone per school")
        void shouldPreventDuplicateParentPhone() {
            assertTrue(parentRepo.existsBySchoolIdAndPhone(
                testSchool.getSchoolId(),
                testParent.getPhone()
            ));
            
            assertThrows(Exception.class, () -> {
                parentService.registerParent(
                    testSchool.getSchoolId(),
                    "Another",
                    "Parent",
                    testParent.getPhone(), // Duplicate phone
                    "another@email.com"
                );
            });
        }
        
        @Test
        @DisplayName("Should save and retrieve fee structure")
        void shouldSaveAndRetrieveFeeStructure() {
            FeeStructure retrieved = feeStructureRepo.findById(testFee.getFeeId()).orElse(null);
            
            assertNotNull(retrieved);
            assertEquals(testFee.getFeeId(), retrieved.getFeeId());
            assertEquals(testFee.getFeeName(), retrieved.getFeeName());
            assertEquals(testFee.getAmount(), retrieved.getAmount());
        }
        
        @Test
        @DisplayName("Should filter active fee structures")
        void shouldFilterActiveFeeStructures() {
            List<FeeStructure> activeFees = feeStructureRepo.findActiveBySchoolId(
                testSchool.getSchoolId()
            );
            
            assertEquals(1, activeFees.size());
            assertTrue(activeFees.get(0).isActive());
        }
        
        @Test
        @DisplayName("Should find school account by school ID")
        void shouldFindSchoolAccountBySchoolId() {
            SchoolAccount account = schoolAccountRepo.findBySchoolId(testSchool.getSchoolId())
                .orElse(null);
            
            assertNotNull(account);
            assertEquals(testSchool.getSchoolId(), account.getSchoolId());
        }
    }
    
    // ========== Service Layer Tests ==========
    
    @Nested
    @DisplayName("Service Layer Tests")
    class ServiceLayerTests {
        
        @Test
        @DisplayName("Should register school with trial subscription")
        void shouldRegisterSchoolWithTrialSubscription() {
            assertEquals("TRIAL", testSchool.getSubscriptionStatus());
            assertNotNull(testSchool.getSubscriptionExpiresAt());
            assertTrue(testSchool.isActive());
        }
        
        @Test
        @DisplayName("Should enroll student and update school count")
        void shouldEnrollStudentAndUpdateSchoolCount() {
            School updated = schoolRepo.findById(testSchool.getSchoolId()).orElse(null);
            assertNotNull(updated);
            assertTrue(updated.getTotalStudents() > 0);
        }
        
        @Test
        @DisplayName("Should apply scholarship to student")
        void shouldApplyScholarshipToStudent() {
            studentService.applyScholarship(testStudent.getStudentId(), 50);
            
            Student updated = studentRepo.findById(testStudent.getStudentId()).orElse(null);
            assertNotNull(updated);
            assertTrue(updated.hasScholarship());
            assertEquals(50, updated.getScholarshipPercentage());
            
            // Test discount calculation
            Money original = Money.of(1000.00, "ZAR");
            Money discounted = updated.applyScholarship(original);
            assertEquals(Money.of(500.00, "ZAR"), discounted);
        }
        
        @Test
        @DisplayName("Should verify parent phone number")
        void shouldVerifyParentPhoneNumber() {
            assertFalse(testParent.isPhoneVerified());
            
            parentService.verifyPhone(testParent.getParentId());
            
            Parent updated = parentRepo.findById(testParent.getParentId()).orElse(null);
            assertNotNull(updated);
            assertTrue(updated.isPhoneVerified());
        }
    }
    
    // ========== Payment Processing Tests ==========
    
    @Nested
    @DisplayName("Payment Processing Tests")
    class PaymentProcessingTests {
        
        @Test
        @DisplayName("Should process payment successfully")
        void shouldProcessPaymentSuccessfully() {
            Payment payment = feePaymentService.processPayment(
                testParent.getParentId(),
                testStudent.getStudentId(),
                testFee.getFeeId(),
                Money.of(1000.00, "ZAR"),
                PaymentMethod.INSTANT_EFT,
                UUID.randomUUID().toString()
            );
            
            assertNotNull(payment);
            assertNotNull(payment.getPaymentId());
            assertNotNull(payment.getPaymentReference());
            assertEquals("COMPLETED", payment.getPaymentStatus());
            assertEquals(Money.of(1000.00, "ZAR"), payment.getAmount());
        }
        
        @Test
        @DisplayName("Should prevent duplicate payments with idempotency")
        void shouldPreventDuplicatePayments() {
            String idempotencyKey = UUID.randomUUID().toString();
            
            // First payment succeeds
            Payment first = feePaymentService.processPayment(
                testParent.getParentId(),
                testStudent.getStudentId(),
                testFee.getFeeId(),
                Money.of(1000.00, "ZAR"),
                PaymentMethod.INSTANT_EFT,
                idempotencyKey
            );
            
            assertNotNull(first);
            
            // Duplicate attempt throws exception
            DuplicatePaymentException exception = assertThrows(
                DuplicatePaymentException.class,
                () -> feePaymentService.processPayment(
                    testParent.getParentId(),
                    testStudent.getStudentId(),
                    testFee.getFeeId(),
                    Money.of(1000.00, "ZAR"),
                    PaymentMethod.INSTANT_EFT,
                    idempotencyKey
                )
            );
            
            assertEquals(idempotencyKey, exception.getIdempotencyKey());
            assertEquals(first.getPaymentId(), exception.getExistingPaymentId());
        }
        
        @Test
        @DisplayName("Should reject payment with invalid parent")
        void shouldRejectPaymentWithInvalidParent() {
            assertThrows(ResourceNotFoundException.class, () -> {
                feePaymentService.processPayment(
                    "INVALID_PARENT_ID",
                    testStudent.getStudentId(),
                    testFee.getFeeId(),
                    Money.of(1000.00, "ZAR"),
                    PaymentMethod.INSTANT_EFT,
                    UUID.randomUUID().toString()
                );
            });
        }
        
        @Test
        @DisplayName("Should reject payment with invalid student")
        void shouldRejectPaymentWithInvalidStudent() {
            assertThrows(ResourceNotFoundException.class, () -> {
                feePaymentService.processPayment(
                    testParent.getParentId(),
                    "INVALID_STUDENT_ID",
                    testFee.getFeeId(),
                    Money.of(1000.00, "ZAR"),
                    PaymentMethod.INSTANT_EFT,
                    UUID.randomUUID().toString()
                );
            });
        }
        
        @Test
        @DisplayName("Should update school account balance after payment")
        void shouldUpdateSchoolAccountBalanceAfterPayment() {
            SchoolAccount accountBefore = schoolAccountRepo.findBySchoolId(testSchool.getSchoolId())
                .orElseThrow();
            Money balanceBefore = accountBefore.getBalance();
            
            feePaymentService.processPayment(
                testParent.getParentId(),
                testStudent.getStudentId(),
                testFee.getFeeId(),
                Money.of(1000.00, "ZAR"),
                PaymentMethod.INSTANT_EFT,
                UUID.randomUUID().toString()
            );
            
            SchoolAccount accountAfter = schoolAccountRepo.findBySchoolId(testSchool.getSchoolId())
                .orElseThrow();
            Money balanceAfter = accountAfter.getBalance();
            
            assertEquals(balanceBefore.add(Money.of(1000.00, "ZAR")), balanceAfter);
        }
        
        @Test
        @DisplayName("Should calculate total paid for student")
        void shouldCalculateTotalPaidForStudent() {
            // Make 3 payments
            for (int i = 0; i < 3; i++) {
                feePaymentService.processPayment(
                    testParent.getParentId(),
                    testStudent.getStudentId(),
                    testFee.getFeeId(),
                    Money.of(1000.00, "ZAR"),
                    PaymentMethod.INSTANT_EFT,
                    UUID.randomUUID().toString()
                );
            }
            
            Money totalPaid = feePaymentService.calculateTotalPaid(testStudent.getStudentId());
            assertEquals(Money.of(3000.00, "ZAR"), totalPaid);
        }
    }
    
    // ========== Payment History Tests ==========
    
    @Nested
    @DisplayName("Payment History Tests")
    class PaymentHistoryTests {
        
        @Test
        @DisplayName("Should retrieve payment by reference")
        void shouldRetrievePaymentByReference() {
            Payment payment = feePaymentService.processPayment(
                testParent.getParentId(),
                testStudent.getStudentId(),
                testFee.getFeeId(),
                Money.of(1000.00, "ZAR"),
                PaymentMethod.INSTANT_EFT,
                UUID.randomUUID().toString()
            );
            
            Payment retrieved = feePaymentService.getPaymentByReference(payment.getPaymentReference());
            
            assertNotNull(retrieved);
            assertEquals(payment.getPaymentId(), retrieved.getPaymentId());
        }
        
        @Test
        @DisplayName("Should get payment statistics")
        void shouldGetPaymentStatistics() {
            // Create multiple payments
            for (int i = 0; i < 5; i++) {
                feePaymentService.processPayment(
                    testParent.getParentId(),
                    testStudent.getStudentId(),
                    testFee.getFeeId(),
                    Money.of(1000.00, "ZAR"),
                    PaymentMethod.INSTANT_EFT,
                    UUID.randomUUID().toString()
                );
            }
            
            PaymentHistoryService.PaymentStatistics stats = 
                paymentHistoryService.getStatistics(testSchool.getSchoolId());
            
            assertEquals(5, stats.getTotalPayments());
            assertEquals(5, stats.getCompletedPayments());
            assertEquals(0, stats.getPendingPayments());
            assertEquals(0, stats.getFailedPayments());
            assertEquals(100.0, stats.getSuccessRate(), 0.01);
            assertEquals(Money.of(5000.00, "ZAR"), stats.getTotalCollected());
        }
        
        @Test
        @DisplayName("Should filter payments by status")
        void shouldFilterPaymentsByStatus() {
            feePaymentService.processPayment(
                testParent.getParentId(),
                testStudent.getStudentId(),
                testFee.getFeeId(),
                Money.of(1000.00, "ZAR"),
                PaymentMethod.INSTANT_EFT,
                UUID.randomUUID().toString()
            );
            
            List<Payment> completed = paymentHistoryService.getCompletedPayments(testSchool.getSchoolId());
            
            assertEquals(1, completed.size());
            assertEquals("COMPLETED", completed.get(0).getPaymentStatus());
        }
    }
    
    // ========== Receipt Generation Tests ==========
    
    @Nested
    @DisplayName("Receipt Generation Tests")
    class ReceiptGenerationTests {
        
        @Test
        @DisplayName("Should generate payment receipt")
        void shouldGeneratePaymentReceipt() {
            Payment payment = feePaymentService.processPayment(
                testParent.getParentId(),
                testStudent.getStudentId(),
                testFee.getFeeId(),
                Money.of(1000.00, "ZAR"),
                PaymentMethod.INSTANT_EFT,
                UUID.randomUUID().toString()
            );
            
            String receipt = receiptService.generateReceipt(payment.getPaymentId());
            
            assertNotNull(receipt);
            assertTrue(receipt.contains("PAYMENT RECEIPT"));
            assertTrue(receipt.contains(payment.getPaymentReference()));
            assertTrue(receipt.contains(testSchool.getName()));
            assertTrue(receipt.contains(testStudent.getFullName()));
        }
        
        @Test
        @DisplayName("Should generate short receipt for SMS")
        void shouldGenerateShortReceiptForSMS() {
            Payment payment = feePaymentService.processPayment(
                testParent.getParentId(),
                testStudent.getStudentId(),
                testFee.getFeeId(),
                Money.of(1000.00, "ZAR"),
                PaymentMethod.INSTANT_EFT,
                UUID.randomUUID().toString()
            );
            
            String shortReceipt = receiptService.generateShortReceipt(payment.getPaymentId());
            
            assertNotNull(shortReceipt);
            assertTrue(shortReceipt.contains("Payment Received"));
            assertTrue(shortReceipt.contains(payment.getPaymentReference()));
            assertTrue(shortReceipt.length() < 200); // SMS friendly
        }
        
        @Test
        @DisplayName("Should generate student statement")
        void shouldGenerateStudentStatement() {
            // Make multiple payments
            for (int i = 0; i < 3; i++) {
                feePaymentService.processPayment(
                    testParent.getParentId(),
                    testStudent.getStudentId(),
                    testFee.getFeeId(),
                    Money.of(1000.00, "ZAR"),
                    PaymentMethod.INSTANT_EFT,
                    UUID.randomUUID().toString()
                );
            }
            
            String statement = receiptService.generateStudentStatement(testStudent.getStudentId());
            
            assertNotNull(statement);
            assertTrue(statement.contains("STUDENT STATEMENT"));
            assertTrue(statement.contains(testStudent.getFullName()));
            assertTrue(statement.contains("Total Paid"));
        }
    }
    
    // ========== Security Tests ==========
    
    @Nested
    @DisplayName("Security Tests")
    class SecurityTests {
        
        @Test
        @DisplayName("Should authenticate user successfully")
        void shouldAuthenticateUserSuccessfully() {
            SecurityContext context = securityService.authenticate(
                "school_admin", 
                "school123", 
                "192.168.1.1"
            );
            
            assertNotNull(context);
            assertEquals("school_admin", context.getUsername());
            assertEquals(UserRole.ADMIN, context.getRole());
            assertNotNull(context.getSessionId());
        }
        
        @Test
        @DisplayName("Should reject invalid credentials")
        void shouldRejectInvalidCredentials() {
            assertThrows(AuthenticationException.class, () -> {
                securityService.authenticate(
                    "school_admin", 
                    "wrong_password", 
                    "192.168.1.1"
                );
            });
        }
        
        @Test
        @DisplayName("Should validate active session")
        void shouldValidateActiveSession() {
            SecurityContext context = securityService.authenticate(
                "school_admin", 
                "school123", 
                "192.168.1.1"
            );
            
            SecurityContext validated = securityService.validateSession(context.getSessionId());
            
            assertNotNull(validated);
            assertEquals(context.getSessionId(), validated.getSessionId());
        }
        
        @Test
        @DisplayName("Should reject expired session")
        void shouldRejectExpiredSession() {
            assertThrows(AuthenticationException.class, () -> {
                securityService.validateSession("INVALID_SESSION_ID");
            });
        }
        
        @Test
        @DisplayName("Should lock account after failed attempts")
        void shouldLockAccountAfterFailedAttempts() {
            // Attempt failed logins
            for (int i = 0; i < 5; i++) {
                try {
                    securityService.authenticate("school_admin", "wrong_password", "192.168.1.1");
                } catch (AuthenticationException e) {
                    // Expected
                }
            }
            
            // Account should be locked
            assertTrue(securityService.isAccountLocked("school_admin"));
        }
    }
    
    // ========== Validation Tests ==========
    
    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {
        
        @Test
        @DisplayName("Should validate SA phone numbers")
        void shouldValidateSAPhoneNumbers() {
            assertTrue(ValidationUtils.isValidSAPhone("+27821234567"));
            assertTrue(ValidationUtils.isValidSAPhone("0821234567"));
            assertFalse(ValidationUtils.isValidSAPhone("invalid"));
            assertFalse(ValidationUtils.isValidSAPhone("123"));
        }
        
        @Test
        @DisplayName("Should validate email addresses")
        void shouldValidateEmailAddresses() {
            assertTrue(ValidationUtils.isValidEmail("test@example.com"));
            assertTrue(ValidationUtils.isValidEmail("user.name@domain.co.za"));
            assertFalse(ValidationUtils.isValidEmail("invalid"));
            assertFalse(ValidationUtils.isValidEmail("@example.com"));
        }
        
        @Test
        @DisplayName("Should validate grade levels")
        void shouldValidateGradeLevels() {
            assertTrue(ValidationUtils.isValidGradeLevel("Grade R"));
            assertTrue(ValidationUtils.isValidGradeLevel("Grade 1"));
            assertTrue(ValidationUtils.isValidGradeLevel("Grade 10"));
            assertTrue(ValidationUtils.isValidGradeLevel("Grade 12"));
            assertFalse(ValidationUtils.isValidGradeLevel("Grade 13"));
            assertFalse(ValidationUtils.isValidGradeLevel("10"));
        }
    }
    
    // ========== Money Value Object Tests ==========
    
    @Nested
    @DisplayName("Money Value Object Tests")
    class MoneyTests {
        
        @Test
        @DisplayName("Should add money amounts")
        void shouldAddMoneyAmounts() {
            Money a = Money.of(100.00, "ZAR");
            Money b = Money.of(50.00, "ZAR");
            Money result = a.add(b);
            
            assertEquals(Money.of(150.00, "ZAR"), result);
        }
        
        @Test
        @DisplayName("Should subtract money amounts")
        void shouldSubtractMoneyAmounts() {
            Money a = Money.of(100.00, "ZAR");
            Money b = Money.of(30.00, "ZAR");
            Money result = a.subtract(b);
            
            assertEquals(Money.of(70.00, "ZAR"), result);
        }
        
        @Test
        @DisplayName("Should reject operations with different currencies")
        void shouldRejectOperationsWithDifferentCurrencies() {
            Money zar = Money.of(100.00, "ZAR");
            Money usd = Money.of(100.00, "USD");
            
            assertThrows(IllegalArgumentException.class, () -> {
                zar.add(usd);
            });
        }
        
        @Test
        @DisplayName("Should compare money amounts")
        void shouldCompareMoneyAmounts() {
            Money hundred = Money.of(100.00, "ZAR");
            Money fifty = Money.of(50.00, "ZAR");
            
            assertTrue(hundred.isGreaterThan(fifty));
            assertTrue(fifty.isLessThan(hundred));
            assertFalse(hundred.isZero());
            assertTrue(Money.zero("ZAR").isZero());
        }
    }
}