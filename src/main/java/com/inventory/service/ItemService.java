package com.inventory.service;

import com.inventory.dto.request.ItemRequest;
import com.inventory.dto.response.ItemResponse;
import com.inventory.entity.Item;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.InventoryRepository;
import com.inventory.repository.ItemRepository;
import com.inventory.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;

    /**
     * Get all items with pagination and calculated current stock
     */
    public Page<ItemResponse> getAllItems(Pageable pageable) {
        Page<Item> items = itemRepository.findAll(pageable);
        return items.map(this::convertToResponse);
    }

    /**
     * Get single item by ID with calculated current stock
     */
    public ItemResponse getItemById(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));
        return convertToResponse(item);
    }

    /**
     * Create new item
     */
    @Transactional
    public ItemResponse createItem(ItemRequest request) {
        Item item = new Item();
        item.setName(request.getName());
        item.setPrice(request.getPrice());
        item.setIsDeleted(false);

        Item savedItem = itemRepository.save(item);
        return convertToResponse(savedItem);
    }

    /**
     * Update existing item (name and price only)
     */
    @Transactional
    public ItemResponse updateItem(Long id, ItemRequest request) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));

        item.setName(request.getName());
        item.setPrice(request.getPrice());

        Item updatedItem = itemRepository.save(item);
        return convertToResponse(updatedItem);
    }

    /**
     * Soft delete item
     */
    @Transactional
    public void deleteItem(Long id) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id));
        itemRepository.delete(item); // Will trigger @SQLDelete (soft delete)
    }

    /**
     * Calculate current stock for an item
     * Formula: SUM(inventory T) - SUM(inventory W) - SUM(orders)
     */
    public Integer calculateCurrentStock(Long itemId) {
        Integer inventoryStock = inventoryRepository.calculateStockFromInventory(itemId);
        Integer orderedQty = orderRepository.getTotalOrderedQty(itemId);
        return inventoryStock - orderedQty;
    }

    /**
     * Convert Item entity to ItemResponse DTO with calculated stock
     */
    private ItemResponse convertToResponse(Item item) {
        Integer currentStock = calculateCurrentStock(item.getId());
        return new ItemResponse(
                item.getId(),
                item.getName(),
                item.getPrice(),
                currentStock);
    }
}
