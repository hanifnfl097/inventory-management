package com.inventory.service;

import com.inventory.dto.request.InventoryRequest;
import com.inventory.dto.response.InventoryResponse;
import com.inventory.entity.Inventory;
import com.inventory.entity.Item;
import com.inventory.exception.InsufficientStockException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.InventoryRepository;
import com.inventory.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ItemRepository itemRepository;
    private final ItemService itemService;

    /**
     * Record inventory transaction (Top Up or Withdrawal)
     * CRITICAL: Validate stock for Withdrawal transactions
     * Uses pessimistic locking to prevent race conditions
     */
    @Transactional
    public InventoryResponse recordTransaction(InventoryRequest request) {
        // 0. Explicit negative quantity check
        if (request.getQty() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive, got: " + request.getQty());
        }

        // 1. Validate item exists WITH PESSIMISTIC LOCK
        Item item = itemRepository.findByIdWithLock(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + request.getItemId()));

        // 2. If Withdrawal, check stock sufficiency
        if ("W".equals(request.getType())) {
            Integer currentStock = itemService.calculateCurrentStock(item.getId());
            if (currentStock < request.getQty()) {
                throw new InsufficientStockException(
                        "Insufficient stock for item: " + item.getName() +
                                ". Available: " + currentStock + ", Requested: " + request.getQty());
            }
        }

        // 3. Save inventory record
        Inventory inventory = new Inventory();
        inventory.setItem(item);
        inventory.setQty(request.getQty());
        inventory.setType(request.getType());
        inventory.setIsDeleted(false);

        Inventory savedInventory = inventoryRepository.save(inventory);
        return convertToResponse(savedInventory);
    }

    /**
     * Get all inventory transactions with pagination
     */
    public Page<InventoryResponse> getAllTransactions(Pageable pageable) {
        Page<Inventory> inventories = inventoryRepository.findAll(pageable);
        return inventories.map(this::convertToResponse);
    }

    /**
     * Get single inventory transaction by ID
     */
    public InventoryResponse getTransactionById(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory transaction not found with id: " + id));
        return convertToResponse(inventory);
    }

    /**
     * Update existing inventory transaction
     * CRITICAL: Validates stock for Withdrawal type
     * Fixed: Handles cross-item updates and uses pessimistic locking
     */
    @Transactional
    public InventoryResponse updateTransaction(Long id, InventoryRequest request) {
        // 0. Explicit negative quantity check
        if (request.getQty() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive, got: " + request.getQty());
        }

        // 1. Find existing transaction
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory transaction not found with id: " + id));

        // 2. Validate item exists WITH PESSIMISTIC LOCK
        Item item = itemRepository.findByIdWithLock(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + request.getItemId()));

        // 3. Validate stock for Withdrawal type
        if ("W".equals(request.getType())) {
            Integer currentStock = itemService.calculateCurrentStock(item.getId());

            // CRITICAL: Only adjust for old transaction if item hasn't changed
            Integer availableStock = currentStock;

            if (inventory.getItem().getId().equals(item.getId())) {
                // Same item: adjust for old transaction effect
                if ("T".equals(inventory.getType())) {
                    // Changing from Top Up to Withdrawal on same item
                    availableStock = currentStock - inventory.getQty();
                } else if ("W".equals(inventory.getType())) {
                    // Updating existing Withdrawal on same item
                    availableStock = currentStock + inventory.getQty();
                }
            }
            // else: Different item, use current stock as-is

            if (availableStock < request.getQty()) {
                throw new InsufficientStockException(
                        "Insufficient stock for item: " + item.getName() +
                                ". Available: " + availableStock +
                                " (Current: " + currentStock +
                                (inventory.getItem().getId().equals(item.getId())
                                        ? ", Old Transaction: " + inventory.getType() + inventory.getQty()
                                        : "")
                                + ")" +
                                ", Requested: " + request.getQty());
            }
        }

        // 4. Update transaction
        inventory.setItem(item);
        inventory.setQty(request.getQty());
        inventory.setType(request.getType());

        Inventory updatedInventory = inventoryRepository.save(inventory);
        return convertToResponse(updatedInventory);
    }

    /**
     * Soft delete inventory transaction
     */
    @Transactional
    public void deleteTransaction(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory transaction not found with id: " + id));
        inventoryRepository.delete(inventory); // Soft delete via @SQLDelete
    }

    /**
     * Convert Inventory entity to InventoryResponse DTO
     */
    private InventoryResponse convertToResponse(Inventory inventory) {
        return new InventoryResponse(
                inventory.getId(),
                inventory.getItem().getId(),
                inventory.getItem().getName(),
                inventory.getQty(),
                inventory.getType());
    }
}
