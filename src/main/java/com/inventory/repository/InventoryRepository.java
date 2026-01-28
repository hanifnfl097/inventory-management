package com.inventory.repository;

import com.inventory.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * Find all inventory transactions with pagination
     * Automatically filters out soft-deleted records due to @Where clause
     */
    Page<Inventory> findAll(Pageable pageable);

    /**
     * Calculate stock from inventory transactions (Top Up - Withdrawal)
     * ONLY counts non-deleted records
     * Type 'T' adds to stock, Type 'W' subtracts from stock
     */
    @Query("SELECT COALESCE(SUM(CASE WHEN i.type = 'T' THEN i.qty ELSE -i.qty END), 0) " +
            "FROM Inventory i WHERE i.item.id = :itemId AND i.isDeleted = false")
    Integer calculateStockFromInventory(@Param("itemId") Long itemId);
}
