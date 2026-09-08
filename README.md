# Lifem — Personal Life Management API

A production-ready Spring Boot REST API for managing personal finances and scheduling. Lifem provides a structured backend for tracking accounts, income, expenses, calendar events, and credentials — with real-time balance management, event-driven transaction publishing, and Redis-backed caching.

Live Demo:**[https://idnaheim.com](https://idnaheim.com)**

<img width="3834" height="1919" alt="image" src="https://github.com/user-attachments/assets/d1066a65-c1ad-4c8e-9e13-dd22fae0cbb5" />

---

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [API Documentation](#api-documentation)
- [Data Model](#data-model)
- [Configuration](#configuration)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Local Development](#local-development)
  - [Docker](#docker)
- [CI/CD](#cicd)
- [Project Structure](#project-structure)

---

## Overview

Lifem is a single-user personal finance and life management backend. It exposes a RESTful API that tracks every aspect of personal cash flow — from recurring income and expenses to one-off transactions, inter-account transfers, and a scheduled calendar — all wired to an immutable transaction ledger with Kafka event streaming.

---

## Features

### Financial Management
- **Account tracking** — manage bank accounts, cash holdings, and e-wallets with live balance updates
- **Inter-account transfers** — atomically debit the source and credit the destination, with full ledger entries on both sides
- **Income management** — define recurring and one-time income sources; mark them as active or inactive; receive payments with a single action that credits the linked account and writes an immutable ledger entry
- **Expense management** — define expenses with flexible frequencies; pay them with an action that debits the linked account and writes a ledger entry; flag fixed vs. variable amounts
- **Run-rate projections** — aggregate active incomes and expenses into weekly / monthly / quarterly / annual projections automatically derived from each item's configured frequency

### Transaction Ledger
- Every financial action (income receipt, expense payment, transfer, or direct entry) creates an immutable `TransactionEntity` with an auto-generated 10-character reference number
- Balance reversals happen automatically when a transaction is deleted, keeping accounts consistent
- All transaction creates and updates publish a `TransactionEvent` to a Kafka topic for downstream processing or audit

### Calendar
- Full CRUD for personal scheduled events
- Time-range queries return events ordered by start date
- Supports reminders with configurable lead time (minutes before), recurrence frequency, and location

### Password Vault
- AES-encrypted credential storage for external platforms
- Encrypts on write and decrypts on read using Spring Security's `TextEncryptor`
- Stores platform name, username, password, MFA status, and remarks

### Infrastructure
- **Redis caching** — income and expense read paths (list, by-ID, run-rate) are cached with configurable TTLs; all mutating operations evict relevant cache entries granularly
- **Kafka event streaming** — every transaction create or update publishes a `TransactionEvent` to the `lifem.transactions` topic, keyed by reference number
- **OpenAPI / Swagger UI** — interactive API documentation available at [idnaheim.com:8080/swagger-ui/index.html](http://idnaheim.com:8080/swagger-ui/index.html)
- **Spring Data JPA auditing** — every entity automatically tracks `createdBy`, `createdDate`, `modifiedBy`, `modifiedDate`

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.5 |
| Web | Spring MVC (`spring-boot-starter-webmvc`) |
| Persistence | Spring Data JPA + Hibernate (`ddl-auto=update`) |
| Database | MySQL 8 |
| Caching | Redis — `spring-boot-starter-data-redis` + `spring-boot-starter-cache` |
| Messaging | Apache Kafka — `spring-kafka` |
| Encryption | Spring Security Crypto — AES `TextEncryptor` |
| Serialization | Jackson (`jackson-databind` + `jackson-datatype-jsr310`) |
| API Documentation | springdoc-openapi 3.0.0 (OpenAPI 3 / Swagger UI) |
| Boilerplate | Lombok 1.18.42 |
| Build | Maven Wrapper (`mvnw` / `mvnw.cmd`) |
| Container | Docker — multi-stage build on `eclipse-temurin:21-jre-jammy` |
| CI/CD | Azure Pipelines (triggers on `deployments` branch) |

---

## Architecture

```
┌────────────────────────────────────────────────────────────┐
│                        REST Clients                        │
└───────────────────────────┬────────────────────────────────┘
                            │ HTTP
┌───────────────────────────▼────────────────────────────────┐
│               Spring MVC Controllers                        │
│  /accounts  /incomes  /expenses  /transactions              │
│  /calendar/events  /passwords                               │
└────────┬──────────────────────────────────────┬────────────┘
         │ Service calls                         │
┌────────▼───────────────────┐       ┌──────────▼───────────┐
│        Service Layer        │       │   Redis Cache Layer   │
│  Business logic, balance    │◄─────►│  expenses, incomes,  │
│  updates, run-rate calcs    │       │  run-rate projections │
└────────┬────────────────────┘       └──────────────────────┘
         │
┌────────▼────────────────────────────────────────────────────┐
│                  Spring Data JPA Repositories               │
│              (AccountRepository, TransactionRepository, …)  │
└────────┬────────────────────────────────────────────────────┘
         │                              │
┌────────▼──────────┐        ┌──────────▼────────────────────┐
│      MySQL DB      │        │     Apache Kafka              │
│  (JPA ddl=update)  │        │  topic: lifem.transactions    │
└───────────────────┘        └───────────────────────────────┘
```

**Key design decisions:**
- All controller endpoints return `ResponseEntity<CustomResponse<T>>` — a uniform JSON envelope with `success`, `statusCode`, `message`, and `data` fields, with `null` fields omitted from serialization
- The transaction ledger is the single source of truth for all money movements; balance changes always produce a matching ledger entry
- Cache eviction is performed granularly per operation (e.g. `updateIncome` evicts `incomeById:{id}` and `incomes` but not unrelated caches) to minimize cold reads

---

## API Documentation

The full interactive API documentation is available live via Swagger UI:

**[http://idnaheim.com:8080/swagger-ui/index.html](http://idnaheim.com:8080/swagger-ui/index.html)**

All endpoints are browsable, executable, and documented with request/response schemas directly in the UI. Every response follows a consistent JSON envelope:

```json
{
  "success": true,
  "statusCode": 200,
  "message": "Success",
  "data": { ... }
}
```

---

## Data Model

### Enums

| Enum | Values |
|---|---|
| `EnumAccountCategory` | `BANK`, `CASH`, `E_WALLET` |
| `EnumAccountType` | `SAVINGS`, `CHECKING`, `CURRENT`, `CREDIT`, `INVESTMENT` |
| `EnumBaseCategory` | `HOUSING`, `CAR`, `FOOD`, `OFFICE`, `LEISURE`, `HEALTH`, `OTHER`, `TRAVEL`, `SHOPPING`, `SALARY`, `INVESTMENT`, `INTEREST`, `PERSONAL`, `WORK`, `FINANCE` |
| `EnumBaseFrequency` | `ONCE`, `DAILY`, `WEEKLY`, `MONTHLY`, `QUARTERLY`, `BIYEARLY`, `YEARLY`, `UNPLANNED` |
| `EnumTransactionType` | `INCOME`, `EXPENSE`, `TRANSFER` |

All enums are stored as strings in the database (`@Enumerated(EnumType.STRING)`).

### Audit Fields

Every entity extends `AuditingEntity` and automatically carries:

| Field | Type | Description |
|---|---|---|
| `createdBy` | String | Set to `"SYSTEM"` (auth placeholder) |
| `createdDate` | LocalDateTime | Set on first persist |
| `modifiedBy` | String | Updated on every save |
| `modifiedDate` | LocalDateTime | Updated on every save |

---

## Configuration

All sensitive and environment-specific values are externalized. The following environment variables are consumed at runtime:

| Variable | Description | Default (dev) |
|---|---|---|
| `SPRING_DATASOURCE_URL` | JDBC connection URL | `jdbc:mysql://localhost:3306/lifem_test` |
| `SPRING_DATASOURCE_USERNAME` | DB username | `root` |
| `SPRING_DATASOURCE_PASSWORD` | DB password | — |
| `ENCRYPTION_PASSWORD` | AES key for password vault | — |
| `ENCRYPTION_SALT` | Hex salt for AES encryption | — |
| `SPRING_DATA_REDIS_HOST` | Redis hostname | `localhost` |
| `SPRING_DATA_REDIS_PORT` | Redis port | `6379` |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | Kafka broker address | `localhost:9092` |
| `TZ` | Container timezone | `Asia/Manila` |

**Cache TTLs** are tunable via application properties (defaults to 5 minutes each):
```properties
cache.expenses.ttl-minutes=5
cache.incomes.ttl-minutes=5
```

**Kafka topic** is auto-created on startup:
```properties
kafka.topic.transactions=lifem.transactions  # 1 partition, 1 replica
```

---

## Getting Started

### Prerequisites

- Java 21
- Maven (or use the included `mvnw` wrapper)
- MySQL 8
- Redis
- Apache Kafka

### Local Development

1. **Clone the repository:**
   ```bash
   git clone https://github.com/your-org/backend-lifem.git
   cd backend-lifem
   ```

2. **Set up the database:**
   ```sql
   CREATE DATABASE lifem;
   ```

3. **Configure `application.properties`** (or export environment variables) with your local MySQL credentials, Redis host, Kafka broker, and encryption keys.

4. **Run the application:**
   ```bash
   # Windows
   mvnw.cmd spring-boot:run

   # macOS / Linux
   ./mvnw spring-boot:run
   ```

5. **Access Swagger UI:** `http://localhost:8080/swagger-ui/index.html`

**Other useful commands:**

```bash
# Build fat JAR (skipping tests)
./mvnw clean package -DskipTests

# Run tests
./mvnw test

# Generate openapi.yml (requires the app to be running on :8080)
./mvnw integration-test
# Output: openapi.yml at project root
```

### Docker

The `Dockerfile` uses a two-stage build:
- **Stage 1** — Maven build on `eclipse-temurin:21-jdk-jammy`, with dependency caching for faster rebuilds
- **Stage 2** — Minimal runtime on `eclipse-temurin:21-jre-jammy`, copies only the fat JAR

```bash
# Build the image
docker build -t lifem-api .

# Run with environment variables
docker run -d \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/lifem \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD=your_password \
  -e ENCRYPTION_PASSWORD=your_key \
  -e ENCRYPTION_SALT=your_salt \
  --name lifem-api \
  lifem-api
```

> **Note:** Redis and Kafka are not included in `docker-compose.yml` — they are expected to be running on the host or a separate service. The compose file orchestrates the API backend and the frontend only.

```bash
# Full stack with frontend (expects Redis and Kafka on host)
docker-compose up --build
```

---

## CI/CD

The project deploys via **Azure Pipelines** on every push to the `deployments` branch. The pipeline:

1. Stops and removes the existing `api` container
2. Ensures the shared `lifem-net` Docker network exists (used for frontend-to-API communication)
3. Builds a fresh Docker image tagged `api:latest`
4. Starts the new container with production environment variables pointing to Azure Database for MySQL
5. Waits 15 seconds and verifies the container started successfully via `docker logs`

The self-hosted agent runs on the deployment server, making zero-downtime swaps straightforward.

---

## Project Structure

```
backend-lifem/
├── src/
│   └── main/
│       ├── java/com/idnaheim/lifem/
│       │   ├── account/          # Account CRUD + inter-account transfers
│       │   ├── calendar/         # Personal events with time-range queries
│       │   ├── config/           # @Configuration classes (cache, Kafka, encryption, CORS, OpenAPI)
│       │   ├── enums/            # Shared domain enums
│       │   ├── expense/          # Expense CRUD + pay action + run-rate projections
│       │   ├── income/           # Income CRUD + receive action + run-rate projections
│       │   ├── messaging/        # Kafka TransactionEvent record + producer
│       │   ├── password/         # AES-encrypted credential vault
│       │   ├── transaction/      # Immutable ledger with auto-generated reference numbers
│       │   ├── utilities/        # AuditingEntity base class + CustomResponse envelope
│       │   └── LifemApplication.java
│       └── resources/
│           └── application.properties
├── Dockerfile
├── docker-compose.yml
├── azure-pipelines.yml
└── pom.xml
```

Each domain package follows a consistent file convention:

| File | Purpose |
|---|---|
| `{Domain}Entity.java` | JPA entity, extends `AuditingEntity` |
| `{Domain}Repository.java` | `JpaRepository<Entity, Long>` interface |
| `{Domain}Service.java` | Business logic — `@Service @AllArgsConstructor` |
| `{Domain}Controller.java` | REST controller — `@RestController @AllArgsConstructor` |
| `{Domain}Request.java` | Inbound DTO |
| `{Domain}Response.java` | Outbound DTO with static `fromEntity()` factory |

---

## License

This project is for personal and portfolio use. Contact the author for licensing inquiries.
