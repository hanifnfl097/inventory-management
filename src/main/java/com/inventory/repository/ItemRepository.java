package com.inventory.repository;

import com.inventory.entity.Item;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    /**
     * Find all items with pagination
     * Automatically filters out soft-deleted items due to @Where clause in Entity
     */
    Page<Item> findAll(Pageable pageable);

    /**
     * Find item by ID with pessimistic write lock
     * CRITICAL: Use this for stock validation to prevent race conditions
     * Locks the row until transaction completes
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Item i WHERE i.id = :id")
    Optional<Item> findByIdWithLock(@Param("id") Long id);
}
