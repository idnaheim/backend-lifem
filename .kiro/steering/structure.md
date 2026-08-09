# Project Structure

## Root Layout

```
backend-lifem/
├── src/main/java/com/idnaheim/lifem/
│   ├── account/          # Bank/cash/wallet accounts
│   ├── calendar/         # Calendar events
│   ├── config/           # Cross-cutting config (CORS, auditing, encryption, ApiResponse)
│   ├── enums/            # All shared enums
│   ├── events/           # General events
│   ├── expense/          # Expense tracking
│   ├── income/           # Income tracking
│   ├── password/         # Encrypted credential storage
│   ├── transaction/      # Transaction ledger
│   ├── utilities/        # Shared base classes (AuditingEntity)
│   └── LifemApplication.java
├── src/main/resources/
│   └── application.properties
├── Dockerfile
└── pom.xml
```

## Module Convention

Each domain module follows the same file pattern:

| File | Role |
|---|---|
| `*Entity.java` | JPA entity mapped to a DB table; extends `AuditingEntity` |
| `*Repository.java` | Spring Data JPA repository interface |
| `*Service.java` | Business logic; `@Service @AllArgsConstructor` |
| `*Controller.java` | REST controller; `@RestController @RequestMapping("/plural") @AllArgsConstructor` |
| `*Request.java` | Inbound DTO (where needed) |
| `*Response.java` | Outbound DTO with a static `fromEntity()` factory method |

## Key Conventions

- **Entities** extend `AuditingEntity`, implement `Serializable`, and set `serialVersionUID = -1L`
- **Enums** stored as `STRING` in the DB (`@Enumerated(EnumType.STRING)`)
- **Monetary values** use `BigDecimal`, never `double`/`float`
- **Response mapping** is done with a static `fromEntity(Entity e)` method on the response class — no MapStruct or reflection-based mappers
- **Controllers** use raw `ResponseEntity` (no generics on the variable) and rely on `Optional.map()` for 404 handling
- **`ApiResponse<T>`** record is defined in `config/` for wrapping responses — use it for new endpoints for consistency (some older controllers return data directly; prefer `ApiResponse` going forward)
- **Service constructors** use `@AllArgsConstructor` — add dependencies as `private final` fields, not via `@Autowired`
- **Transactions** (`@Transactional`) are annotated at the service method level only when multiple writes happen in one operation
- No authentication/security filter exists yet; `AuditorAwareConfig` returns `"SYSTEM"` as a placeholder
