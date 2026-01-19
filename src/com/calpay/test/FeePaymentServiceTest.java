package com.calpay.test;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import com.calpay.core.model.*;
import com.calpay.core.enums.*;
import com.calpay.core.exceptions.*;
import com.calpay.service.*;
import com.calpay.repository.*;
import com.calpay.repository.impl.*;
import com.calpay.audit.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Integration tests for FeePaymentService.
 * 
 * <p><b>Test Scenarios:</b>
 * <ul>
 *   <li>Happy path: Successful payment processing</li>
 *   <li>Idempotency: Duplicate payment prevention</li>
 *   <li>Validation: Invalid inputs</li>
 *   <li>Authorization: Tenant access control</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
@DisplayName("Fee Payment Service Integration Tests")
class FeePaymentServiceTest {
    
    private FeePaymentService paymentService;
    private ParentRepository parentRepo;
    private StudentRepository studentRepo;
    private FeeStructureRepository feeRepo;
    private PaymentRepository paymentRepo;
    private SchoolAccountRepository accountRepo;
    private AuditLogger auditLogger;
    
    private String schoolId;
    private String parentId;
    private String studentId;
    private String feeId;
    
    @BeforeEach
    void setUp() {
        // Initialize repositories
        parentRepo = new InMemoryParentRepository();
        studentRepo = new InMemoryStudentRepository();
        feeRepo = new InMemoryFeeStructureRepository();
        paymentRepo = new InMemoryPaymentRepository();
        accountRepo = new InMemorySchoolAccountRepository();
        auditLogger = new InMemoryAuditLogger();
        
        // Initialize service
        paymentService = new FeePaymentService(
            paymentRepo, parentRepo, studentRepo, feeRepo, accountRepo, auditLogger
        );
        
        // Create test data
        setupTestData();
    }
    
    private void setupTestData() {
        schoolId = UUID.randomUUID().toString();
        
        // Create school account
        SchoolAccount account = new SchoolAccount.Builder()
            .accountId(UUID.randomUUID().toString())
            .schoolId(schoolId)
            .accountNumber("1234567890")
            .status(AccountStatus.ACTIVE)
            .build();
        accountRepo.save(account);
        
        // Create parent
        parentId = UUID.randomUUID().toString();
        Parent parent = new Parent.Builder()
            .parentId(parentId)
            .schoolId(schoolId)
            .parentCode("PAR001")
            .firstName("Jane")
            .lastName("Doe")
            .phone("+27821234567")
            .email("jane@example.com")
            .build();
        parentRepo.save(parent);
        
        // Create student
        studentId = UUID.randomUUID().toString();
        Student student = new Student.Builder()
            .studentId(studentId)
            .schoolId(schoolId)
            .studentCode("STU001")
            .firstName("John")
            .lastName("Doe")
            .gradeLevel("Grade 10")
            .admissionDate(LocalDate.now())
            .build();
        studentRepo.save(student);
        
        // Create fee structure
        feeId = UUID.randomUUID().toString();
        FeeStructure fee = new FeeStructure.Builder()
            .feeId(feeId)
            .schoolId(schoolId)
            .feeType("TUITION")
            .feeName("Grade 10 Tuition")
            .amount(Money.of(500.00, "ZAR"))
            .frequency("MONTHLY")
            .dueDayOfMonth(5)
            .gradeLevel("Grade 10")
            .build();
        feeRepo.save(fee);
    }
    
    @Test
    @DisplayName("Should process payment successfully")
    void shouldProcessPayment() {
        // Arrange
        Money amount = Money.of(500.00, "ZAR");
        String idempotencyKey = UUID.randomUUID().toString();
        
        // Act
        Payment payment = paymentService.processPayment(
            parentId,
            studentId,
            feeId,
            amount,
            PaymentMethod.INSTANT_EFT,
            idempotencyKey
        );
        
        // Assert
        assertNotNull(payment);
        assertEquals(parentId, payment.getParentId());
        assertEquals(studentId, payment.getStudentId());
        assertEquals(amount, payment.getAmount());
        assertEquals("COMPLETED", payment.getPaymentStatus());
        assertNotNull(payment.getPaymentReference());
        
        // Verify audit log
        if (auditLogger instanceof InMemoryAuditLogger) {
            InMemoryAuditLogger memoryLogger = (InMemoryAuditLogger) auditLogger;
            assertTrue(memoryLogger.size() > 0);
        }
    }
    
    @Test
    @DisplayName("Should prevent duplicate payments with idempotency")
    void shouldPreventDuplicatePayments() {
        // Arrange
        Money amount = Money.of(500.00, "ZAR");
        String idempotencyKey = UUID.randomUUID().toString();
        
        // Act - First payment
        Payment firstPayment = paymentService.processPayment(
            parentId, studentId, feeId, amount, PaymentMethod.INSTANT_EFT, idempotencyKey
        );
        
        // Act - Duplicate payment attempt
        DuplicatePaymentException exception = assertThrows(
            DuplicatePaymentException.class,
            () -> paymentService.processPayment(
                parentId, studentId, feeId, amount, PaymentMethod.INSTANT_EFT, idempotencyKey
            )
        );
        
        // Assert
        assertNotNull(exception);
        assertEquals(idempotencyKey, exception.getIdempotencyKey());
        assertEquals(firstPayment.getPaymentId(), exception.getExistingPaymentId());
    }
    
    @Test
    @DisplayName("Should reject payment with invalid parent")
    void shouldRejectInvalidParent() {
        // Arrange
        String invalidParentId = UUID.randomUUID().toString();
        Money amount = Money.of(500.00, "ZAR");
        String idempotencyKey = UUID.randomUUID().toString();
        
        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            paymentService.processPayment(
                invalidParentId, studentId, feeId, amount, 
                PaymentMethod.INSTANT_EFT, idempotencyKey
            );
        });
    }
    
    @Test
    @DisplayName("Should calculate total paid for student")
    void shouldCalculateTotalPaid() {
        // Arrange - Make 3 payments
        for (int i = 0; i < 3; i++) {
            paymentService.processPayment(
                parentId,
                studentId,
                feeId,
                Money.of(500.00, "ZAR"),
                PaymentMethod.INSTANT_EFT,
                UUID.randomUUID().toString()
            );
        }
        
        // Act
        Money totalPaid = paymentService.calculateTotalPaid(studentId);
        
        // Assert
        assertEquals(Money.of(1500.00, "ZAR"), totalPaid);
    }
    
    @Test
    @DisplayName("Should retrieve payment by reference")
    void shouldRetrievePaymentByReference() {
        // Arrange
        Payment payment = paymentService.processPayment(
            parentId, studentId, feeId, Money.of(500.00, "ZAR"),
            PaymentMethod.INSTANT_EFT, UUID.randomUUID().toString()
        );
        
        // Act
        Payment retrieved = paymentService.getPaymentByReference(payment.getPaymentReference());
        
        // Assert
        assertNotNull(retrieved);
        assertEquals(payment.getPaymentId(), retrieved.getPaymentId());
        assertEquals(payment.getPaymentReference(), retrieved.getPaymentReference());
    }
}