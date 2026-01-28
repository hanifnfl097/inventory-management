package com.inventory.controller;

import com.inventory.dto.request.OrderRequest;
import com.inventory.dto.response.ApiResponse;
import com.inventory.dto.response.OrderResponse;
import com.inventory.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<OrderResponse> orders = orderService.getAllOrders(pageable);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Orders retrieved successfully", orders));
    }

    @GetMapping("/{orderNo}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable String orderNo) {
        OrderResponse order = orderService.getOrderById(orderNo);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Order retrieved successfully", order));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(@Valid @RequestBody OrderRequest request) {
        OrderResponse createdOrder = orderService.createOrder(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Order created successfully", createdOrder));
    }

    @PutMapping("/{orderNo}")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrder(
            @PathVariable String orderNo,
            @Valid @RequestBody OrderRequest request) {

        OrderResponse updatedOrder = orderService.updateOrder(orderNo, request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Order updated successfully", updatedOrder));
    }

    @DeleteMapping("/{orderNo}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(@PathVariable String orderNo) {
        orderService.deleteOrder(orderNo);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(new ApiResponse<>(true, "Order deleted successfully", null));
    }
}
