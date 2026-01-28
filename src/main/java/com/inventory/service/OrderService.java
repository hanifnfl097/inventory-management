package com.inventory.service;

import com.inventory.dto.request.OrderRequest;
import com.inventory.dto.response.OrderResponse;
import com.inventory.entity.Item;
import com.inventory.entity.Order;
import com.inventory.exception.InsufficientStockException;
import com.inventory.exception.ResourceNotFoundException;
import com.inventory.repository.ItemRepository;
import com.inventory.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final ItemService itemService;

    /**
     * Create new order with stock validation and auto-generated order number
     * CRITICAL: Validates stock before creating order
     * Uses pessimistic locking to prevent race conditions
     */
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        // 0. Explicit negative quantity check
        if (request.getQty() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive, got: " + request.getQty());
        }

        // 1. Validate item exists WITH PESSIMISTIC LOCK (prevents race condition)
        Item item = itemRepository.findByIdWithLock(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + request.getItemId()));

        // 2. Calculate current stock
        Integer currentStock = itemService.calculateCurrentStock(item.getId());

        // 3. Validate stock sufficiency
        if (currentStock < request.getQty()) {
            throw new InsufficientStockException(
                    "Insufficient stock for item: " + item.getName() +
                            ". Available: " + currentStock + ", Requested: " + request.getQty());
        }

        // 4. Generate order number (O1, O2, O3, ...)
        Integer nextSeq = orderRepository.getMaxOrderSequence() + 1;
        String orderNo = "O" + nextSeq;

        // 5. Determine price: use provided price or default to item's current price
        BigDecimal orderPrice = (request.getPrice() != null) ? request.getPrice() : item.getPrice();

        // 6. Create and save order
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setItem(item);
        order.setQty(request.getQty());
        order.setPrice(orderPrice);
        order.setIsDeleted(false);

        Order savedOrder = orderRepository.save(order);
        return convertToResponse(savedOrder);
    }

    /**
     * Get all orders with pagination
     */
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(this::convertToResponse);
    }

    /**
     * Get single order by order number
     */
    public OrderResponse getOrderById(String orderNo) {
        Order order = orderRepository.findById(orderNo)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNo));
        return convertToResponse(order);
    }

    /**
     * Update existing order
     * CRITICAL: Validates stock availability, order_no cannot be changed
     * Fixed: Handles cross-item updates correctly
     */
    @Transactional
    public OrderResponse updateOrder(String orderNo, OrderRequest request) {
        // 0. Explicit negative quantity check
        if (request.getQty() <= 0) {
            throw new IllegalArgumentException("Quantity must be positive, got: " + request.getQty());
        }

        // 1. Find existing order
        Order order = orderRepository.findById(orderNo)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNo));

        // 2. Validate item exists WITH PESSIMISTIC LOCK
        Item item = itemRepository.findByIdWithLock(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + request.getItemId()));

        // 3. Calculate current stock
        Integer currentStock = itemService.calculateCurrentStock(item.getId());

        // 4. Validate stock sufficiency
        // CRITICAL FIX: Only add back old qty if item hasn't changed!
        Integer availableStock = currentStock;
        if (order.getItem().getId().equals(item.getId())) {
            // Same item: add back existing order qty since it will be replaced
            availableStock = currentStock + order.getQty();
        }
        // else: Different item, use current stock as-is (old order qty goes back to old
        // item)

        if (availableStock < request.getQty()) {
            throw new InsufficientStockException(
                    "Insufficient stock for item: " + item.getName() +
                            ". Available: " + availableStock +
                            " (Current: " + currentStock +
                            (order.getItem().getId().equals(item.getId()) ? ", Existing Order: " + order.getQty() : "")
                            + ")" +
                            ", Requested: " + request.getQty());
        }

        // 5. Determine price: use provided price or default to item's current price
        BigDecimal orderPrice = (request.getPrice() != null) ? request.getPrice() : item.getPrice();

        // 6. Update order (order_no stays the same)
        order.setItem(item);
        order.setQty(request.getQty());
        order.setPrice(orderPrice);

        Order updatedOrder = orderRepository.save(order);
        return convertToResponse(updatedOrder);
    }

    /**
     * Soft delete order
     */
    @Transactional
    public void deleteOrder(String orderNo) {
        Order order = orderRepository.findById(orderNo)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with order number: " + orderNo));
        orderRepository.delete(order); // Soft delete via @SQLDelete
    }

    /**
     * Convert Order entity to OrderResponse DTO
     */
    private OrderResponse convertToResponse(Order order) {
        return new OrderResponse(
                order.getOrderNo(),
                order.getItem().getId(),
                order.getItem().getName(),
                order.getQty(),
                order.getPrice());
    }
}
