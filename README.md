# Lifem — Personal Life Management Backend

A Spring Boot REST API for managing personal finances and life events. Track accounts, income, expenses, transactions, passwords, and calendar events — all in one place.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Domain Modules](#domain-modules)
- [REST API Reference](#rest-api-reference)
- [Kafka Integration](#kafka-integration)
- [Redis Caching](#redis-caching)
- [Data Model](#data-model)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Docker](#docker)
- [Project Structure](#project-structure)

---

## Overview

Lifem is a personal life management backend that helps individuals track financial activity and personal events. Core capabilities:

- **Account management** — bank, cash, and wallet accounts with real-time balance tracking
- **Income & expense tracking** — recurring and one-time entries linked to accounts
- **Transaction ledger** — immutable audit trail with auto-generated reference numbers
- **Event streaming** — Kafka-powered event publishing for every recorded transaction
- **Caching** — Redis-backed account cache to reduce database load
- **Credential vault** — AES-encrypted password/credential storage
- **Calendar & event scheduling** — personal event and calendar management

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.5 |
| Persistence | Spring Data JPA + Hibernate (`ddl-auto=update`) |
| Database | MySQL |
| Caching | Redis via `spring-boot-starter-data-redis` |
| Messaging | Apache Kafka via `spring-kafka` |
| Serialization | Jackson (`jackson-databind`, `jackson-datatype-jsr310`) |
| Security | Spring Security Crypto (AES `TextEncryptor`) |
| Boilerplate reduction | Lombok 1.18.42 |
| Build | Maven Wrapper (`mvnw`) |
| Container | Docker — multi-stage build on `eclipse-temurin:21` |

---

## Architecture

```
HTTP Client
     │
     ▼
[REST Controllers]  ─────────────────────────────────┐
     │                                               │
     ▼                                               ▼
[Service Layer]                              [TransactionEventProducer]
     │                                               │
     ├──► [JPA Repositories] ──► MySQL               ▼
     │                                        Kafka Topic
     ├──► [Redis Cache]                    (lifem.transactions)
     │       └── accounts (TTL: 5 min)              │
     │       └── accountById (TTL: 5 min)           ▼
     │                                    [TransactionEventConsumer]
     └──► [TextEncryptor] (AES)                (in-memory log)
```

**Key flows:**

- Recording income or an expense payment updates the linked account balance, persists a `TransactionEntity`, and publishes a `TransactionEvent` to Kafka — all within a single `@Transactional` boundary.
- Account read operations are served from Redis when cached; any write (create, update, delete, transfer, balance change) evicts the relevant cache entries.
- The Kafka consumer maintains an in-memory ordered log of received `TransactionEvent`s, queryable via `GET /transactions/events`.

---

## Domain Modules

### Accounts
Bank, cash, and wallet accounts. Each account has a `BigDecimal` balance, a category (`AccountCategory`), and a type (`AccountType`).

### Income
Income sources with frequency tracking (weekly, bi-weekly, monthly, quarterly, yearly, one-time). Can be linked to an account and "received" to post a transaction and update the balance.

### Expenses
Recurring and one-time expenses. Can be linked to an account and "paid" to post a transaction and deduct from the balance.

### Transactions
Immutable ledger entries. Every financial action (pay expense, receive income, transfer) creates one or more `TransactionEntity` records with a system-generated 10-character reference number. Each transaction also publishes a Kafka event.

### Calendar Events
Scheduled personal events with start/end times. Supports range queries (`GET /calendar/events/range`).

### Events
General personal events with category and frequency tracking.

### Passwords
AES-encrypted credential storage. The `TextEncryptor` is configured in `EncryptionConfig` using key + salt from `application.properties`. Values are encrypted at rest and decrypted on retrieval.

---

## REST API Reference

All responses follow the `ApiResponse<T>` envelope:

```json
{
  "status": 200,
  "message": "Success",
  "data": { ... }
}
```

### Accounts — `/accounts`

| Method | Path | Description |
|---|---|---|
| GET | `/accounts` | List all accounts (cached) |
| GET | `/accounts/{id}` | Get account by ID (cached) |
| POST | `/accounts` | Create account |
| PUT | `/accounts/{id}` | Update account |
| DELETE | `/accounts/{id}` | Delete account |
| POST | `/accounts/transfer` | Transfer between accounts |

**Transfer request body:**
```json
{
  "fromAccountId": 1,
  "toAccountId": 2,
  "amount": 500.00
}
```

---

### Transactions — `/transactions`

| Method | Path | Description |
|---|---|---|
| GET | `/transactions` | List all transactions |
| GET | `/transactions/{id}` | Get transaction by ID |
| POST | `/transactions` | Create raw transaction |
| PUT | `/transactions/{id}` | Update transaction |
| DELETE | `/transactions/{id}` | Delete and reverse balance |
| POST | `/transactions/expense` | Record expense payment → deducts balance + Kafka event |
| POST | `/transactions/income` | Record income receipt → adds balance + Kafka event |
| GET | `/transactions/events` | View in-memory Kafka event log (newest first) |

**Record expense/income request body:**
```json
{
  "accountId": 1,
  "amount": 250.00,
  "category": "FOOD",
  "remarks": "Grocery run",
  "expenseId": 5
}
```

---

### Income — `/incomes`

| Method | Path | Description |
|---|---|---|
| GET | `/incomes` | List all income sources |
| GET | `/incomes/active` | List active income sources |
| GET | `/incomes/{id}` | Get income by ID |
| POST | `/incomes` | Create income source |
| PUT | `/incomes/{id}` | Update income source |
| DELETE | `/incomes/{id}` | Delete income source |
| POST | `/incomes/{id}/receive` | Receive income → updates account balance |

**Receive income params:** `?accountId=1&amount=5000.00&remarks=Salary`

---

### Expenses — `/expenses`

| Method | Path | Description |
|---|---|---|
| GET | `/expenses` | List all expenses |
| GET | `/expenses/runrate` | Get monthly run-rate calculation |
| GET | `/expenses/{id}` | Get expense by ID |
| POST | `/expenses` | Create expense |
| PUT | `/expenses/{id}` | Update expense |
| DELETE | `/expenses/{id}` | Delete expense |
| POST | `/expenses/{id}/pay` | Pay expense → deducts account balance |

**Pay expense params:** `?accountId=1&amount=100.00&remarks=Electric bill`

---

### Calendar Events — `/calendar/events`

| Method | Path | Description |
|---|---|---|
| GET | `/calendar/events` | List all calendar events |
| GET | `/calendar/events/{id}` | Get event by ID |
| GET | `/calendar/events/range` | Get events in a time range |
| POST | `/calendar/events` | Create calendar event |
| PUT | `/calendar/events/{id}` | Update calendar event |
| DELETE | `/calendar/events/{id}` | Delete calendar event |

**Range query params:** `?start=2026-01-01T00:00:00Z&end=2026-01-31T23:59:59Z`

---

### Events — `/events`

| Method | Path | Description |
|---|---|---|
| GET | `/events` | List all events |
| GET | `/events/{id}` | Get event by ID |
| POST | `/events` | Create event |
| PUT | `/events/{id}` | Update event |
| DELETE | `/events/{id}` | Delete event |

---

### Passwords — `/passwords`

| Method | Path | Description |
|---|---|---|
| GET | `/passwords` | List all credentials (values decrypted) |
| GET | `/passwords/{id}` | Get credential by ID |
| POST | `/passwords` | Store new credential (auto-encrypted) |
| PUT | `/passwords/{id}` | Update credential |
| DELETE | `/passwords/{id}` | Delete credential |

---

## Kafka Integration

Kafka is used to publish a `TransactionEvent` every time income is received or an expense is paid. This provides a decoupled, replayable event stream for any downstream processing.

### Topic

| Topic | Partitions | Replicas |
|---|---|---|
| `lifem.transactions` | 1 | 1 |

The topic name is configurable via `kafka.topic.transactions` in `application.properties`.

### TransactionEvent Schema

```json
{
  "id": 42,
  "referenceNo": "TXN-A1B2C3",
  "accountId": 1,
  "accountName": "BDO Savings",
  "category": "FOOD",
  "type": "EXPENSE",
  "amount": -250.00,
  "remarks": "Grocery run",
  "createdBy": "SYSTEM",
  "createdDate": "2026-08-16T10:30:00"
}
```

### Producer

`TransactionEventProducer` publishes events asynchronously using a `KafkaTemplate<String, TransactionEvent>`. The message key is the `referenceNo`, ensuring ordered delivery per transaction. Producer settings:

- `acks=all` — waits for full ISR acknowledgement before confirming
- `retries=3` — automatic retry on transient failures
- JSON serialization, no type headers

### Consumer

`TransactionEventConsumer` listens on the `lifem.transactions` topic with consumer group `lifem-consumer-group`. Consumed events are stored in a thread-safe `CopyOnWriteArrayList` as an in-memory log. Query the log via:

```
GET /transactions/events
```

Returns events newest-first. Note: this in-memory log is reset on restart — it is intended for short-term observability, not durable storage.

### Configuration

```properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=lifem-consumer-group
spring.kafka.consumer.auto-offset-reset=earliest
kafka.topic.transactions=lifem.transactions
```

---

## Redis Caching

Account data is cached in Redis to reduce repeated database reads.

### Cached Operations

| Cache Name | Trigger | TTL |
|---|---|---|
| `accounts` | `GET /accounts` (list all) | 5 minutes |
| `accountById` | `GET /accounts/{id}` | 5 minutes |

### Cache Invalidation

Any operation that changes account state evicts the relevant cache entries:

- Create, update, delete account → evicts both `accounts` and `accountById`
- Account transfer → evicts both caches (both accounts change)
- Recording income/expense (balance change) → evicts both caches

### Configuration

```properties
spring.cache.type=redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=
cache.accounts.ttl-minutes=5
```

Adjust `cache.accounts.ttl-minutes` to tune cache lifetime without code changes.

---

## Data Model

### Enums

| Enum | Values |
|---|---|
| `AccountCategory` | `BANK`, `CASH`, `WALLET`, `INVESTMENT`, etc. |
| `AccountType` | `SAVINGS`, `CHECKING`, `CREDIT`, etc. |
| `IncomeFrequency` | `WEEKLY`, `BI_WEEKLY`, `MONTHLY`, `QUARTERLY`, `YEARLY`, `ONE_TIME` |
| `ExpenseFrequency` | Same values as `IncomeFrequency` |
| `ExpenseCategory` | `FOOD`, `UTILITIES`, `TRANSPORT`, `ENTERTAINMENT`, etc. |
| `IncomeCategory` | `SALARY`, `FREELANCE`, `BUSINESS`, `INVESTMENT`, etc. |
| `TransactionType` | `INCOME`, `EXPENSE`, `TRANSFER` |
| `TransactionCategory` | Shared categories across income/expense/transfer |
| `EventCategory` | Personal event categories |
| `EventFrequency` | Recurrence options for events |

All enums are stored as `STRING` in the database.

### Audit Fields

Every entity extends `AuditingEntity`, which automatically populates:

| Field | Type | Notes |
|---|---|---|
| `createdBy` | `String` | Set once on insert; currently always `"SYSTEM"` |
| `createdDate` | `LocalDateTime` | Set once on insert |
| `modifiedBy` | `String` | Updated on every save |
| `modifiedDate` | `LocalDateTime` | Updated on every save |

---

## Configuration

All configuration lives in `src/main/resources/application.properties`. Key properties:

```properties
# Database
spring.datasource.url=jdbc:mysql://localhost:3307/lifem_test?...&serverTimezone=Asia/Manila
spring.datasource.username=root
spring.datasource.password=<your-password>
spring.jpa.hibernate.ddl-auto=update

# Encryption (AES)
encryption.password=<your-key>
encryption.salt=<your-salt>

# Server
server.port=8080
application.timezone=GMT+08:00

# Redis
spring.cache.type=redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
cache.accounts.ttl-minutes=5

# Kafka
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=lifem-consumer-group
kafka.topic.transactions=lifem.transactions
```

> **Before deploying:** replace database credentials, encryption key/salt, and tighten CORS settings in `WebConfig`.

---

## Running the Application

### Prerequisites

- Java 21
- MySQL running on port `3306` (or `3307` — match your `datasource.url`)
- Redis running on port `6379`
- Apache Kafka running on port `9092`

### Local setup

1. Create the database:
   ```sql
   CREATE DATABASE lifem_test;
   ```

2. Start Redis (example with Docker):
   ```bash
   docker run -d -p 6379:6379 redis:7-alpine
   ```

3. Start Kafka (example with Docker):
   ```bash
   docker run -d -p 9092:9092 \
     -e KAFKA_NODE_ID=1 \
     -e KAFKA_PROCESS_ROLES=broker,controller \
     -e KAFKA_LISTENERS=PLAINTEXT://:9092,CONTROLLER://:9093 \
     -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
     -e KAFKA_CONTROLLER_QUORUM_VOTERS=1@localhost:9093 \
     -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER \
     -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
     apache/kafka:latest
   ```

4. Update `application.properties` with your credentials.

5. Build and run:
   ```bash
   # Build (skip tests)
   ./mvnw clean package -DskipTests

   # Run
   ./mvnw spring-boot:run
   ```

The API will be available at `http://localhost:8080`.

### Common Commands

```bash
# Build
./mvnw clean package -DskipTests

# Run
./mvnw spring-boot:run

# Run tests
./mvnw test
```

---

## Docker

The `Dockerfile` uses a two-stage build:

1. **Stage 1 (builder):** `eclipse-temurin:21-jdk-jammy` — runs `mvnw clean package -DskipTests`
2. **Stage 2 (runtime):** `eclipse-temurin:21-jre-jammy` — copies the fat JAR and exposes port 8080

```bash
# Build image
docker build -t lifem .

# Run container
# (expects MySQL on host port 3307, Redis and Kafka on default ports)
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://host.docker.internal:3307/lifem_test?..." \
  -e SPRING_DATA_REDIS_HOST=host.docker.internal \
  -e SPRING_KAFKA_BOOTSTRAP_SERVERS=host.docker.internal:9092 \
  lifem
```

---

## Project Structure

```
backend-lifem/
├── src/main/java/com/idnaheim/lifem/
│   ├── account/             # AccountEntity, AccountService, AccountController
│   │                          AccountRepository, AccountResponse, TransferRequest
│   ├── calendar/            # CalendarEventEntity, service, controller, repository
│   ├── config/              # CORS, auditing, encryption, cache (Redis), Kafka producer/consumer/topic
│   ├── enums/               # All shared enums (account, income, expense, transaction, event)
│   ├── events/              # General event management
│   ├── expense/             # Expense tracking with pay action
│   ├── income/              # Income tracking with receive action
│   ├── messaging/           # TransactionEvent (record), TransactionEventProducer, TransactionEventConsumer
│   ├── password/            # AES-encrypted credential storage
│   ├── transaction/         # Transaction ledger; recordExpense / recordIncome publish to Kafka
│   ├── utilities/           # AuditingEntity base class, ApiResponse record
│   └── LifemApplication.java
├── src/main/resources/
│   └── application.properties
├── Dockerfile
└── pom.xml
```
