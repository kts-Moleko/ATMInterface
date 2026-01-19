package com.calpay.repository.impl;

import com.calpay.core.model.Money;
import com.calpay.core.model.Payment;
import com.calpay.repository.PaymentRepository;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * JDBC implementation of PaymentRepository.
 * 
 * <p><b>Database Schema:</b>
 * <pre>
 * CREATE TABLE payments (
 *   payment_id VARCHAR(36) PRIMARY KEY,
 *   payment_reference VARCHAR(50) UNIQUE NOT NULL,
 *   idempotency_key VARCHAR(100) UNIQUE NOT NULL,
 *   school_id VARCHAR(36) NOT NULL,
 *   parent_id VARCHAR(36) NOT NULL,
 *   student_id VARCHAR(36) NOT NULL,
 *   fee_structure_id VARCHAR(36),
 *   amount DECIMAL(10,2) NOT NULL,
 *   currency VARCHAR(3) NOT NULL,
 *   payment_method VARCHAR(20) NOT NULL,
 *   payment_status VARCHAR(20) NOT NULL,
 *   timestamp TIMESTAMP NOT NULL,
 *   payment_date TIMESTAMP,
 *   fee_month VARCHAR(7),
 *   external_reference VARCHAR(100),
 *   gateway_response TEXT,
 *   failure_reason TEXT,
 *   retry_count INT DEFAULT 0,
 *   receipt_id VARCHAR(36),
 *   receipt_url VARCHAR(255),
 *   description VARCHAR(255),
 *   initiated_by VARCHAR(36),
 *   notes TEXT,
 *   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
 *   INDEX idx_school_timestamp (school_id, timestamp),
 *   INDEX idx_parent (parent_id),
 *   INDEX idx_student (student_id),
 *   INDEX idx_status (payment_status)
 * );
 * </pre>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public class PaymentRepositoryJdbc implements PaymentRepository {
    
    private final DataSource dataSource;
    
    public PaymentRepositoryJdbc(DataSource dataSource) {
        this.dataSource = Objects.requireNonNull(dataSource, "DataSource cannot be null");
    }
    
    @Override
    public Payment save(Payment payment) {
        // Try update first, then insert if not exists
        String updateSql = "UPDATE payments SET " +
            "payment_status = ?, gateway_response = ?, failure_reason = ?, " +
            "retry_count = ?, receipt_id = ?, receipt_url = ? " +
            "WHERE payment_id = ?";
        
        String insertSql = "INSERT INTO payments (" +
            "payment_id, payment_reference, idempotency_key, school_id, parent_id, student_id, " +
            "fee_structure_id, amount, currency, payment_method, payment_status, timestamp, " +
            "payment_date, fee_month, external_reference, gateway_response, failure_reason, " +
            "retry_count, receipt_id, receipt_url, description, initiated_by, notes" +
            ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dataSource.getConnection()) {
            // Try update first
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                updateStmt.setString(1, payment.getPaymentStatus());
                updateStmt.setString(2, payment.getGatewayResponse());
                updateStmt.setString(3, payment.getFailureReason());
                updateStmt.setInt(4, payment.getRetryCount());
                updateStmt.setString(5, payment.getReceiptId());
                updateStmt.setString(6, payment.getReceiptUrl());
                updateStmt.setString(7, payment.getPaymentId());
                
                int rowsAffected = updateStmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    return payment; // Update successful
                }
            }
            
            // If update didn't affect any rows, do insert
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                insertStmt.setString(1, payment.getPaymentId());
                insertStmt.setString(2, payment.getPaymentReference());
                insertStmt.setString(3, payment.getIdempotencyKey());
                insertStmt.setString(4, payment.getSchoolId());
                insertStmt.setString(5, payment.getParentId());
                insertStmt.setString(6, payment.getStudentId());
                insertStmt.setString(7, payment.getFeeStructureId());
                insertStmt.setBigDecimal(8, payment.getAmount().getAmount());
                insertStmt.setString(9, payment.getAmount().getCurrency());
                insertStmt.setString(10, payment.getPaymentMethod());
                insertStmt.setString(11, payment.getPaymentStatus());
                insertStmt.setTimestamp(12, Timestamp.valueOf(payment.getTimestamp()));
                insertStmt.setTimestamp(13, payment.getPaymentDate() != null ? 
                    Timestamp.valueOf(payment.getPaymentDate()) : null);
                insertStmt.setString(14, payment.getFeeMonth());
                insertStmt.setString(15, payment.getExternalReference());
                insertStmt.setString(16, payment.getGatewayResponse());
                insertStmt.setString(17, payment.getFailureReason());
                insertStmt.setInt(18, payment.getRetryCount());
                insertStmt.setString(19, payment.getReceiptId());
                insertStmt.setString(20, payment.getReceiptUrl());
                insertStmt.setString(21, payment.getDescription());
                insertStmt.setString(22, payment.getInitiatedBy());
                insertStmt.setString(23, payment.getNotes());
                
                insertStmt.executeUpdate();
                return payment;
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save payment: " + e.getMessage(), e);
        }
    }
    
    @Override
    public Optional<Payment> findById(String paymentId) {
        String sql = "SELECT * FROM payments WHERE payment_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, paymentId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPayment(rs));
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find payment: " + e.getMessage(), e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<Payment> findByPaymentReference(String paymentReference) {
        String sql = "SELECT * FROM payments WHERE payment_reference = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, paymentReference);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPayment(rs));
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find payment by reference: " + e.getMessage(), e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
        String sql = "SELECT * FROM payments WHERE idempotency_key = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idempotencyKey);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPayment(rs));
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find payment by idempotency key: " + e.getMessage(), e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<Payment> findBySchoolId(String schoolId) {
        String sql = "SELECT * FROM payments WHERE school_id = ? ORDER BY timestamp DESC";
        return executeQueryList(sql, schoolId);
    }
    
    @Override
    public List<Payment> findBySchoolIdAndStatus(String schoolId, String status) {
        String sql = "SELECT * FROM payments " +
            "WHERE school_id = ? AND payment_status = ? " +
            "ORDER BY timestamp DESC";
        return executeQueryList(sql, schoolId, status);
    }
    
    @Override
    public List<Payment> findBySchoolIdAndPaymentMethod(String schoolId, String paymentMethod) {
        String sql = "SELECT * FROM payments " +
            "WHERE school_id = ? AND payment_method = ? " +
            "ORDER BY timestamp DESC";
        return executeQueryList(sql, schoolId, paymentMethod);
    }
    
    @Override
    public List<Payment> findBySchoolIdAndTimestampBetween(
            String schoolId, 
            LocalDateTime startDate, 
            LocalDateTime endDate) {
        String sql = "SELECT * FROM payments " +
            "WHERE school_id = ? AND timestamp BETWEEN ? AND ? " +
            "ORDER BY timestamp DESC";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, schoolId);
            stmt.setTimestamp(2, Timestamp.valueOf(startDate));
            stmt.setTimestamp(3, Timestamp.valueOf(endDate));
            
            return executeQueryWithStatement(stmt);
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find payments by date range: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<Payment> findBySchoolIdAndYearMonth(String schoolId, int year, int month) {
        String feeMonth = String.format("%04d-%02d", year, month);
        String sql = "SELECT * FROM payments " +
            "WHERE school_id = ? AND fee_month = ? " +
            "ORDER BY timestamp DESC";
        return executeQueryList(sql, schoolId, feeMonth);
    }
    
    @Override
    public List<Payment> findByStudentId(String studentId) {
        String sql = "SELECT * FROM payments WHERE student_id = ? ORDER BY timestamp DESC";
        return executeQueryList(sql, studentId);
    }
    
    @Override
    public List<Payment> findByStudentIdAndStatus(String studentId, String status) {
        String sql = "SELECT * FROM payments " +
            "WHERE student_id = ? AND payment_status = ? " +
            "ORDER BY timestamp DESC";
        return executeQueryList(sql, studentId, status);
    }
    
    @Override
    public List<Payment> findByParentId(String parentId) {
        String sql = "SELECT * FROM payments WHERE parent_id = ? ORDER BY timestamp DESC";
        return executeQueryList(sql, parentId);
    }
    
    @Override
    public List<Payment> findByParentIdAndStatus(String parentId, String status) {
        String sql = "SELECT * FROM payments " +
            "WHERE parent_id = ? AND payment_status = ? " +
            "ORDER BY timestamp DESC";
        return executeQueryList(sql, parentId, status);
    }
    
    @Override
    public boolean existsByIdempotencyKey(String idempotencyKey) {
        String sql = "SELECT COUNT(*) FROM payments WHERE idempotency_key = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, idempotencyKey);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check idempotency key: " + e.getMessage(), e);
        }
        
        return false;
    }
    
    @Override
    public long countBySchoolId(String schoolId) {
        String sql = "SELECT COUNT(*) FROM payments WHERE school_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, schoolId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to count payments: " + e.getMessage(), e);
        }
        
        return 0;
    }
    
    @Override
    public void deleteById(String paymentId) {
        String sql = "DELETE FROM payments WHERE payment_id = ?";
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, paymentId);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete payment: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void deleteAll() {
        String sql = "DELETE FROM payments";
        
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(sql);
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete all payments: " + e.getMessage(), e);
        }
    }
    
    // Helper methods
    
    private List<Payment> executeQueryList(String sql, Object... params) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            return executeQueryWithStatement(stmt);
            
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute query: " + e.getMessage(), e);
        }
    }
    
    private List<Payment> executeQueryWithStatement(PreparedStatement stmt) throws SQLException {
        List<Payment> payments = new ArrayList<>();
        
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                payments.add(mapResultSetToPayment(rs));
            }
        }
        
        return payments;
    }
    
    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        return new Payment.Builder()
            .paymentId(rs.getString("payment_id"))
            .paymentReference(rs.getString("payment_reference"))
            .idempotencyKey(rs.getString("idempotency_key"))
            .schoolId(rs.getString("school_id"))
            .parentId(rs.getString("parent_id"))
            .studentId(rs.getString("student_id"))
            .feeStructureId(rs.getString("fee_structure_id"))
            .amount(Money.of(rs.getBigDecimal("amount"), rs.getString("currency")))
            .paymentMethod(rs.getString("payment_method"))
            .paymentStatus(rs.getString("payment_status"))
            .timestamp(rs.getTimestamp("timestamp").toLocalDateTime())
            .paymentDate(rs.getTimestamp("payment_date") != null ? 
                rs.getTimestamp("payment_date").toLocalDateTime() : null)
            .feeMonth(rs.getString("fee_month"))
            .externalReference(rs.getString("external_reference"))
            .gatewayResponse(rs.getString("gateway_response"))
            .failureReason(rs.getString("failure_reason"))
            .retryCount(rs.getInt("retry_count"))
            .receiptId(rs.getString("receipt_id"))
            .receiptUrl(rs.getString("receipt_url"))
            .description(rs.getString("description"))
            .initiatedBy(rs.getString("initiated_by"))
            .notes(rs.getString("notes"))
            .build();
    }
}