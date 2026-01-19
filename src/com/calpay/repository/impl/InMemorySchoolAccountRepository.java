package com.calpay.repository.impl;

import com.calpay.core.enums.AccountStatus;
import com.calpay.core.model.SchoolAccount;
import com.calpay.repository.SchoolAccountRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory implementation of SchoolAccountRepository for testing/demo.
 * 
 * <p><b>Thread Safety:</b> Uses ConcurrentHashMap for thread-safe operations.
 * 
 * <p><b>Indexing Strategy:</b>
 * <ul>
 *   <li>Primary: accountId → SchoolAccount</li>
 *   <li>Unique: schoolId → accountId (one account per school)</li>
 *   <li>Unique: accountNumber → accountId</li>
 * </ul>
 * 
 * @author CalPay Team
 * @version 2.0
 * @since 2.0
 */
public class InMemorySchoolAccountRepository implements SchoolAccountRepository {
    
    private final Map<String, SchoolAccount> accounts = new ConcurrentHashMap<>();
    private final Map<String, String> schoolIndex = new ConcurrentHashMap<>();   // schoolId -> accountId
    private final Map<String, String> accountNumberIndex = new ConcurrentHashMap<>(); // accountNumber -> accountId
    
    @Override
    public SchoolAccount save(SchoolAccount account) {
        accounts.put(account.getAccountId(), account);
        
        // Update indexes
        schoolIndex.put(account.getSchoolId(), account.getAccountId());
        accountNumberIndex.put(account.getAccountNumber(), account.getAccountId());
        
        return account;
    }
    
    @Override
    public Optional<SchoolAccount> findById(String accountId) {
        return Optional.ofNullable(accounts.get(accountId));
    }
    
    @Override
    public Optional<SchoolAccount> findBySchoolId(String schoolId) {
        String accountId = schoolIndex.get(schoolId);
        return accountId != null ? Optional.ofNullable(accounts.get(accountId)) : Optional.empty();
    }
    
    @Override
    public Optional<SchoolAccount> findByAccountNumber(String accountNumber) {
        String accountId = accountNumberIndex.get(accountNumber);
        return accountId != null ? Optional.ofNullable(accounts.get(accountId)) : Optional.empty();
    }
    
    @Override
    public List<SchoolAccount> findAll() {
        return new ArrayList<>(accounts.values());
    }
    
    @Override
    public List<SchoolAccount> findByStatus(AccountStatus status) {
        return accounts.values().stream()
            .filter(a -> a.getStatus() == status)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<SchoolAccount> findBySubscriptionStatus(SchoolAccount.SubscriptionStatus subscriptionStatus) {
        return accounts.values().stream()
            .filter(a -> a.getSubscriptionStatus() == subscriptionStatus)
            .collect(Collectors.toList());
    }
    
    @Override
    public boolean existsBySchoolId(String schoolId) {
        return schoolIndex.containsKey(schoolId);
    }
    
    @Override
    public long count() {
        return accounts.size();
    }
    
    @Override
    public void deleteById(String accountId) {
        SchoolAccount account = accounts.remove(accountId);
        if (account != null) {
            // Clean up indexes
            schoolIndex.remove(account.getSchoolId());
            accountNumberIndex.remove(account.getAccountNumber());
        }
    }
    
    @Override
    public void deleteAll() {
        accounts.clear();
        schoolIndex.clear();
        accountNumberIndex.clear();
    }
    
    /**
     * Gets all accounts (for testing).
     * 
     * @return all school accounts
     */
    public Collection<SchoolAccount> getAll() {
        return new ArrayList<>(accounts.values());
    }
    
    /**
     * Finds active accounts.
     * 
     * @return list of active accounts
     */
    public List<SchoolAccount> findActiveAccounts() {
        return accounts.values().stream()
            .filter(a -> a.getStatus() == AccountStatus.ACTIVE)
            .collect(Collectors.toList());
    }
    
    /**
     * Finds accounts with trial subscriptions.
     * 
     * @return list of trial accounts
     */
    public List<SchoolAccount> findTrialAccounts() {
        return accounts.values().stream()
            .filter(a -> a.getSubscriptionStatus() == SchoolAccount.SubscriptionStatus.TRIAL)
            .collect(Collectors.toList());
    }
    
    /**
     * Finds accounts with expired subscriptions.
     * 
     * @return list of expired accounts
     */
    public List<SchoolAccount> findExpiredSubscriptions() {
        return accounts.values().stream()
            .filter(a -> !a.isSubscriptionActive() && 
                        a.getSubscriptionStatus() != SchoolAccount.SubscriptionStatus.FREE)
            .collect(Collectors.toList());
    }
}