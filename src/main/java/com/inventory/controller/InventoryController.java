package com.inventory.controller;

import com.inventory.dto.request.InventoryRequest;
import com.inventory.dto.response.ApiResponse;
import com.inventory.dto.response.InventoryResponse;
import com.inventory.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<InventoryResponse>>> getAllTransactions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<InventoryResponse> inventories = inventoryService.getAllTransactions(pageable);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Inventory transactions retrieved successfully", inventories));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryResponse>> getTransactionById(@PathVariable Long id) {
        InventoryResponse inventory = inventoryService.getTransactionById(id);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Inventory transaction retrieved successfully", inventory));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<InventoryResponse>> recordTransaction(
            @Valid @RequestBody InventoryRequest request) {

        InventoryResponse createdInventory = inventoryService.recordTransaction(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Inventory transaction recorded successfully", createdInventory));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryResponse>> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody InventoryRequest request) {

        InventoryResponse updatedInventory = inventoryService.updateTransaction(id, request);
        return ResponseEntity.ok(
                new ApiResponse<>(true, "Inventory transaction updated successfully", updatedInventory));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTransaction(@PathVariable Long id) {
        inventoryService.deleteTransaction(id);
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(new ApiResponse<>(true, "Inventory transaction deleted successfully", null));
    }
}
