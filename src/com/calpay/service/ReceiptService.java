package com.calpay.service;

import com.calpay.core.exceptions.ResourceNotFoundException;
import com.calpay.core.model.Payment;
import com.calpay.core.model.Parent;
import com.calpay.core.model.Student;
import com.calpay.core.model.School;
import com.calpay.repository.PaymentRepository;
import com.calpay.repository.ParentRepository;
import com.calpay.repository.StudentRepository;
import com.calpay.repository.SchoolRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * Service for generating payment receipts and statements.
 * 
 * <p><b>Key Responsibilities:</b>
 * <ul>
 *   <li>Generate payment receipts</li>
 *   <li>Generate student account statements</li>
 *   <li>Generate school collection summaries</li>
 *   <li>Format receipts for different channels (PDF, SMS, WhatsApp)</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public class ReceiptService {
    
    private final PaymentRepository paymentRepository;
    private final ParentRepository parentRepository;
    private final StudentRepository studentRepository;
    private final SchoolRepository schoolRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
    
    public ReceiptService(
            PaymentRepository paymentRepository,
            ParentRepository parentRepository,
            StudentRepository studentRepository,
            SchoolRepository schoolRepository) {
        this.paymentRepository = Objects.requireNonNull(paymentRepository);
        this.parentRepository = Objects.requireNonNull(parentRepository);
        this.studentRepository = Objects.requireNonNull(studentRepository);
        this.schoolRepository = Objects.requireNonNull(schoolRepository);
    }
    
    /**
     * Generates a payment receipt.
     * 
     * <p><b>Receipt Format:</b>
     * <pre>
     * ========== PAYMENT RECEIPT ==========
     * School: Sunnydale Primary School
     * Receipt #: PAY-2025-001234
     * Date: 15 Jan 2025 14:30
     * 
     * Student: John Doe (Grade 10)
     * Parent: Jane Doe
     * 
     * Fee: Grade 10 Tuition
     * Amount: ZAR 500.00
     * Method: Instant EFT
     * Status: Completed
     * 
     * Thank you for your payment!
     * =====================================
     * </pre>
     * 
     * @param paymentId payment ID
     * @return formatted receipt
     */
    public String generateReceipt(String paymentId) {
        Payment payment = findPaymentById(paymentId);
        Parent parent = findParentById(payment.getParentId());
        Student student = findStudentById(payment.getStudentId());
        School school = findSchoolById(payment.getSchoolId());
        
        StringBuilder receipt = new StringBuilder();
        receipt.append("========== PAYMENT RECEIPT ==========\n");
        receipt.append(String.format("School: %s\n", school.getName()));
        receipt.append(String.format("Receipt #: %s\n", payment.getPaymentReference()));
        receipt.append(String.format("Date: %s\n", payment.getTimestamp().format(DATE_FORMATTER)));
        receipt.append("\n");
        receipt.append(String.format("Student: %s (%s)\n", student.getFullName(), student.getGradeLevel()));
        receipt.append(String.format("Parent: %s\n", parent.getFullName()));
        receipt.append("\n");
        receipt.append(String.format("Fee: %s\n", payment.getDescription()));
        receipt.append(String.format("Amount: %s\n", payment.getAmount()));
        receipt.append(String.format("Method: %s\n", payment.getPaymentMethod()));
        receipt.append(String.format("Status: %s\n", payment.getPaymentStatus()));
        receipt.append("\n");
        receipt.append("Thank you for your payment!\n");
        receipt.append("=====================================\n");
        
        return receipt.toString();
    }
    
    /**
     * Generates a short receipt for SMS/WhatsApp.
     * 
     * <p><b>Format:</b>
     * <pre>
     * Payment Received
     * Ref: PAY-2025-001234
     * Student: John Doe
     * Amount: ZAR 500.00
     * Date: 15 Jan 2025
     * Thank you!
     * </pre>
     * 
     * @param paymentId payment ID
     * @return short receipt (< 160 chars for SMS)
     */
    public String generateShortReceipt(String paymentId) {
        Payment payment = findPaymentById(paymentId);
        Student student = findStudentById(payment.getStudentId());
        
        return String.format(
            "Payment Received\n" +
            "Ref: %s\n" +
            "Student: %s\n" +
            "Amount: %s\n" +
            "Date: %s\n" +
            "Thank you!",
            payment.getPaymentReference(),
            student.getFullName(),
            payment.getAmount(),
            payment.getTimestamp().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
        );
    }
    
    /**
     * Generates a student account statement.
     * 
     * <p><b>Statement Format:</b>
     * <pre>
     * ========== STUDENT STATEMENT ==========
     * School: Sunnydale Primary School
     * Student: John Doe (Grade 10)
     * Parent: Jane Doe
     * Statement Date: 15 Jan 2025
     * 
     * PAYMENT HISTORY:
     * Date          | Description       | Amount
     * -------------------------------------------
     * 10 Jan 2025   | Grade 10 Tuition  | ZAR 500.00
     * 05 Jan 2025   | Sport Fees        | ZAR 150.00
     * 
     * Total Paid: ZAR 650.00
     * =======================================
     * </pre>
     * 
     * @param studentId student ID
     * @return formatted statement
     */
    public String generateStudentStatement(String studentId) {
        Student student = findStudentById(studentId);
        School school = findSchoolById(student.getSchoolId());
        List<Payment> payments = paymentRepository.findByStudentId(studentId);
        
        StringBuilder statement = new StringBuilder();
        statement.append("========== STUDENT STATEMENT ==========\n");
        statement.append(String.format("School: %s\n", school.getName()));
        statement.append(String.format("Student: %s (%s)\n", student.getFullName(), student.getGradeLevel()));
        statement.append(String.format("Statement Date: %s\n", 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))));
        statement.append("\n");
        statement.append("PAYMENT HISTORY:\n");
        statement.append("Date          | Description       | Amount\n");
        statement.append("-------------------------------------------\n");
        
        if (payments.isEmpty()) {
            statement.append("No payments found.\n");
        } else {
            for (Payment payment : payments) {
                if ("COMPLETED".equals(payment.getPaymentStatus())) {
                    statement.append(String.format("%-13s | %-17s | %s\n",
                        payment.getTimestamp().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                        truncate(payment.getDescription(), 17),
                        payment.getAmount()
                    ));
                }
            }
        }
        
        statement.append("\n");
        statement.append(String.format("Total Paid: %s\n", calculateTotalPaid(payments)));
        statement.append("=======================================\n");
        
        return statement.toString();
    }
    
    /**
     * Generates a school collection summary.
     * 
     * @param schoolId school ID
     * @param year year
     * @param month month (1-12)
     * @return formatted summary
     */
    public String generateCollectionSummary(String schoolId, int year, int month) {
        School school = findSchoolById(schoolId);
        List<Payment> payments = paymentRepository.findBySchoolIdAndYearMonth(
            schoolId, year, month
        );
        
        long completedCount = payments.stream()
            .filter(p -> "COMPLETED".equals(p.getPaymentStatus()))
            .count();
        
        long pendingCount = payments.stream()
            .filter(p -> "PENDING".equals(p.getPaymentStatus()))
            .count();
        
        long failedCount = payments.stream()
            .filter(p -> "FAILED".equals(p.getPaymentStatus()))
            .count();
        
        StringBuilder summary = new StringBuilder();
        summary.append("========== COLLECTION SUMMARY ==========\n");
        summary.append(String.format("School: %s\n", school.getName()));
        summary.append(String.format("Period: %s %d\n", getMonthName(month), year));
        summary.append(String.format("Generated: %s\n", 
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm"))));
        summary.append("\n");
        summary.append(String.format("Total Payments: %d\n", payments.size()));
        summary.append(String.format("  Completed: %d\n", completedCount));
        summary.append(String.format("  Pending: %d\n", pendingCount));
        summary.append(String.format("  Failed: %d\n", failedCount));
        summary.append("\n");
        summary.append(String.format("Total Collected: %s\n", calculateTotalCompleted(payments)));
        summary.append("========================================\n");
        
        return summary.toString();
    }
    
    /**
     * Generates a parent payment summary.
     * 
     * @param parentId parent ID
     * @return formatted summary
     */
    public String generateParentSummary(String parentId) {
        Parent parent = findParentById(parentId);
        School school = findSchoolById(parent.getSchoolId());
        List<Payment> payments = paymentRepository.findByParentId(parentId);
        
        StringBuilder summary = new StringBuilder();
        summary.append("========== PARENT SUMMARY ==========\n");
        summary.append(String.format("School: %s\n", school.getName()));
        summary.append(String.format("Parent: %s\n", parent.getFullName()));
        summary.append(String.format("Phone: %s\n", parent.getPhone()));
        summary.append("\n");
        summary.append(String.format("Total Payments: %d\n", payments.size()));
        summary.append(String.format("Total Amount: %s\n", calculateTotalCompleted(payments)));
        summary.append("====================================\n");
        
        return summary.toString();
    }
    
    // ========== Helper Methods ==========
    
    private Payment findPaymentById(String paymentId) {
        return paymentRepository.findById(paymentId)
            .orElseThrow(() -> new ResourceNotFoundException("Payment", paymentId));
    }
    
    private Parent findParentById(String parentId) {
        return parentRepository.findById(parentId)
            .orElseThrow(() -> new ResourceNotFoundException("Parent", parentId));
    }
    
    private Student findStudentById(String studentId) {
        return studentRepository.findById(studentId)
            .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
    }
    
    private School findSchoolById(String schoolId) {
        return schoolRepository.findById(schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("School", schoolId));
    }
    
    private String calculateTotalPaid(List<Payment> payments) {
        return payments.stream()
            .filter(p -> "COMPLETED".equals(p.getPaymentStatus()))
            .map(Payment::getAmount)
            .reduce((a, b) -> a.add(b))
            .map(Object::toString)
            .orElse("ZAR 0.00");
    }
    
    private String calculateTotalCompleted(List<Payment> payments) {
        return calculateTotalPaid(payments);
    }
    
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() <= maxLength ? text : text.substring(0, maxLength - 3) + "...";
    }
    
    private String getMonthName(int month) {
        String[] months = {"", "Jan", "Feb", "Mar", "Apr", "May", "Jun", 
                          "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        return month >= 1 && month <= 12 ? months[month] : "";
    }
}