# FinFlow 💰 — Personal Finance & Expense Tracker

A production-style Spring Boot backend for managing income, expenses, monthly budgets, and financial analytics.

## Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Programming language |
| Spring Boot 3.4 | Backend framework |
| Spring Security + JWT | Authentication & authorization |
| Spring Data JPA | Database operations |
| PostgreSQL | Persistent storage |
| Swagger / OpenAPI | API documentation |
| Docker | Containerization |
| Maven | Build management |

## Quick Start

### Prerequisites
- Java 21+
- Maven 3.9+
- PostgreSQL 16+ (or Docker)

### Option 1: Docker Compose (recommended)
```bash
docker compose up -d
```
The API will be available at `http://localhost:8080`.

### Option 2: Local Development
1. **Start PostgreSQL** and create a database called `finflow`:
   ```sql
   CREATE DATABASE finflow;
   ```

2. **Run the application**:
   ```bash
   ./mvnw spring-boot:run
   ```

## API Endpoints

### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/auth/register` | Register a new user |
| POST | `/api/auth/login` | Login and get JWT token |

### Transactions
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/transactions` | Add income or expense |
| GET | `/api/transactions` | List transactions (paginated) |
| GET | `/api/transactions/{id}` | Get one transaction |
| PUT | `/api/transactions/{id}` | Update transaction |
| DELETE | `/api/transactions/{id}` | Delete transaction |

### Budgets
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/budgets` | Create a budget |
| GET | `/api/budgets` | View budgets |
| DELETE | `/api/budgets/{id}` | Delete a budget |

### Analytics
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/analytics/monthly` | Monthly financial summary |
| GET | `/api/analytics/categories` | Spending by category |

## API Documentation

Once the app is running, visit: **http://localhost:8080/swagger-ui.html**

## Usage Examples

### Register
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Soham","email":"soham@example.com","password":"password123"}'
```

### Add an Expense
```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{"amount":250.00,"type":"EXPENSE","category":"FOOD","description":"Lunch","transactionDate":"2026-10-22"}'
```

### Get Monthly Summary
```bash
curl http://localhost:8080/api/analytics/monthly?month=2026-10 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Project Structure

```
src/main/java/com/finflow/
├── config/          # Security, JWT filter, OpenAPI config
├── controller/      # REST controllers
├── dto/             # Request/response DTOs
├── entity/          # JPA entities
├── exception/       # Custom exceptions & global handler
├── mapper/          # Entity ↔ DTO mappers
├── repository/      # Spring Data JPA repositories
├── service/         # Business logic
└── FinFlowApplication.java
```

## License

This project is for learning purposes.
