package com.inventory.service;

import com.inventory.dto.request.InventoryRequest;
import com.inventory.dto.response.InventoryResponse;
import com.inventory.entity.Inventory;
import com.inventory.entity.Item;
import com.inventory.exception.InsufficientStockException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.InventoryRepository;
import com.inventory.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InventoryService
 * Tests inventory transaction logic and stock validation
 */
@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemService itemService;

    @InjectMocks
    private InventoryService inventoryService;

    private Item testItem;
    private Inventory testInventory;
    private InventoryRequest testRequest;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Item");
        testItem.setPrice(new BigDecimal("10.00"));

        testInventory = new Inventory();
        testInventory.setId(1L);
        testInventory.setItem(testItem);
        testInventory.setQty(10);
        testInventory.setType("T");
        testInventory.setIsDeleted(false);

        testRequest = new InventoryRequest();
        testRequest.setItemId(1L);
        testRequest.setQty(10);
        testRequest.setType("T");
    }

    @Test
    void recordTransaction_TopUp_Success() {
        // Given
        when(itemRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testItem));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // When
        InventoryResponse response = inventoryService.recordTransaction(testRequest);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getItemId());
        assertEquals(10, response.getQty());
        assertEquals("T", response.getType());
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void recordTransaction_Withdrawal_Success() {
        // Given
        InventoryRequest withdrawalRequest = new InventoryRequest();
        withdrawalRequest.setItemId(1L);
        withdrawalRequest.setQty(5);
        withdrawalRequest.setType("W");

        when(itemRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testItem));
        when(itemService.calculateCurrentStock(1L)).thenReturn(10);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // When
        InventoryResponse response = inventoryService.recordTransaction(withdrawalRequest);

        // Then
        assertNotNull(response);
        verify(itemService, times(1)).calculateCurrentStock(1L);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void recordTransaction_Withdrawal_InsufficientStock_ThrowsException() {
        // Given
        InventoryRequest withdrawalRequest = new InventoryRequest();
        withdrawalRequest.setItemId(1L);
        withdrawalRequest.setQty(20);
        withdrawalRequest.setType("W");

        when(itemRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testItem));
        when(itemService.calculateCurrentStock(1L)).thenReturn(10);

        // When & Then
        InsufficientStockException exception = assertThrows(
                InsufficientStockException.class,
                () -> inventoryService.recordTransaction(withdrawalRequest));

        assertTrue(exception.getMessage().contains("Insufficient stock"));
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void recordTransaction_ItemNotFound_ThrowsException() {
        // Given
        when(itemRepository.findByIdWithLock(999L)).thenReturn(Optional.empty());
        testRequest.setItemId(999L);

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> inventoryService.recordTransaction(testRequest));
    }

    @Test
    void getAllTransactions_Success() {
        // Given
        Page<Inventory> inventoryPage = new PageImpl<>(Arrays.asList(testInventory));
        Pageable pageable = PageRequest.of(0, 10);

        when(inventoryRepository.findAll(pageable)).thenReturn(inventoryPage);

        // When
        Page<InventoryResponse> result = inventoryService.getAllTransactions(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(inventoryRepository, times(1)).findAll(pageable);
    }

    @Test
    void getTransactionById_Success() {
        // Given
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));

        // When
        InventoryResponse response = inventoryService.getTransactionById(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Item", response.getItemName());
    }

    @Test
    void getTransactionById_NotFound_ThrowsException() {
        // Given
        when(inventoryRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> inventoryService.getTransactionById(999L));
    }

    @Test
    void updateTransaction_Success() {
        // Given
        InventoryRequest updateRequest = new InventoryRequest();
        updateRequest.setItemId(1L);
        updateRequest.setQty(15);
        updateRequest.setType("T");

        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));
        when(itemRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testItem));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // When
        InventoryResponse response = inventoryService.updateTransaction(1L, updateRequest);

        // Then
        assertNotNull(response);
        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    @Test
    void deleteTransaction_Success() {
        // Given
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(testInventory));

        // When
        inventoryService.deleteTransaction(1L);

        // Then
        verify(inventoryRepository, times(1)).delete(testInventory);
    }
}
