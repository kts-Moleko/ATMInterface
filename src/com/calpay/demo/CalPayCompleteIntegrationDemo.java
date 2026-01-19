package com.calpay.demo;

import com.calpay.audit.AuditLogger;
import com.calpay.audit.InMemoryAuditLogger;
import com.calpay.core.enums.*;
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
 * Complete integration demo for CalPay Platform v2.0.
 * 
 * <p><b>Demo Scenario:</b>
 * <ol>
 *   <li>Initialize all repositories and services</li>
 *   <li>Register Sunnydale Primary School</li>
 *   <li>Authenticate as school admin</li>
 *   <li>Create fee structures (Tuition, Sport, Uniform)</li>
 *   <li>Enroll 3 students (different grades)</li>
 *   <li>Register 3 parents</li>
 *   <li>Process multiple payments</li>
 *   <li>Generate receipts and statements</li>
 *   <li>View payment statistics</li>
 *   <li>Display audit trail</li>
 * </ol>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public class CalPayCompleteIntegrationDemo {
    
    // Repositories
    private final SchoolRepository schoolRepo;
    private final SchoolAccountRepository schoolAccountRepo;
    private final StudentRepository studentRepo;
    private final ParentRepository parentRepo;
    private final FeeStructureRepository feeStructureRepo;
    private final PaymentRepository paymentRepo;
    private final SchoolAdminRepository adminRepo;
    
    // Services
    private final SchoolService schoolService;
    private final StudentService studentService;
    private final ParentService parentService;
    private final FeeStructureService feeStructureService;
    private final FeePaymentService feePaymentService;
    private final PaymentHistoryService paymentHistoryService;
    private final ReceiptService receiptService;
    private final SecurityService securityService;
    
    // Infrastructure
    private final AuditLogger auditLogger;
    
    // Demo data holders
    private School school;
    private List<Student> students;
    private List<Parent> parents;
    private List<FeeStructure> feeStructures;
    private SecurityContext adminContext;
    
    public CalPayCompleteIntegrationDemo() {
        System.out.println("Initializing CalPay Platform Demo...\n");
        
        // Initialize audit logger
        this.auditLogger = new InMemoryAuditLogger();
        
        // Initialize repositories (in-memory for demo)
        this.schoolRepo = new InMemorySchoolRepository();
        this.schoolAccountRepo = new InMemorySchoolAccountRepository();
        this.studentRepo = new InMemoryStudentRepository();
        this.parentRepo = new InMemoryParentRepository();
        this.feeStructureRepo = new InMemoryFeeStructureRepository();
        this.paymentRepo = new InMemoryPaymentRepository();
        this.adminRepo = new InMemorySchoolAdminRepository();
        
        // Initialize security
        this.securityService = new InMemorySecurityService(auditLogger);
        
        // Initialize services
        this.schoolService = new SchoolService(schoolRepo, schoolAccountRepo, auditLogger);
        this.studentService = new StudentService(
            studentRepo, schoolRepo, schoolAccountRepo, schoolService, auditLogger
        );
        this.parentService = new ParentService(parentRepo, schoolRepo, auditLogger);
        this.feeStructureService = new FeeStructureService(feeStructureRepo, schoolRepo, auditLogger);
        this.feePaymentService = new FeePaymentService(
            paymentRepo, parentRepo, studentRepo, feeStructureRepo, schoolAccountRepo, auditLogger
        );
        this.paymentHistoryService = new PaymentHistoryService(paymentRepo);
        this.receiptService = new ReceiptService(paymentRepo, parentRepo, studentRepo, schoolRepo);
        
        System.out.println("✓ All repositories and services initialized\n");
    }
    
    public void runCompleteDemo() {
        printHeader("CalPay Platform v2.0 - Complete Integration Demo");
        
        try {
            // Step 1: Register School
            step1_RegisterSchool();
            
            // Step 2: Authenticate Admin
            step2_AuthenticateAdmin();
            
            // Step 3: Create Fee Structures
            step3_CreateFeeStructures();
            
            // Step 4: Enroll Students
            step4_EnrollStudents();
            
            // Step 5: Register Parents
            step5_RegisterParents();
            
            // Step 6: Process Payments
            step6_ProcessPayments();
            
            // Step 7: Generate Receipts
            step7_GenerateReceipts();
            
            // Step 8: View Statistics
            step8_ViewStatistics();
            
            // Step 9: Generate Student Statement
            step9_GenerateStudentStatement();
            
            // Step 10: View Audit Trail
            step10_ViewAuditTrail();
            
            // Final Summary
            printFinalSummary();
            
        } catch (Exception e) {
            System.err.println("\n❌ ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void step1_RegisterSchool() {
        printStepHeader(1, "Register School");
        
        school = schoolService.registerSchool(
            "Sunnydale Primary School",
            "admin@sunnydale.co.za",
            "+27821234567"
        );
        
        System.out.println("✓ School registered successfully");
        System.out.println("  Code: " + school.getSchoolCode());
        System.out.println("  Name: " + school.getName());
        System.out.println("  Email: " + school.getEmail());
        System.out.println("  Subscription: " + school.getSubscriptionStatus() + " (30-day trial)");
        System.out.println("  Status: " + school.getStatus());
        System.out.println();
    }
    
    private void step2_AuthenticateAdmin() {
        printStepHeader(2, "Authenticate School Administrator");
        
        // In demo, we use pre-configured admin from InMemorySecurityService
        adminContext = securityService.authenticate("school_admin", "school123", "192.168.1.100");
        
        System.out.println("✓ Authentication successful");
        System.out.println("  Username: " + adminContext.getUsername());
        System.out.println("  Role: " + adminContext.getRole());
        System.out.println("  Session ID: " + adminContext.getSessionId());
        System.out.println("  Tenant: " + adminContext.getTenantId());
        System.out.println("  Permissions: " + adminContext.getPermissions().size());
        System.out.println();
    }
    
    private void step3_CreateFeeStructures() {
        printStepHeader(3, "Create Fee Structures");
        
        // Create Tuition Fee (Monthly)
        FeeStructure tuitionGrade10 = new FeeStructure.Builder()
            .feeId(UUID.randomUUID().toString())
            .schoolId(school.getSchoolId())
            .feeType("TUITION")
            .feeName("Grade 10 Tuition")
            .description("Monthly tuition fee for Grade 10 students")
            .amount(Money.of(2500.00, "ZAR"))
            .frequency("MONTHLY")
            .dueDayOfMonth(5)
            .gradeLevel("Grade 10")
            .isMandatory(true)
            .isActive(true)
            .build();
        feeStructureRepo.save(tuitionGrade10);
        
        // Create Sport Fee (Quarterly)
        FeeStructure sportFee = new FeeStructure.Builder()
            .feeId(UUID.randomUUID().toString())
            .schoolId(school.getSchoolId())
            .feeType("SPORT")
            .feeName("Sport Activities")
            .description("Quarterly sport and physical education fee")
            .amount(Money.of(450.00, "ZAR"))
            .frequency("QUARTERLY")
            .gradeLevel("All Grades")
            .isMandatory(false)
            .isActive(true)
            .build();
        feeStructureRepo.save(sportFee);
        
        // Create Uniform Fee (One-time)
        FeeStructure uniformFee = new FeeStructure.Builder()
            .feeId(UUID.randomUUID().toString())
            .schoolId(school.getSchoolId())
            .feeType("UNIFORM")
            .feeName("School Uniform")
            .description("Complete school uniform set")
            .amount(Money.of(850.00, "ZAR"))
            .frequency("ONE_TIME")
            .gradeLevel("All Grades")
            .isMandatory(true)
            .isActive(true)
            .build();
        feeStructureRepo.save(uniformFee);
        
        feeStructures = List.of(tuitionGrade10, sportFee, uniformFee);
        
        System.out.println("✓ Created 3 fee structures:");
        for (FeeStructure fee : feeStructures) {
            System.out.printf("  • %s - %s (%s, %s)%n",
                fee.getFeeName(),
                fee.getAmount(),
                fee.getFrequency(),
                fee.isMandatory() ? "Mandatory" : "Optional"
            );
        }
        System.out.println();
    }
    
    private void step4_EnrollStudents() {
        printStepHeader(4, "Enroll Students");
        
        Student john = studentService.enrollStudent(
            school.getSchoolId(),
            "John",
            "Doe",
            "Grade 10",
            LocalDate.of(2010, 5, 15)
        );
        
        Student sarah = studentService.enrollStudent(
            school.getSchoolId(),
            "Sarah",
            "Smith",
            "Grade 9",
            LocalDate.of(2011, 8, 22)
        );
        
        Student michael = studentService.enrollStudent(
            school.getSchoolId(),
            "Michael",
            "Johnson",
            "Grade 10",
            LocalDate.of(2010, 3, 10)
        );
        
        // Apply scholarship to Michael
        studentService.applyScholarship(michael.getStudentId(), 25);
        michael = studentService.getStudent(michael.getStudentId());
        
        students = List.of(john, sarah, michael);
        
        System.out.println("✓ Enrolled 3 students:");
        for (Student student : students) {
            System.out.printf("  • %s (%s) - %s%s%n",
                student.getFullName(),
                student.getStudentCode(),
                student.getGradeLevel(),
                student.hasScholarship() ? String.format(" [%d%% Scholarship]", student.getScholarshipPercentage()) : ""
            );
        }
        System.out.println();
    }
    
    private void step5_RegisterParents() {
        printStepHeader(5, "Register Parents/Guardians");
        
        Parent janeDoe = parentService.registerParent(
            school.getSchoolId(),
            "Jane",
            "Doe",
            "+27821234567",
            "jane.doe@email.com"
        );
        
        Parent robertSmith = parentService.registerParent(
            school.getSchoolId(),
            "Robert",
            "Smith",
            "+27829876543",
            "robert.smith@email.com"
        );
        
        Parent lisaJohnson = parentService.registerParent(
            school.getSchoolId(),
            "Lisa",
            "Johnson",
            "+27825551234",
            "lisa.johnson@email.com"
        );
        
        // Verify parent phone numbers
        parentService.verifyPhone(janeDoe.getParentId());
        parentService.verifyPhone(robertSmith.getParentId());
        parentService.verifyPhone(lisaJohnson.getParentId());
        
        parents = List.of(janeDoe, robertSmith, lisaJohnson);
        
        System.out.println("✓ Registered 3 parents:");
        for (Parent parent : parents) {
            System.out.printf("  • %s (%s) - %s [%s]%n",
                parent.getFullName(),
                parent.getParentCode(),
                parent.getPhone(),
                parent.isPhoneVerified() ? "Verified" : "Unverified"
            );
        }
        System.out.println();
    }
    
    private void step6_ProcessPayments() {
        printStepHeader(6, "Process Fee Payments");
        
        int paymentCount = 0;
        
        // Jane Doe pays tuition for John Doe
        Payment payment1 = feePaymentService.processPayment(
            parents.get(0).getParentId(),
            students.get(0).getStudentId(),
            feeStructures.get(0).getFeeId(),
            Money.of(2500.00, "ZAR"),
            PaymentMethod.INSTANT_EFT,
            UUID.randomUUID().toString()
        );
        paymentCount++;
        System.out.println("✓ Payment 1: " + payment1.getPaymentReference() + " - " + payment1.getAmount());
        
        // Robert Smith pays tuition for Sarah Smith
        Payment payment2 = feePaymentService.processPayment(
            parents.get(1).getParentId(),
            students.get(1).getStudentId(),
            feeStructures.get(0).getFeeId(),
            Money.of(2500.00, "ZAR"),
            PaymentMethod.CARD,
            UUID.randomUUID().toString()
        );
        paymentCount++;
        System.out.println("✓ Payment 2: " + payment2.getPaymentReference() + " - " + payment2.getAmount());
        
        // Lisa Johnson pays tuition for Michael (with 25% scholarship)
        Money discountedTuition = students.get(2).applyScholarship(feeStructures.get(0).getAmount());
        Payment payment3 = feePaymentService.processPayment(
            parents.get(2).getParentId(),
            students.get(2).getStudentId(),
            feeStructures.get(0).getFeeId(),
            discountedTuition,
            PaymentMethod.INSTANT_EFT,
            UUID.randomUUID().toString()
        );
        paymentCount++;
        System.out.println("✓ Payment 3: " + payment3.getPaymentReference() + " - " + payment3.getAmount() + " (25% scholarship applied)");
        
        // Jane Doe pays uniform for John
        Payment payment4 = feePaymentService.processPayment(
            parents.get(0).getParentId(),
            students.get(0).getStudentId(),
            feeStructures.get(2).getFeeId(),
            Money.of(850.00, "ZAR"),
            PaymentMethod.INSTANT_EFT,
            UUID.randomUUID().toString()
        );
        paymentCount++;
        System.out.println("✓ Payment 4: " + payment4.getPaymentReference() + " - " + payment4.getAmount());
        
        // Robert Smith pays sport fee for Sarah
        Payment payment5 = feePaymentService.processPayment(
            parents.get(1).getParentId(),
            students.get(1).getStudentId(),
            feeStructures.get(1).getFeeId(),
            Money.of(450.00, "ZAR"),
            PaymentMethod.CASH,
            UUID.randomUUID().toString()
        );
        paymentCount++;
        System.out.println("✓ Payment 5: " + payment5.getPaymentReference() + " - " + payment5.getAmount());
        
        System.out.println("\n  Total payments processed: " + paymentCount);
        System.out.println();
    }
    
    private void step7_GenerateReceipts() {
        printStepHeader(7, "Generate Payment Receipts");
        
        List<Payment> recentPayments = paymentHistoryService.getRecentPayments(school.getSchoolId(), 2);
        
        System.out.println("Sample Receipt (Most Recent Payment):");
        System.out.println("-".repeat(70));
        
        if (!recentPayments.isEmpty()) {
            String receipt = receiptService.generateReceipt(recentPayments.get(0).getPaymentId());
            System.out.println(receipt);
        }
        
        System.out.println();
    }
    
    private void step8_ViewStatistics() {
        printStepHeader(8, "Payment Statistics & Analytics");
        
        PaymentHistoryService.PaymentStatistics stats = paymentHistoryService.getStatistics(school.getSchoolId());
        
        System.out.println("School Payment Statistics:");
        System.out.println("  Total Payments: " + stats.getTotalPayments());
        System.out.println("  Completed: " + stats.getCompletedPayments());
        System.out.println("  Pending: " + stats.getPendingPayments());
        System.out.println("  Failed: " + stats.getFailedPayments());
        System.out.println("  Success Rate: " + String.format("%.1f%%", stats.getSuccessRate()));
        System.out.println();
        System.out.println("Revenue:");
        System.out.println("  Total Collected: " + stats.getTotalCollected());
        System.out.println("  Monthly Collection: " + stats.getMonthlyCollection());
        System.out.println();
        
        // Payment method breakdown
        List<Payment> allPayments = paymentHistoryService.getSchoolPayments(school.getSchoolId());
        long eftCount = allPayments.stream().filter(p -> "INSTANT_EFT".equals(p.getPaymentMethod())).count();
        long cardCount = allPayments.stream().filter(p -> "CARD".equals(p.getPaymentMethod())).count();
        long cashCount = allPayments.stream().filter(p -> "CASH".equals(p.getPaymentMethod())).count();
        
        System.out.println("Payment Methods:");
        System.out.println("  Instant EFT: " + eftCount);
        System.out.println("  Card: " + cardCount);
        System.out.println("  Cash: " + cashCount);
        System.out.println();
    }
    
    private void step9_GenerateStudentStatement() {
        printStepHeader(9, "Generate Student Account Statement");
        
        if (!students.isEmpty()) {
            String statement = receiptService.generateStudentStatement(students.get(0).getStudentId());
            System.out.println(statement);
        }
    }
    
    private void step10_ViewAuditTrail() {
        printStepHeader(10, "Audit Trail");
        
        if (auditLogger instanceof InMemoryAuditLogger) {
            InMemoryAuditLogger memoryLogger = (InMemoryAuditLogger) auditLogger;
            
            System.out.println("Audit Summary:");
            System.out.println("  Total Events: " + memoryLogger.size());
            System.out.println();
            System.out.println("Recent Events:");
            System.out.println("-".repeat(100));
            System.out.printf("%-25s %-30s %-40s%n", "Timestamp", "Event Type", "Description");
            System.out.println("-".repeat(100));
            
            memoryLogger.getAllEvents().stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(15)
                .forEach(event -> {
                    System.out.printf("%-25s %-30s %-40s%n",
                        DateUtils.formatDateTime(event.getTimestamp()),
                        StringUtils.truncate(event.getEventType(), 30),
                        StringUtils.truncate(event.getDescription(), 40)
                    );
                });
            
            System.out.println();
        }
    }
    
    private void printFinalSummary() {
        printHeader("Demo Completed Successfully! 🎉");
        
        System.out.println("Summary:");
        System.out.println("  Schools Registered: 1");
        System.out.println("  Students Enrolled: " + students.size());
        System.out.println("  Parents Registered: " + parents.size());
        System.out.println("  Fee Structures Created: " + feeStructures.size());
        System.out.println("  Payments Processed: " + paymentHistoryService.getSchoolPayments(school.getSchoolId()).size());
        System.out.println();
        
        System.out.println("School Status:");
        System.out.println("  Name: " + school.getName());
        System.out.println("  Subscription: " + school.getSubscriptionStatus());
        System.out.println("  Total Students: " + studentService.getActiveStudents(school.getSchoolId()).size());
        System.out.println("  Total Revenue: " + paymentHistoryService.calculateTotalCollected(school.getSchoolId()));
        System.out.println();
        
        System.out.println("Next Steps:");
        System.out.println("  • Explore CLI interface: java -jar calpay-cli.jar");
        System.out.println("  • Run integration tests: mvn test");
        System.out.println("  • Deploy to production: Configure JDBC repositories");
        System.out.println();
    }
    
    // Utility methods for formatted output
    
    private void printHeader(String title) {
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println(" ".repeat((70 - title.length()) / 2) + title);
        System.out.println("=".repeat(70));
        System.out.println();
    }
    
    private void printStepHeader(int stepNumber, String stepName) {
        System.out.println("\n" + "─".repeat(70));
        System.out.printf("STEP %d: %s%n", stepNumber, stepName);
        System.out.println("─".repeat(70));
        System.out.println();
    }
    
    public static void main(String[] args) {
        CalPayCompleteIntegrationDemo demo = new CalPayCompleteIntegrationDemo();
        demo.runCompleteDemo();
    }
}