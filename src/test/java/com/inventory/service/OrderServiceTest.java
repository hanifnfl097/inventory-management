package com.inventory.service;

import com.inventory.dto.request.OrderRequest;
import com.inventory.dto.response.OrderResponse;
import com.inventory.entity.Item;
import com.inventory.entity.Order;
import com.inventory.exception.InsufficientStockException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.ItemRepository;
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
 * Unit tests for OrderService
 * Tests order creation with auto-generated order numbers and stock validation
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemService itemService;

    @InjectMocks
    private OrderService orderService;

    private Item testItem;
    private Order testOrder;
    private OrderRequest testRequest;

    @BeforeEach
    void setUp() {
        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Item");
        testItem.setPrice(new BigDecimal("10.00"));

        testOrder = new Order();
        testOrder.setOrderNo("O1");
        testOrder.setItem(testItem);
        testOrder.setQty(5);
        testOrder.setPrice(new BigDecimal("50.00"));
        testOrder.setIsDeleted(false);

        testRequest = new OrderRequest();
        testRequest.setItemId(1L);
        testRequest.setQty(5);
        testRequest.setPrice(new BigDecimal("50.00"));
    }

    @Test
    void createOrder_Success() {
        // Given
        when(itemRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testItem));
        when(itemService.calculateCurrentStock(1L)).thenReturn(10);
        when(orderRepository.getMaxOrderSequence()).thenReturn(0);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        OrderResponse response = orderService.createOrder(testRequest);

        // Then
        assertNotNull(response);
        assertEquals("O1", response.getOrderNo());
        assertEquals(5, response.getQty());
        assertEquals(new BigDecimal("50.00"), response.getPrice());
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(itemService, times(1)).calculateCurrentStock(1L);
    }

    @Test
    void createOrder_InsufficientStock_ThrowsException() {
        // Given
        testRequest.setQty(20);
        when(itemRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testItem));
        when(itemService.calculateCurrentStock(1L)).thenReturn(10);

        // When & Then
        InsufficientStockException exception = assertThrows(
                InsufficientStockException.class,
                () -> orderService.createOrder(testRequest));

        assertTrue(exception.getMessage().contains("Insufficient stock"));
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_ItemNotFound_ThrowsException() {
        // Given
        when(itemRepository.findByIdWithLock(999L)).thenReturn(Optional.empty());
        testRequest.setItemId(999L);

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> orderService.createOrder(testRequest));
    }

    @Test
    void createOrder_AutoGeneratesOrderNumber() {
        // Given
        when(itemRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testItem));
        when(itemService.calculateCurrentStock(1L)).thenReturn(20);
        when(orderRepository.getMaxOrderSequence()).thenReturn(5); // Last order was O5

        Order expectedOrder = new Order();
        expectedOrder.setOrderNo("O6"); // Should generate O6
        expectedOrder.setItem(testItem);
        expectedOrder.setQty(5);
        expectedOrder.setPrice(new BigDecimal("50.00"));

        when(orderRepository.save(any(Order.class))).thenReturn(expectedOrder);

        // When
        OrderResponse response = orderService.createOrder(testRequest);

        // Then
        assertEquals("O6", response.getOrderNo());
        verify(orderRepository, times(1)).getMaxOrderSequence();
    }

    @Test
    void getAllOrders_Success() {
        // Given
        Page<Order> orderPage = new PageImpl<>(Arrays.asList(testOrder));
        Pageable pageable = PageRequest.of(0, 10);

        when(orderRepository.findAll(pageable)).thenReturn(orderPage);

        // When
        Page<OrderResponse> result = orderService.getAllOrders(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals("O1", result.getContent().get(0).getOrderNo());
        verify(orderRepository, times(1)).findAll(pageable);
    }

    @Test
    void getOrderById_Success() {
        // Given
        when(orderRepository.findById("O1")).thenReturn(Optional.of(testOrder));

        // When
        OrderResponse response = orderService.getOrderById("O1");

        // Then
        assertNotNull(response);
        assertEquals("O1", response.getOrderNo());
        assertEquals("Test Item", response.getItemName());
    }

    @Test
    void getOrderById_NotFound_ThrowsException() {
        // Given
        when(orderRepository.findById("O999")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> orderService.getOrderById("O999"));
    }

    @Test
    void updateOrder_Success() {
        // Given
        OrderRequest updateRequest = new OrderRequest();
        updateRequest.setItemId(1L);
        updateRequest.setQty(3);
        updateRequest.setPrice(new BigDecimal("30.00"));

        when(orderRepository.findById("O1")).thenReturn(Optional.of(testOrder));
        when(itemRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testItem));
        when(itemService.calculateCurrentStock(1L)).thenReturn(15);
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        OrderResponse response = orderService.updateOrder("O1", updateRequest);

        // Then
        assertNotNull(response);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(itemService, times(1)).calculateCurrentStock(1L);
    }

    @Test
    void updateOrder_InsufficientStock_ThrowsException() {
        // Given
        OrderRequest updateRequest = new OrderRequest();
        updateRequest.setItemId(1L);
        updateRequest.setQty(30);
        updateRequest.setPrice(new BigDecimal("300.00"));

        when(orderRepository.findById("O1")).thenReturn(Optional.of(testOrder));
        when(itemRepository.findByIdWithLock(1L)).thenReturn(Optional.of(testItem));
        when(itemService.calculateCurrentStock(1L)).thenReturn(10);

        // When & Then
        assertThrows(InsufficientStockException.class,
                () -> orderService.updateOrder("O1", updateRequest));
    }

    @Test
    void deleteOrder_Success() {
        // Given
        when(orderRepository.findById("O1")).thenReturn(Optional.of(testOrder));

        // When
        orderService.deleteOrder("O1");

        // Then
        verify(orderRepository, times(1)).delete(testOrder);
    }

    @Test
    void deleteOrder_NotFound_ThrowsException() {
        // Given
        when(orderRepository.findById("O999")).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> orderService.deleteOrder("O999"));
    }
}
