# Inventory Management System

A production-ready REST API for inventory management built with **Java Spring Boot 3.2.1** and **H2 Database**, fulfilling all requirements from the technical specification.

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## 📋 Requirements Coverage

This project implements **100% of the specified requirements**:

### ✅ Requirement 1: H2 Database with Table Relations
- H2 in-memory database configured
- Three tables with proper foreign key relations:
  - `ITEM` (id, name, price)
  - `INVENTORY` (id, item_id, qty, type[T/W])
  - `ORDER` (order_no, item_id, qty, price)

### ✅ Requirement 2: Stock Calculation Logic
```
Current Stock = SUM(INVENTORY.qty WHERE type='T') 
              - SUM(INVENTORY.qty WHERE type='W') 
              - SUM(ORDER.qty)
```
- Real-time calculation on every API call
- Displayed in GET/List ITEM endpoints

### ✅ Requirement 3: API Endpoints

#### Module 1: ITEM
- ✅ **Get** - Get single item by ID
- ✅ **Listing with pagination** - Get all items with page support
- ✅ **Save** - Create new item
- ✅ **Edit** - Update existing item
- ✅ **Delete** - Soft delete item
- ✅ **Stock Display** - Current stock shown in responses

#### Module 2: INVENTORY
- ✅ **Get** - Get single transaction by ID
- ✅ **Listing with pagination** - Get all transactions
- ✅ **Save** - Record Top Up (T) or Withdrawal (W)
- ✅ **Edit** - Update existing transaction
- ✅ **Delete** - Soft delete transaction

#### Module 3: ORDER
- ✅ **Get** - Get single order by order number
- ✅ **Listing with pagination** - Get all orders
- ✅ **Save** - Create new order with stock validation
- ✅ **Edit** - Update existing order
- ✅ **Delete** - Soft delete order
- ✅ **Stock Validation** - Validates stock before order creation

### ✅ Requirement 4: Field Validation
- All mandatory fields validated using Bean Validation (JSR-380)
- Custom validation messages
- Automatic error responses

### ✅ Requirement 5: Exception Handling
- Global exception handler with meaningful error messages
- Custom exceptions: `ResourceNotFoundException`, `InsufficientStockException`
- Standardized error response format

## 🚀 Quick Start

### Prerequisites
- JDK 17 or higher
- Maven 3.6+

### Run Application

```bash
# Clone repository
git clone https://github.com/hanifnfl097/inventory-management.git
cd inventory-management

# Run application
mvn spring-boot:run
```

Application starts on **http://localhost:8080**

### Access Points

| Service | URL | Credentials |
|---------|-----|-------------|
| **Swagger UI** | http://localhost:8080/swagger-ui.html | Interactive API documentation |
| **H2 Console** | http://localhost:8080/h2-console | JDBC URL: `jdbc:h2:mem:inventorydb`<br>User: `sa`<br>Password: _(empty)_ |

## 📡 API Endpoints

### ITEM Module

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/items` | Get all items (paginated) with current stock |
| GET | `/api/v1/items/{id}` | Get item by ID with current stock |
| POST | `/api/v1/items` | Create new item |
| PUT | `/api/v1/items/{id}` | Update item ✨ |
| DELETE | `/api/v1/items/{id}` | Delete item (soft delete) |

### INVENTORY Module

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/inventories` | Get all transactions (paginated) |
| GET | `/api/v1/inventories/{id}` | Get transaction by ID |
| POST | `/api/v1/inventories` | Record Top Up (T) or Withdrawal (W) |
| PUT | `/api/v1/inventories/{id}` | Update transaction ✨ |
| DELETE | `/api/v1/inventories/{id}` | Delete transaction (soft delete) |

### ORDER Module

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/orders` | Get all orders (paginated) |
| GET | `/api/v1/orders/{orderNo}` | Get order by order number |
| POST | `/api/v1/orders` | Create order (auto-generates O1, O2...) |
| PUT | `/api/v1/orders/{orderNo}` | Update order ✨ |
| DELETE | `/api/v1/orders/{orderNo}` | Delete order (soft delete) |

## 📝 Example Usage

### 1. Get All Items (with Stock)

```bash
curl -X GET http://localhost:8080/api/v1/items
```

**Response:**
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
      },
      {
        "id": 2,
        "name": "Book",
        "price": 20.00,
        "currentStock": 10
      }
    ],
    "totalElements": 7,
    "totalPages": 1
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

### 4. Create Order with Stock Validation

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"itemId": 1, "qty": 2}'
```

**Note:** Price is optional (auto-fills from item price)

### 5. Insufficient Stock Example

```bash
curl -X POST http://localhost:8080/api/v1/orders \
  -H "Content-Type: application/json" \
  -d '{"itemId": 1, "qty": 100}'
```

**Error Response:**
```json
{
  "success": false,
  "message": "Insufficient stock for item: Pen. Available: 1, Requested: 100",
  "data": null
}
```

## 🗄️ Database Schema

### Tables

**ITEM**
- `id` BIGINT (PK, Auto Increment)
- `name` VARCHAR(255) NOT NULL
- `price` DECIMAL(10,2) NOT NULL
- `is_deleted` BOOLEAN (Soft Delete)
- `deleted_at` TIMESTAMP

**INVENTORY**
- `id` BIGINT (PK, Auto Increment)
- `item_id` BIGINT (FK → ITEM)
- `qty` INTEGER NOT NULL (≥ 1)
- `type` CHAR(1) ('T' or 'W')
- `is_deleted` BOOLEAN
- `deleted_at` TIMESTAMP

**ORDERS**
- `order_no` VARCHAR(50) (PK)
- `item_id` BIGINT (FK → ITEM)
- `qty` INTEGER NOT NULL (≥ 1)
- `price` DECIMAL(10,2) NOT NULL
- `is_deleted` BOOLEAN
- `deleted_at` TIMESTAMP

### Stock Calculation Formula

```sql
SELECT 
  (SELECT COALESCE(SUM(CASE WHEN type='T' THEN qty ELSE -qty END), 0) 
   FROM inventory 
   WHERE item_id = ? AND is_deleted = false)
  - 
  (SELECT COALESCE(SUM(qty), 0) 
   FROM orders 
   WHERE item_id = ? AND is_deleted = false)
AS current_stock
```

## 🧪 Testing

### Unit Tests (JUnit 5 + Mockito)

```bash
mvn test
```

**Results:**
```
Tests run: 28, Failures: 0, Errors: 0, Skipped: 0
✅ ItemService: 8 tests
✅ InventoryService: 9 tests  
✅ OrderService: 11 tests
```

### API Testing (Postman)

Import `InventoryManagement.postman_collection.json` into Postman:
- 14 pre-configured API requests
- Sample request bodies
- Test all CRUD operations

## 🔒 Security Features

### Beyond Requirements (Bonus)

- **Pessimistic Locking** - Prevents race conditions in concurrent order creation
- **Cross-Item Validation** - Proper stock validation when changing item in updates
- **Negative Quantity Prevention** - Explicit validation in service layer
- **Soft Delete Pattern** - Data retention for audit trail

## 🛠️ Tech Stack

- **Java 17**
- **Spring Boot 3.2.1**
  - Spring Web (REST API)
  - Spring Data JPA
  - Spring Validation
- **H2 Database** (in-memory)
- **Hibernate/JPA**
- **Lombok** (reduce boilerplate)
- **Springdoc OpenAPI 3** (Swagger UI)
- **JUnit 5** (testing framework)
- **Mockito** (mocking framework)
- **Maven** (build tool)

## 📂 Project Structure

```
inventory-management/
├── src/main/java/com/inventory/
│   ├── config/              # Configuration classes
│   ├── controller/          # REST Controllers (ITEM, INVENTORY, ORDER)
│   ├── dto/                 # Data Transfer Objects
│   │   ├── request/         # Request DTOs with validation
│   │   └── response/        # Response DTOs
│   ├── entity/              # JPA Entities with soft delete
│   ├── exception/           # Custom exceptions & global handler
│   ├── repository/          # JPA Repositories with custom queries
│   └── service/             # Business logic with @Transactional
├── src/main/resources/
│   └── application.properties
├── src/test/java/           # Unit tests (28 tests)
├── pom.xml
├── README.md
└── InventoryManagement.postman_collection.json
```

## 📊 Sample Data

Application auto-loads sample data on startup:

**Items:**
- Pen (Rp 5.00, stock: 1)
- Book (Rp 20.00, stock: 10)
- Bag (Rp 150.00, stock: 1)
- Pencil (Rp 3.00, stock: 5)
- Shoe (Rp 300.00, stock: 1)
- Box (Rp 75.00, stock: 3)
- Cap (Rp 50.00, stock: 4)

**Inventory Transactions:** 9 transactions (8 Top Up, 1 Withdrawal)  
**Orders:** 10 orders (O1 to O10)

## 🎯 Key Features

✅ **100% Requirement Coverage** - All Excel specifications implemented  
✅ **Production-Ready** - Transaction management, validation, exception handling  
✅ **Well-Tested** - 28 unit tests, Postman collection  
✅ **Developer-Friendly** - Swagger UI, auto-generated docs, sample data  
✅ **Security Hardened** - Race condition prevention, proper validation  
✅ **Clean Architecture** - Layered design, DTO pattern, soft delete  

## 📖 Documentation

- **README.md** - This file (setup & API docs)
- **Swagger UI** - Interactive API documentation at `/swagger-ui.html`
- **Postman Collection** - `InventoryManagement.postman_collection.json`
- **Code Comments** - Inline documentation explaining business logic

## 🤝 Contributing

This is a technical assessment project. For questions or issues, please contact the repository owner.

## 📄 License

MIT License - feel free to use this as a reference for learning purposes.

## 👨‍💻 Author

**Hanif Naufal**  
GitHub: [@hanifnfl097](https://github.com/hanifnfl097)

---

**Repository:** https://github.com/hanifnfl097/inventory-management
