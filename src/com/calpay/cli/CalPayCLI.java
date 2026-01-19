package com.calpay.cli;

import com.calpay.audit.AuditLogger;
import com.calpay.audit.InMemoryAuditLogger;
import com.calpay.core.enums.PaymentMethod;
import com.calpay.core.model.*;
import com.calpay.repository.*;
import com.calpay.repository.impl.*;
import com.calpay.security.InMemorySecurityService;
import com.calpay.security.SecurityContext;
import com.calpay.security.SecurityService;
import com.calpay.service.*;
import com.calpay.util.DateUtils;
import com.calpay.util.StringUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Command-line interface for CalPay platform.
 * 
 * <p><b>Commands:</b>
 * <ul>
 *   <li>school register - Register a new school</li>
 *   <li>school list - List all schools</li>
 *   <li>student enroll - Enroll a student</li>
 *   <li>student list - List students</li>
 *   <li>parent register - Register a parent</li>
 *   <li>payment process - Process a payment</li>
 *   <li>payment history - View payment history</li>
 *   <li>receipt generate - Generate receipt</li>
 *   <li>audit view - View audit trail</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public class CalPayCLI {
    
    private final Scanner scanner;
    private final SchoolService schoolService;
    private final StudentService studentService;
    private final ParentService parentService;
    private final FeeStructureService feeStructureService;
    private final FeePaymentService feePaymentService;
    private final PaymentHistoryService paymentHistoryService;
    private final ReceiptService receiptService;
    private final SecurityService securityService;
    private final AuditLogger auditLogger;
    
    private SecurityContext currentUser;
    
    public CalPayCLI() {
        this.scanner = new Scanner(System.in);
        
        // Initialize repositories (in-memory for CLI demo)
        SchoolRepository schoolRepo = new InMemorySchoolRepository();
        SchoolAccountRepository accountRepo = new InMemorySchoolAccountRepository();
        StudentRepository studentRepo = new InMemoryStudentRepository();
        ParentRepository parentRepo = new InMemoryParentRepository();
        FeeStructureRepository feeRepo = new InMemoryFeeStructureRepository();
        PaymentRepository paymentRepo = new InMemoryPaymentRepository();
        
        // Initialize audit logger
        this.auditLogger = new InMemoryAuditLogger();
        
        // Initialize services
        this.schoolService = new SchoolService(schoolRepo, accountRepo, auditLogger);
        this.studentService = new StudentService(
            studentRepo, schoolRepo, accountRepo, schoolService, auditLogger
        );
        this.parentService = new ParentService(parentRepo, schoolRepo, auditLogger);
        this.feeStructureService = new FeeStructureService(feeRepo, schoolRepo, auditLogger);
        this.feePaymentService = new FeePaymentService(
            paymentRepo, parentRepo, studentRepo, feeRepo, accountRepo, auditLogger
        );
        this.paymentHistoryService = new PaymentHistoryService(paymentRepo);
        this.receiptService = new ReceiptService(paymentRepo, parentRepo, studentRepo, schoolRepo);
        this.securityService = new InMemorySecurityService(auditLogger);
    }
    
    public void run() {
        printWelcome();
        
        while (true) {
            try {
                printMenu();
                String command = scanner.nextLine().trim().toLowerCase();
                
                if (command.equals("exit") || command.equals("quit")) {
                    System.out.println("Goodbye!");
                    break;
                }
                
                handleCommand(command);
                
            } catch (Exception e) {
                System.err.println("ERROR: " + e.getMessage());
                if (Boolean.getBoolean("debug")) {
                    e.printStackTrace();
                }
            }
        }
        
        scanner.close();
    }
    
    private void printWelcome() {
        System.out.println();
        System.out.println("======================================================================");
        System.out.println("               CalPay School Fee Payment System");
        System.out.println("                         CLI Interface v2.0");
        System.out.println("======================================================================");
        System.out.println();
    }
    
    private void printMenu() {
        System.out.println("\nAvailable Commands:");
        System.out.println("  1. login         - Login to system");
        System.out.println("  2. school        - School management");
        System.out.println("  3. student       - Student management");
        System.out.println("  4. parent        - Parent management");
        System.out.println("  5. fee           - Fee structure management");
        System.out.println("  6. payment       - Payment processing");
        System.out.println("  7. receipt       - Receipt generation");
        System.out.println("  8. audit         - Audit trail viewer");
        System.out.println("  9. help          - Show detailed help");
        System.out.println("  0. exit          - Exit application");
        System.out.print("\nEnter command: ");
    }
    
    private void handleCommand(String command) {
        switch (command) {
            case "1":
            case "login":
                handleLogin();
                break;
            case "2":
            case "school":
                handleSchoolCommands();
                break;
            case "3":
            case "student":
                handleStudentCommands();
                break;
            case "4":
            case "parent":
                handleParentCommands();
                break;
            case "5":
            case "fee":
                handleFeeCommands();
                break;
            case "6":
            case "payment":
                handlePaymentCommands();
                break;
            case "7":
            case "receipt":
                handleReceiptCommands();
                break;
            case "8":
            case "audit":
                handleAuditCommands();
                break;
            case "9":
            case "help":
                printDetailedHelp();
                break;
            default:
                System.out.println("Unknown command. Type 'help' for options.");
                break;
        }
    }
    
    // ========== Login ==========
    
    private void handleLogin() {
        System.out.println("\n--- Login ---");
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        
        try {
            currentUser = securityService.authenticate(username, password, "127.0.0.1");
            System.out.println("✓ Login successful!");
            System.out.println("  User: " + currentUser.getUsername());
            System.out.println("  Role: " + currentUser.getRole());
            System.out.println("  Session: " + currentUser.getSessionId());
        } catch (Exception e) {
            System.err.println("✗ Login failed: " + e.getMessage());
        }
    }
    
    // ========== School Management ==========
    
    private void handleSchoolCommands() {
        System.out.println("\n--- School Management ---");
        System.out.println("1. Register new school");
        System.out.println("2. List all schools");
        System.out.println("3. View school details");
        System.out.print("Select option: ");
        
        String option = scanner.nextLine().trim();
        
        switch (option) {
            case "1":
                registerSchool();
                break;
            case "2":
                listSchools();
                break;
            case "3":
                viewSchoolDetails();
                break;
            default:
                System.out.println("Invalid option");
                break;
        }
    }
    
    private void registerSchool() {
        System.out.println("\n--- Register School ---");
        
        System.out.print("School name: ");
        String name = scanner.nextLine().trim();
        
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        
        System.out.print("Phone: ");
        String phone = scanner.nextLine().trim();
        
        try {
            School school = schoolService.registerSchool(name, email, phone);
            
            System.out.println("\n✓ School registered successfully!");
            System.out.println("  Code: " + school.getSchoolCode());
            System.out.println("  Name: " + school.getName());
            System.out.println("  Email: " + school.getEmail());
            System.out.println("  Subscription: " + school.getSubscriptionStatus());
            
        } catch (Exception e) {
            System.err.println("✗ Registration failed: " + e.getMessage());
        }
    }
    
    private void listSchools() {
        System.out.println("\n--- Schools ---");
        
        List<School> schools = schoolService.getAllActiveSchools();
        
        if (schools.isEmpty()) {
            System.out.println("No schools registered.");
            return;
        }
        
        System.out.printf("%-10s %-30s %-20s %-15s%n", 
            "Code", "Name", "Email", "Subscription");
        System.out.println("--------------------------------------------------------------------------------");
        
        for (School school : schools) {
            System.out.printf("%-10s %-30s %-20s %-15s%n",
                school.getSchoolCode(),
                StringUtils.truncate(school.getName(), 30),
                StringUtils.truncate(school.getEmail(), 20),
                school.getSubscriptionStatus()
            );
        }
    }
    
    private void viewSchoolDetails() {
        System.out.print("\nSchool code: ");
        String code = scanner.nextLine().trim();
        
        try {
            School school = schoolService.getSchoolByCode(code);
            
            System.out.println("\n--- School Details ---");
            System.out.println("Code: " + school.getSchoolCode());
            System.out.println("Name: " + school.getName());
            System.out.println("Email: " + school.getEmail());
            System.out.println("Phone: " + school.getPhone());
            System.out.println("Subscription: " + school.getSubscriptionStatus());
            System.out.println("Students: " + school.getTotalStudents());
            System.out.println("Status: " + (school.isActive() ? "Active" : "Inactive"));
            
        } catch (Exception e) {
            System.err.println("✗ School not found: " + e.getMessage());
        }
    }
    
    // ========== Student Management ==========
    
    private void handleStudentCommands() {
        System.out.println("\n--- Student Management ---");
        System.out.println("1. Enroll new student");
        System.out.println("2. List students");
        System.out.println("3. View student details");
        System.out.print("Select option: ");
        
        String option = scanner.nextLine().trim();
        
        switch (option) {
            case "1":
                enrollStudent();
                break;
            case "2":
                listStudents();
                break;
            case "3":
                viewStudentDetails();
                break;
            default:
                System.out.println("Invalid option");
                break;
        }
    }
    
    private void enrollStudent() {
        System.out.println("\n--- Enroll Student ---");
        
        System.out.print("School code: ");
        String schoolCode = scanner.nextLine().trim();
        
        try {
            School school = schoolService.getSchoolByCode(schoolCode);
            
            System.out.print("First name: ");
            String firstName = scanner.nextLine().trim();
            
            System.out.print("Last name: ");
            String lastName = scanner.nextLine().trim();
            
            System.out.print("Grade level (e.g., Grade 10): ");
            String gradeLevel = scanner.nextLine().trim();
            
            System.out.print("Date of birth (yyyy-MM-dd): ");
            String dobStr = scanner.nextLine().trim();
            LocalDate dob = LocalDate.parse(dobStr);
            
            Student student = studentService.enrollStudent(
                school.getSchoolId(), firstName, lastName, gradeLevel, dob
            );
            
            System.out.println("\n✓ Student enrolled successfully!");
            System.out.println("  Code: " + student.getStudentCode());
            System.out.println("  Name: " + student.getFullName());
            System.out.println("  Grade: " + student.getGradeLevel());
            
        } catch (Exception e) {
            System.err.println("✗ Enrollment failed: " + e.getMessage());
        }
    }
    
    private void listStudents() {
        System.out.print("\nSchool code: ");
        String schoolCode = scanner.nextLine().trim();
        
        try {
            School school = schoolService.getSchoolByCode(schoolCode);
            List<Student> students = studentService.getActiveStudents(school.getSchoolId());
            
            if (students.isEmpty()) {
                System.out.println("No students enrolled.");
                return;
            }
            
            System.out.println("\n--- Students ---");
            System.out.printf("%-10s %-30s %-15s %-10s%n", 
                "Code", "Name", "Grade", "Status");
            System.out.println("----------------------------------------------------------------------");
            
            for (Student student : students) {
                System.out.printf("%-10s %-30s %-15s %-10s%n",
                    student.getStudentCode(),
                    StringUtils.truncate(student.getFullName(), 30),
                    student.getGradeLevel(),
                    student.isActive() ? "Active" : "Inactive"
                );
            }
            
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
        }
    }
    
    private void viewStudentDetails() {
        System.out.print("\nSchool code: ");
        String schoolCode = scanner.nextLine().trim();
        
        System.out.print("Student code: ");
        String studentCode = scanner.nextLine().trim();
        
        try {
            School school = schoolService.getSchoolByCode(schoolCode);
            Student student = studentService.getStudentByCode(school.getSchoolId(), studentCode);
            
            System.out.println("\n--- Student Details ---");
            System.out.println("Code: " + student.getStudentCode());
            System.out.println("Name: " + student.getFullName());
            System.out.println("Grade: " + student.getGradeLevel());
            System.out.println("Admission Date: " + DateUtils.formatDate(student.getAdmissionDate()));
            System.out.println("Scholarship: " + (student.hasScholarship() ? 
                student.getScholarshipPercentage() + "%" : "None"));
            System.out.println("Status: " + (student.isActive() ? "Active" : "Inactive"));
            
        } catch (Exception e) {
            System.err.println("✗ Student not found: " + e.getMessage());
        }
    }
    
    // ========== Parent Management ==========
    
    private void handleParentCommands() {
        System.out.println("\n--- Parent Management ---");
        System.out.println("1. Register new parent");
        System.out.println("2. List parents");
        System.out.print("Select option: ");
        
        String option = scanner.nextLine().trim();
        
        switch (option) {
            case "1":
                registerParent();
                break;
            case "2":
                listParents();
                break;
            default:
                System.out.println("Invalid option");
                break;
        }
    }
    
    private void registerParent() {
        System.out.println("\n--- Register Parent ---");
        
        System.out.print("School code: ");
        String schoolCode = scanner.nextLine().trim();
        
        try {
            School school = schoolService.getSchoolByCode(schoolCode);
            
            System.out.print("First name: ");
            String firstName = scanner.nextLine().trim();
            
            System.out.print("Last name: ");
            String lastName = scanner.nextLine().trim();
            
            System.out.print("Phone: ");
            String phone = scanner.nextLine().trim();
            
            System.out.print("Email (optional): ");
            String email = scanner.nextLine().trim();
            if (email.isEmpty()) email = null;
            
            Parent parent = parentService.registerParent(
                school.getSchoolId(), firstName, lastName, phone, email
            );
            
            System.out.println("\n✓ Parent registered successfully!");
            System.out.println("  Code: " + parent.getParentCode());
            System.out.println("  Name: " + parent.getFullName());
            System.out.println("  Phone: " + parent.getPhone());
            
        } catch (Exception e) {
            System.err.println("✗ Registration failed: " + e.getMessage());
        }
    }
    
    private void listParents() {
        System.out.print("\nSchool code: ");
        String schoolCode = scanner.nextLine().trim();
        
        try {
            School school = schoolService.getSchoolByCode(schoolCode);
            List<Parent> parents = parentService.getActiveParents(school.getSchoolId());
            
            if (parents.isEmpty()) {
                System.out.println("No parents registered.");
                return;
            }
            
            System.out.println("\n--- Parents ---");
            System.out.printf("%-10s %-30s %-20s%n", "Code", "Name", "Phone");
            System.out.println("-----------------------------------------------------------------");
            
            for (Parent parent : parents) {
                System.out.printf("%-10s %-30s %-20s%n",
                    parent.getParentCode(),
                    StringUtils.truncate(parent.getFullName(), 30),
                    parent.getPhone()
                );
            }
            
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
        }
    }
    
    // ========== Payment Processing ==========
    
    private void handlePaymentCommands() {
        System.out.println("\n--- Payment Management ---");
        System.out.println("1. Process new payment");
        System.out.println("2. View payment history");
        System.out.println("3. View payment statistics");
        System.out.print("Select option: ");
        
        String option = scanner.nextLine().trim();
        
        switch (option) {
            case "1":
                processPayment();
                break;
            case "2":
                viewPaymentHistory();
                break;
            case "3":
                viewPaymentStatistics();
                break;
            default:
                System.out.println("Invalid option");
                break;
        }
    }
    
    private void processPayment() {
        System.out.println("\n--- Process Payment ---");
        System.out.println("This is a demo - payment will be automatically approved");
        
        // Implementation would collect parent, student, fee, amount details
        // and call feePaymentService.processPayment()
        
        System.out.println("\n✓ Payment processed successfully!");
        System.out.println("  Reference: PAY-2025-01-001234");
        System.out.println("  Amount: ZAR 500.00");
        System.out.println("  Status: COMPLETED");
    }
    
    private void viewPaymentHistory() {
        System.out.print("\nSchool code: ");
        String schoolCode = scanner.nextLine().trim();
        
        try {
            School school = schoolService.getSchoolByCode(schoolCode);
            List<Payment> payments = paymentHistoryService.getSchoolPayments(school.getSchoolId());
            
            if (payments.isEmpty()) {
                System.out.println("No payments found.");
                return;
            }
            
            System.out.println("\n--- Payment History ---");
            System.out.printf("%-20s %-15s %-12s %-15s%n", 
                "Reference", "Date", "Amount", "Status");
            System.out.println("----------------------------------------------------------------------");
            
            List<Payment> limitedPayments = payments.stream()
                .limit(10)
                .collect(Collectors.toList());
            
            for (Payment payment : limitedPayments) {
                System.out.printf("%-20s %-15s %-12s %-15s%n",
                    payment.getPaymentReference(),
                    DateUtils.formatDateTime(payment.getTimestamp()),
                    payment.getAmount(),
                    payment.getPaymentStatus()
                );
            }
            
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
        }
    }
    
    private void viewPaymentStatistics() {
        System.out.print("\nSchool code: ");
        String schoolCode = scanner.nextLine().trim();
        
        try {
            School school = schoolService.getSchoolByCode(schoolCode);
            PaymentHistoryService.PaymentStatistics stats = 
                paymentHistoryService.getStatistics(school.getSchoolId());
            
            System.out.println("\n--- Payment Statistics ---");
            System.out.println("Total Payments: " + stats.getTotalPayments());
            System.out.println("Completed: " + stats.getCompletedPayments());
            System.out.println("Pending: " + stats.getPendingPayments());
            System.out.println("Failed: " + stats.getFailedPayments());
            System.out.println("Success Rate: " + String.format("%.1f%%", stats.getSuccessRate()));
            System.out.println("Total Collected: " + stats.getTotalCollected());
            System.out.println("Monthly Collection: " + stats.getMonthlyCollection());
            
        } catch (Exception e) {
            System.err.println("✗ Error: " + e.getMessage());
        }
    }
    
    // ========== Receipt Generation ==========
    
    private void handleReceiptCommands() {
        System.out.println("\n--- Receipt Generation ---");
        System.out.print("Payment reference: ");
        String reference = scanner.nextLine().trim();
        
        try {
            Payment payment = paymentHistoryService.getSchoolPayments("demo-school").stream()
                .filter(p -> p.getPaymentReference().equals(reference))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Payment not found"));
            
            String receipt = receiptService.generateReceipt(payment.getPaymentId());
            System.out.println("\n" + receipt);
            
        } catch (Exception e) {
            System.err.println("✗ Payment not found: " + e.getMessage());
        }
    }
    
    // ========== Fee Management ==========
    
    private void handleFeeCommands() {
        System.out.println("\n--- Fee Structure Management ---");
        System.out.println("1. Create fee structure");
        System.out.println("2. List fee structures");
        System.out.print("Select option: ");
        
        String option = scanner.nextLine().trim();
        
        switch (option) {
            case "1":
                createFeeStructure();
                break;
            case "2":
                listFeeStructures();
                break;
            default:
                System.out.println("Invalid option");
                break;
        }
    }
    
    private void createFeeStructure() {
        System.out.println("\n--- Create Fee Structure ---");
        System.out.println("Demo implementation - see service layer for full logic");
    }
    
    private void listFeeStructures() {
        System.out.println("\n--- Fee Structures ---");
        System.out.println("Demo implementation - see service layer for full logic");
    }
    
    // ========== Audit Trail ==========
    
    private void handleAuditCommands() {
        System.out.println("\n--- Audit Trail ---");
        
        if (auditLogger instanceof InMemoryAuditLogger) {
            InMemoryAuditLogger memoryLogger = (InMemoryAuditLogger) auditLogger;
            
            System.out.println("Total events: " + memoryLogger.size());
            System.out.println("\nRecent events:");
            System.out.printf("%-25s %-30s %-40s%n", "Timestamp", "Type", "Description");
            System.out.println("----------------------------------------------------------------------------------------------------");
            
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
        }
    }
    
    // ========== Help ==========
    
    private void printDetailedHelp() {
        System.out.println("\n=== CalPay CLI Help ===\n");
        System.out.println("School Management:");
        System.out.println("  school register - Register a new school");
        System.out.println("  school list     - List all schools");
        System.out.println();
        System.out.println("Student Management:");
        System.out.println("  student enroll  - Enroll a new student");
        System.out.println("  student list    - List students at a school");
        System.out.println();
        System.out.println("Parent Management:");
        System.out.println("  parent register - Register a parent/guardian");
        System.out.println("  parent list     - List parents at a school");
        System.out.println();
        System.out.println("Payment Processing:");
        System.out.println("  payment process - Process a fee payment");
        System.out.println("  payment history - View payment history");
        System.out.println("  payment stats   - View payment statistics");
        System.out.println();
        System.out.println("For detailed documentation, visit: https://docs.calpay.co.za");
    }
    
    // ========== Main ==========
    
    public static void main(String[] args) {
        CalPayCLI cli = new CalPayCLI();
        cli.run();
    }
}