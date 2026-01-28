package com.inventory.config;

import com.inventory.entity.Inventory;
import com.inventory.entity.Item;
import com.inventory.entity.Order;
import com.inventory.repository.InventoryRepository;
import com.inventory.repository.ItemRepository;
import com.inventory.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final ItemRepository itemRepository;
    private final InventoryRepository inventoryRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing sample data from Excel file...");

        // 1. Create Items (7 items)
        Item pen = createItem("Pen", 5.00);
        Item book = createItem("Book", 20.00);
        Item bag = createItem("Bag", 150.00);
        Item pencil = createItem("Pencil", 3.00);
        Item shoe = createItem("Shoe", 300.00);
        Item box = createItem("Box", 75.00);
        Item cap = createItem("Cap", 50.00);

        // 2. Create Inventory Transactions (9 transactions: 8 Top Up, 1 Withdrawal)
        createInventory(pen, 5, "T"); // Pen: +5
        createInventory(book, 10, "T"); // Book: +10
        createInventory(bag, 3, "T"); // Bag: +3
        createInventory(pencil, 8, "T"); // Pencil: +8
        createInventory(shoe, 2, "T"); // Shoe: +2
        createInventory(box, 4, "T"); // Box: +4
        createInventory(cap, 6, "T"); // Cap: +6
        createInventory(pen, 2, "W"); // Pen: -2 (Withdrawal)
        createInventory(book, 3, "T"); // Book: +3 (additional Top Up)

        // 3. Create Orders (10 orders: O1-O10)
        createOrder("O1", pen, 1, 5.00); // Order Pen x1
        createOrder("O2", book, 2, 40.00); // Order Book x2
        createOrder("O3", bag, 1, 150.00); // Order Bag x1
        createOrder("O4", pencil, 3, 9.00); // Order Pencil x3
        createOrder("O5", shoe, 1, 300.00); // Order Shoe x1
        createOrder("O6", box, 1, 75.00); // Order Box x1
        createOrder("O7", cap, 2, 100.00); // Order Cap x2
        createOrder("O8", pen, 1, 5.00); // Order Pen x1
        createOrder("O9", book, 1, 20.00); // Order Book x1
        createOrder("O10", bag, 1, 150.00); // Order Bag x1

        log.info("Sample data initialized successfully!");
        log.info("Items: 7, Inventory Transactions: 9, Orders: 10");
    }

    private Item createItem(String name, double price) {
        Item item = new Item();
        item.setName(name);
        item.setPrice(BigDecimal.valueOf(price));
        item.setIsDeleted(false);
        return itemRepository.save(item);
    }

    private void createInventory(Item item, int qty, String type) {
        Inventory inventory = new Inventory();
        inventory.setItem(item);
        inventory.setQty(qty);
        inventory.setType(type);
        inventory.setIsDeleted(false);
        inventoryRepository.save(inventory);
    }

    private void createOrder(String orderNo, Item item, int qty, double price) {
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setItem(item);
        order.setQty(qty);
        order.setPrice(BigDecimal.valueOf(price));
        order.setIsDeleted(false);
        orderRepository.save(order);
    }
}
