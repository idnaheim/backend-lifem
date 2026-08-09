# Tech Stack

## Runtime & Framework
- **Java 21**
- **Spring Boot 4.0.5** (spring-boot-starter-webmvc, spring-boot-starter-data-jpa)
- **Hibernate / JPA** with `ddl-auto=update`
- **MySQL** (driver: `com.mysql.cj.jdbc.Driver`; dialect: `MySQLDialect`)

## Key Libraries
- **Lombok 1.18.42** — `@Getter`, `@Setter`, `@AllArgsConstructor` on almost every class; avoid generating boilerplate manually
- **Jackson** (`jackson-databind`, `jackson-datatype-jsr310`) — JSON serialization; `@JsonInclude(NON_NULL)` used on `ApiResponse`
- **Spring Security Crypto** — `TextEncryptor` (AES) for password storage; config in `EncryptionConfig`
- **Spring Data JPA Auditing** — `AuditingEntityListener` via `AuditingEntity` base class
- **Spring Boot DevTools** — live reload in dev

## Build System
Maven Wrapper (`mvnw` / `mvnw.cmd`). Use the wrapper, not a globally installed Maven.

## Common Commands

```bash
# Build (skip tests)
./mvnw clean package -DskipTests

# Run locally
./mvnw spring-boot:run

# Run tests
./mvnw test

# Build Docker image (after package)
docker build -t lifem .

# Run container (expects MySQL on host port 3307)
docker run -p 8080:8080 lifem
```

## Database
- Local dev: MySQL on `localhost:3306/lifem`
- Docker: MySQL reachable via `host.docker.internal:3307/lifem`
- Credentials in `application.properties` (not committed to production — replace before deploying)
- Encryption keys (`encryption.password`, `encryption.salt`) also in `application.properties`

## CORS
All origins and methods are permitted globally (`WebConfig`). Tighten before production.
