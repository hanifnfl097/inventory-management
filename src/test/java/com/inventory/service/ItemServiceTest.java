package com.inventory.service;

import com.inventory.dto.request.ItemRequest;
import com.inventory.dto.response.ItemResponse;
import com.inventory.entity.Item;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.ItemRepository;
import com.inventory.repository.InventoryRepository;
import com.inventory.repository.OrderRepository;
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
import static org.mockito.Mockito.*;

/**
 * Unit tests for ItemService
 * Tests business logic, CRUD operations, and exception handling
 */
@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private ItemService itemService;

    private Item testItem;
    private ItemRequest testRequest;

    @BeforeEach
    void setUp() {
        // Setup test data
        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Item");
        testItem.setPrice(new BigDecimal("10.00"));
        testItem.setIsDeleted(false);

        testRequest = new ItemRequest();
        testRequest.setName("Test Item");
        testRequest.setPrice(new BigDecimal("10.00"));
    }

    @Test
    void createItem_Success() {
        // Given
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);
        when(inventoryRepository.calculateStockFromInventory(1L)).thenReturn(0);
        when(orderRepository.getTotalOrderedQty(1L)).thenReturn(0);

        // When
        ItemResponse response = itemService.createItem(testRequest);

        // Then
        assertNotNull(response);
        assertEquals("Test Item", response.getName());
        assertEquals(new BigDecimal("10.00"), response.getPrice());
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void getItemById_Success() {
        // Given
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        // When
        ItemResponse response = itemService.getItemById(1L);

        // Then
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Test Item", response.getName());
        verify(itemRepository, times(1)).findById(1L);
    }

    @Test
    void getItemById_NotFound_ThrowsException() {
        // Given
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> itemService.getItemById(999L));

        assertEquals("Item not found with id: 999", exception.getMessage());
        verify(itemRepository, times(1)).findById(999L);
    }

    @Test
    void updateItem_Success() {
        // Given
        ItemRequest updateRequest = new ItemRequest();
        updateRequest.setName("Updated Item");
        updateRequest.setPrice(new BigDecimal("15.00"));

        Item updatedItem = new Item();
        updatedItem.setId(1L);
        updatedItem.setName("Updated Item");
        updatedItem.setPrice(new BigDecimal("15.00"));

        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.save(any(Item.class))).thenReturn(updatedItem);

        // When
        ItemResponse response = itemService.updateItem(1L, updateRequest);

        // Then
        assertNotNull(response);
        assertEquals("Updated Item", response.getName());
        assertEquals(new BigDecimal("15.00"), response.getPrice());
        verify(itemRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).save(any(Item.class));
    }

    @Test
    void updateItem_NotFound_ThrowsException() {
        // Given
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> itemService.updateItem(999L, testRequest));
        verify(itemRepository, times(1)).findById(999L);
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void deleteItem_Success() {
        // Given
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        // When
        itemService.deleteItem(1L);

        // Then
        verify(itemRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).delete(testItem);
    }

    @Test
    void deleteItem_NotFound_ThrowsException() {
        // Given
        when(itemRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> itemService.deleteItem(999L));
        verify(itemRepository, times(1)).findById(999L);
        verify(itemRepository, never()).delete(any(Item.class));
    }

    @Test
    void getAllItems_Success() {
        // Given
        Item item2 = new Item();
        item2.setId(2L);
        item2.setName("Item 2");
        item2.setPrice(new BigDecimal("20.00"));

        Page<Item> itemPage = new PageImpl<>(Arrays.asList(testItem, item2));
        Pageable pageable = PageRequest.of(0, 10);

        when(itemRepository.findAll(pageable)).thenReturn(itemPage);

        // When
        Page<ItemResponse> result = itemService.getAllItems(pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getContent().size());
        assertEquals("Test Item", result.getContent().get(0).getName());
        assertEquals("Item 2", result.getContent().get(1).getName());
        verify(itemRepository, times(1)).findAll(pageable);
    }
}
