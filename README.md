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
   mvn spring-boot:run
   ```

### Running Tests
Tests use an in-memory H2 database (`src/test/resources/application-test.yml`), so no PostgreSQL instance is needed:
```bash
mvn test
```
Every push and pull request to `main` runs `mvn -B verify` on GitHub Actions (`.github/workflows/ci.yml`).

## Configuration

Settings live in `src/main/resources/application.yml` and can be overridden with environment variables (Spring Boot relaxed binding):

| Environment variable | Default | Description |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/finflow` | JDBC URL of the PostgreSQL database |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Database user |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | Database password |
| `JWT_SECRET` | development key | Base64-encoded HMAC key used to sign JWTs (at least 256 bits). **Always override in production.** |
| `JWT_EXPIRATION` | `86400000` | Token lifetime in milliseconds (24 hours) |
| `SERVER_PORT` | `8080` | HTTP port |

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

### Query Parameters

- `GET /api/transactions` supports `page` (default `0`), `size` (default `10`), `sortBy` (default `transactionDate`), `sortDir` (`asc` or `desc`, default `desc`), and an optional `type` (`INCOME` or `EXPENSE`) or `category` filter.
- `GET /api/budgets`, `GET /api/analytics/monthly` and `GET /api/analytics/categories` accept an optional `month` in `YYYY-MM` format; analytics default to the current month.

### Error Responses

Errors share one JSON shape:
```json
{
  "status": 400,
  "code": "VALIDATION_FAILED",
  "message": "Validation failed",
  "timestamp": "2026-10-22T12:00:00",
  "errors": { "amount": "Amount must be positive" }
}
```

`code` is stable and meant for programmatic handling; `message` is for humans and may change.

| Status | `code` | When |
|---|---|---|
| 400 | `VALIDATION_FAILED` | A request body field failed validation (details in `errors`) |
| 400 | `MALFORMED_REQUEST` | The JSON body could not be parsed (syntax error, wrong type, unknown enum value) |
| 400 | `INVALID_PARAMETER` | A query or path parameter is malformed (e.g. a bad `month`) |
| 401 | `INVALID_CREDENTIALS` | Wrong email or password on login |
| 401 | `UNAUTHENTICATED` | Missing, invalid or expired JWT on a protected endpoint |
| 403 | `ACCESS_DENIED` | Accessing another user's transaction or budget |
| 404 | `RESOURCE_NOT_FOUND` | Transaction or budget not found |
| 404 | `ENDPOINT_NOT_FOUND` | No endpoint at that path |
| 405 | `METHOD_NOT_ALLOWED` | HTTP method not supported by the endpoint |
| 409 | `DUPLICATE_RESOURCE` | Email already registered, or a budget already exists for that category and month |
| 415 | `UNSUPPORTED_MEDIA_TYPE` | Request body is not `application/json` |
| 500 | `INTERNAL_ERROR` | Unexpected server error (details are logged, not returned) |

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
