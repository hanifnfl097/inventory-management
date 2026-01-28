package com.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String orderNo;
    private Long itemId;
    private String itemName; // For display
    private Integer qty;
    private BigDecimal price;
}
