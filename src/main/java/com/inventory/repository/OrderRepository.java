package com.inventory.repository;

import com.inventory.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    /**
     * Find all orders with pagination
     * Automatically filters out soft-deleted orders due to @Where clause
     */
    Page<Order> findAll(Pageable pageable);

    /**
     * Get total ordered quantity for a specific item
     * ONLY counts non-deleted orders
     */
    @Query("SELECT COALESCE(SUM(o.qty), 0) FROM Order o " +
            "WHERE o.item.id = :itemId AND o.isDeleted = false")
    Integer getTotalOrderedQty(@Param("itemId") Long itemId);

    /**
     * Get the maximum order sequence number for auto-generation
     * Extracts number from O1, O2, O3, ... format
     * ONLY counts non-deleted orders
     */
    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(o.orderNo, 2) AS int)), 0) FROM Order o WHERE o.isDeleted = false")
    Integer getMaxOrderSequence();
}
