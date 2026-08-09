# Lifem — Backend

REST API for the [Lifem](https://github.com/idnaheim/lifem) personal life management dashboard, built with Java 21 and Spring Boot.

![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-brightgreen)
![License](https://img.shields.io/badge/License-MIT-blue)

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.0.5 |
| Persistence | Spring Data JPA + Hibernate |
| Database | MySQL 8.0 |
| Build Tool | Maven (via `mvnw` wrapper) |
| Encryption | Spring Security Crypto (AES) |
| Boilerplate | Lombok |
| Auditing | Spring Data JPA Auditing |

---

## Project Structure

```
src/main/java/com/idnaheim/lifem/
├── LifemApplication.java    # Entry point (@SpringBootApplication, @EnableJpaAuditing)
├── account/                 # Financial accounts (bank, cash, e-wallet)
├── calendar/                # Calendar events with recurrence and reminders
├── config/                  # ApiResponse, WebConfig (CORS), EncryptionConfig
├── enums/                   # All shared enums
├── events/                  # Domain events
├── expense/                 # Expenses with payment tracking
├── income/                  # Income sources
├── password/                # AES-encrypted credential vault
├── transaction/             # Financial transaction ledger
└── utilities/               # AuditingEntity base class
```

Each domain module follows a consistent structure:
```
{module}/
├── {Domain}Entity.java       # JPA entity, extends AuditingEntity
├── {Domain}Repository.java   # Spring Data JPA repository
├── {Domain}Service.java      # Business logic
└── {Domain}Controller.java   # REST controller
```

---

## Getting Started

### Prerequisites

- Java 21
- MySQL 8.0 with a database named `lifem`

### Configuration

Open `src/main/resources/application.properties`. By default the Docker datasource is active. For local dev without Docker, switch to the local block:

```properties
# ── Local ──────────────────────────────────────────────────────────────────
spring.datasource.url=jdbc:mysql://localhost:3306/lifem?useLegacyDatetimeCode=false&serverTimezone=Asia/Manila&allowPublicKeyRetrieval=true&useSSL=false
spring.datasource.username=root
spring.datasource.password=your_password

# ── Comment out the Docker block ────────────────────────────────────────────
# spring.datasource.url=jdbc:mysql://host.docker.internal:3307/lifem...
```

### Run

```bash
./mvnw spring-boot:run
```

API available at `http://localhost:8080`.

### Build JAR

```bash
./mvnw clean package
```

### Run Tests

```bash
./mvnw test
```

---

## Running with Docker

This service is orchestrated in the [parent repository](https://github.com/idnaheim/lifem):

```bash
git clone --recurse-submodules https://github.com/idnaheim/lifem.git
cd lifem
docker compose up --build
```

---

## API Reference

All endpoints are prefixed with `http://localhost:8080`. CORS is open to all origins.

### Accounts `/accounts`

| Method | Path | Description |
|---|---|---|
| `GET` | `/accounts` | Get all accounts |
| `GET` | `/accounts/{id}` | Get account by ID |
| `POST` | `/accounts` | Create account |
| `PUT` | `/accounts/{id}` | Update account |
| `DELETE` | `/accounts/{id}` | Delete account |
| `POST` | `/accounts/transfer` | Transfer between accounts |

**Account fields:**
```json
{
  "name": "string",
  "balance": 0.00,
  "category": "BANK | CASH | E_WALLET",
  "type": "SAVINGS | CHECKING | CURRENT | CREDIT | INVESTMENT",
  "remarks": "string"
}
```

**Transfer request:**
```json
{
  "fromAccountId": 1,
  "toAccountId": 2,
  "amount": 500.00
}
```

---

### Expenses `/expenses`

| Method | Path | Description |
|---|---|---|
| `GET` | `/expenses` | Get all expenses (with paid status and missed payments) |
| `GET` | `/expenses/{id}` | Get expense by ID |
| `GET` | `/expenses/runrate` | Get monthly and annual expense run rate |
| `POST` | `/expenses` | Create expense |
| `PUT` | `/expenses/{id}` | Update expense |
| `DELETE` | `/expenses/{id}` | Delete expense |
| `POST` | `/expenses/{id}/pay?accountId={id}&amount={amount}&remarks={remarks}` | Pay an expense from an account |

**Expense fields:**
```json
{
  "name": "string",
  "frequency": "WEEKLY | MONTHLY | QUARTERLY | ONE_TIME",
  "amount": 0.00,
  "description": "string",
  "paymentStartDate": "2024-01-01T00:00:00Z",
  "category": "HOUSING | CAR | FOOD | OFFICE | LEISURE | HEALTH",
  "isFixedAmount": true
}
```

---

### Incomes `/incomes`

| Method | Path | Description |
|---|---|---|
| `GET` | `/incomes` | Get all incomes |
| `GET` | `/incomes/active` | Get active incomes only |
| `GET` | `/incomes/{id}` | Get income by ID |
| `POST` | `/incomes` | Create income |
| `PUT` | `/incomes/{id}` | Update income |
| `DELETE` | `/incomes/{id}` | Delete income |
| `POST` | `/incomes/{id}/receive?accountId={id}&amount={amount}&remarks={remarks}` | Receive income into an account |

**Income fields:**
```json
{
  "name": "string",
  "source": "string",
  "amount": 0.00,
  "category": "SALARY | FREELANCE | BUSINESS | INVESTMENT | RENTAL | DIVIDEND | INTEREST | SIDE_HUSTLE | OTHER",
  "frequency": "WEEKLY | BI_WEEKLY | MONTHLY | QUARTERLY | YEARLY | ONE_TIME",
  "accountId": 1,
  "isActive": true,
  "remarks": "string"
}
```

---

### Transactions `/transactions`

| Method | Path | Description |
|---|---|---|
| `GET` | `/transactions` | Get all transactions |
| `GET` | `/transactions/{id}` | Get transaction by ID |
| `POST` | `/transactions` | Create a manual transaction |
| `PUT` | `/transactions/{id}` | Update transaction |
| `DELETE` | `/transactions/{id}` | Delete transaction |
| `POST` | `/transactions/expense` | Record an expense transaction |
| `POST` | `/transactions/income` | Record an income transaction |

**Transaction fields:**
```json
{
  "accountId": 1,
  "expenseId": 1,
  "incomeId": 1,
  "type": "INCOME | EXPENSE | TRANSFER",
  "category": "HOUSING | CAR | FOOD | OFFICE | TRAVEL | LEISURE | SHOPPING | HEALTH | SALARY | INVESTMENT | OTHER | TRANSFER",
  "amount": 0.00,
  "remarks": "string"
}
```

> Transactions auto-generate a 10-character uppercase reference number on creation.

---

### Passwords `/passwords`

| Method | Path | Description |
|---|---|---|
| `GET` | `/passwords` | Get all credentials (decrypted) |
| `GET` | `/passwords/{id}` | Get credential by ID |
| `POST` | `/passwords` | Store new credential (encrypted at rest) |
| `PUT` | `/passwords/{id}` | Update credential |
| `DELETE` | `/passwords/{id}` | Delete credential |

**Password fields:**
```json
{
  "platform": "string",
  "username": "string",
  "password": "string",
  "hasMFA": false,
  "remarks": "string"
}
```

> Passwords are encrypted using AES via Spring Security Crypto before being stored in the database.

---

### Calendar `/calendar/events`

| Method | Path | Description |
|---|---|---|
| `GET` | `/calendar/events` | Get all events |
| `GET` | `/calendar/events/{id}` | Get event by ID |
| `GET` | `/calendar/events/range?start={instant}&end={instant}` | Get events within a date range |
| `POST` | `/calendar/events` | Create event |
| `PUT` | `/calendar/events/{id}` | Update event |
| `DELETE` | `/calendar/events/{id}` | Delete event |

**Calendar event fields:**
```json
{
  "title": "string",
  "description": "string",
  "startDate": "2024-01-01T00:00:00Z",
  "endDate": "2024-01-01T01:00:00Z",
  "allDay": false,
  "category": "PERSONAL | WORK | HEALTH | FINANCE | TRAVEL | OTHER",
  "frequency": "ONCE | DAILY | WEEKLY | MONTHLY | YEARLY",
  "location": "string",
  "reminderEnabled": true,
  "reminderMinutesBefore": 30
}
```

---

## Data Models

### Auditing Fields

All entities include these fields automatically:

| Field | Description |
|---|---|
| `createdBy` | Who created the record |
| `createdDate` | When it was created |
| `modifiedBy` | Who last modified it |
| `modifiedDate` | When it was last modified |

---

## Configuration Reference

| Property | Description |
|---|---|
| `spring.datasource.url` | JDBC connection string |
| `spring.datasource.username` | Database username |
| `spring.datasource.password` | Database password |
| `spring.jpa.hibernate.ddl-auto` | Schema strategy (`update` by default) |
| `encryption.password` | AES encryption key for credential vault |
| `encryption.salt` | AES encryption salt (hex string) |
| `application.timezone` | `GMT+08:00` (Asia/Manila) |

> ⚠️ Do not commit real encryption keys or credentials to source control.

---

## License

MIT
