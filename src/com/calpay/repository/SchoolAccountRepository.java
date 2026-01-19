package com.calpay.repository;

import com.calpay.core.model.SchoolAccount;
import com.calpay.core.enums.AccountStatus;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for SchoolAccount data access operations.
 * 
 * <p><b>Indexing Strategy:</b>
 * <ul>
 *   <li>schoolId (unique, one account per school)</li>
 *   <li>accountNumber (unique)</li>
 *   <li>status (for filtering)</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public interface SchoolAccountRepository {
    
    /**
     * Saves a school account (insert or update).
     * 
     * @param account school account to save
     * @return saved school account
     */
    SchoolAccount save(SchoolAccount account);
    
    /**
     * Finds a school account by ID.
     * 
     * @param accountId account ID
     * @return optional school account
     */
    Optional<SchoolAccount> findById(String accountId);
    
    /**
     * Finds a school account by school ID.
     * 
     * @param schoolId school ID
     * @return optional school account
     */
    Optional<SchoolAccount> findBySchoolId(String schoolId);
    
    /**
     * Finds a school account by account number.
     * 
     * @param accountNumber account number
     * @return optional school account
     */
    Optional<SchoolAccount> findByAccountNumber(String accountNumber);
    
    /**
     * Finds all school accounts.
     * 
     * @return list of all school accounts
     */
    List<SchoolAccount> findAll();
    
    /**
     * Finds school accounts by status.
     * 
     * @param status account status
     * @return list of school accounts
     */
    List<SchoolAccount> findByStatus(AccountStatus status);
    
    /**
     * Finds school accounts by subscription status.
     * 
     * @param subscriptionStatus subscription status
     * @return list of school accounts
     */
    List<SchoolAccount> findBySubscriptionStatus(SchoolAccount.SubscriptionStatus subscriptionStatus);
    
    /**
     * Checks if an account exists for a school.
     * 
     * @param schoolId school ID
     * @return true if exists
     */
    boolean existsBySchoolId(String schoolId);
    
    /**
     * Counts total school accounts.
     * 
     * @return account count
     */
    long count();
    
    /**
     * Deletes a school account by ID.
     * 
     * @param accountId account ID
     */
    void deleteById(String accountId);
    
    /**
     * Deletes all school accounts (testing only).
     */
    void deleteAll();
}