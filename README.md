# Inventory Management System

A REST API for inventory management built with **Java Spring Boot 3.2.1**, **H2 Database**, and **JPA/Hibernate**.

## Features

✅ 3 Core Modules: **ITEM**, **INVENTORY**, **ORDER**  
✅ Real-time stock calculation from inventory transactions  
✅ Soft delete implementation (`@SQLDelete`, `@Where`)  
✅ Transaction management with `@Transactional`  
✅ Comprehensive validation and exception handling  
✅ Swagger/OpenAPI documentation  
✅ Sample data auto-loaded on startup  

## Tech Stack

- **Java 17**
- **Spring Boot 3.2.1**
- **H2 Database** (in-memory)
- **Spring Data JPA / Hibernate**
- **Lombok**
- **Springdoc OpenAPI** (Swagger)
- **Maven**

## Quick Start

### Prerequisites
- JDK 17 or higher
- Maven 3.6+

### Run Application

```bash
mvn spring-boot:run
```

Application will start on **http://localhost:8080**

### Access Points

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:inventorydb`
  - Username: `sa`
  - Password: _(empty)_

## API Endpoints

### ITEM Module

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/items` | Get all items (paginated, with current stock) |
| GET | `/api/v1/items/{id}` | Get item by ID |
| POST | `/api/v1/items` | Create new item |
| PUT | `/api/v1/items/{id}` | Update item |
| DELETE | `/api/v1/items/{id}` | Delete item (soft delete) |

### INVENTORY Module

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/inventories` | Get all transactions (paginated) |
| GET | `/api/v1/inventories/{id}` | Get transaction by ID |
| POST | `/api/v1/inventories` | Record Top Up (T) or Withdrawal (W) |
| DELETE | `/api/v1/inventories/{id}` | Delete transaction (soft delete) |

### ORDER Module

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/orders` | Get all orders (paginated) |
| GET | `/api/v1/orders/{orderNo}` | Get order by number |
| POST | `/api/v1/orders` | Create new order (auto-generates O1, O2, ...) |
| DELETE | `/api/v1/orders/{orderNo}` | Delete order (soft delete) |

## API Response Format

All endpoints return standardized `ApiResponse`:

```json
{
  "success": true,
  "message": "Items retrieved successfully",
  "data": { ... }
}
```

## Example Requests

### 1. Get All Items

```bash
curl -X GET http://localhost:8080/api/v1/items
```

Response:
```json
{
  "success": true,
  "message": "Items retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "Pen",
        "price": 5.00,
        "currentStock": 1
      }
    ]
  }
}
```

### 2. Create Item

```bash
curl -X POST http://localhost:8080/api/v1/items \
  -H "Content-Type: application/json" \
  -d '{"name": "Notebook", "price": 25.00}'
```

### 3. Top Up Inventory

```bash
curl -X POST http://localhost:8080/api/v1/inventories \
  -H "Content-Type: application/json" \
  -d '{"itemId": 1, "qty": 10, "type": "T"}'
```

### 4. Create Order

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"itemId": 1, "qty": 2, "price": 10.00}'
```

## Stock Calculation Logic

Real-time stock formula:
```
currentStock = SUM(inventory WHERE type='T' AND is_deleted=FALSE)
             - SUM(inventory WHERE type='W' AND is_deleted=FALSE)  
             - SUM(orders.qty WHERE is_deleted=FALSE)
```

## Sample Data

Application auto-loads 7 items:
- Pen (stock: 1)
- Book (stock: 10)
- Bag (stock: 1)
- Pencil (stock: 5)
- Shoe (stock: 1)
- Box (stock: 3)
- Cap (stock: 4)

## Database Schema

**Tables:**
- `item` - Items master data
- `inventory` - Inventory transactions (T/W)
- `orders` - Customer orders

All tables include soft delete fields: `is_deleted`, `deleted_at`

## Testing

Import `InventoryManagement.postman_collection.json` into Postman for comprehensive API testing.

## Author

Inventory Management Team

## License

MIT
