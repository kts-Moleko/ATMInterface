package com.calpay.repository;

import java.util.List;
import java.util.Optional;

/**
 * Generic repository interface providing CRUD operations.
 * 
 * <p><b>Why generic?</b> Reduces code duplication across entity types.
 * All repositories share common operations (save, find, delete), so
 * we define them once and reuse via inheritance.
 * 
 * <p><b>Why Optional?</b> Forces callers to handle "not found" case
 * explicitly, preventing NullPointerExceptions.
 * 
 * @param <T> the entity type managed by this repository
 * @param <ID> the type of the entity's identifier
 * @author CalPay Team
 * @since 2.0
 */
public interface Repository<T, ID> {
    
    /**
     * Persists an entity to the data store.
     * 
     * <p><b>Upsert semantics:</b> If entity exists (by ID), updates it.
     * If entity is new, inserts it. This simplifies caller logic.
     * 
     * @param entity the entity to save
     * @return the saved entity (may have generated fields like timestamps)
     */
    T save(T entity);
    
    /**
     * Retrieves an entity by its unique identifier.
     * 
     * @param id the unique identifier to search for
     * @return Optional containing entity if found, empty otherwise
     */
    Optional<T> findById(ID id);
    
    /**
     * Retrieves all entities of this type.
     * 
     * <p><b>Warning:</b> For production with large datasets, add pagination.
     * 
     * @return list of all entities (may be empty, never null)
     */
    List<T> findAll();
    
    /**
     * Removes an entity from the data store.
     * 
     * <p><b>Idempotent:</b> If entity doesn't exist, this is a no-op.
     * 
     * @param id the identifier of the entity to delete
     */
    void delete(ID id);
    
    /**
     * Checks if an entity with given ID exists.
     * 
     * @param id the identifier to check
     * @return true if entity exists, false otherwise
     */
    boolean exists(ID id);
}