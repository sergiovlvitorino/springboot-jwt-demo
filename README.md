# Spring Boot JWT Demo

A RESTful API demonstration using Spring Boot 3.5.5, Spring Security 6, and JSON Web Token (JWT) authentication with enhanced security features and best practices.

![Java CI with Maven](https://github.com/sergiovlvitorino/springboot-jwt-demo/workflows/Java%20CI%20with%20Maven/badge.svg)
[![codecov](https://codecov.io/gh/sergiovlvitorino/springboot-jwt-demo/branch/master/graph/badge.svg)](https://codecov.io/gh/sergiovlvitorino/springboot-jwt-demo)

## Features

- **JWT Authentication** — Stateless authentication via Spring OAuth2 Resource Server (Nimbus JOSE)
- **Spring Security 6** — Role-based access control (RBAC) with `@PreAuthorize`
- **Refresh Token** — Token rotation via `POST /auth/refresh` with automatic invalidation
- **Rate Limiting** — Configurable login attempts per minute per IP (returns 429), supports `X-Forwarded-For`
- **Strong Password Validation** — Enforces secure password policies via custom validator
- **DTO Response Layer** — `UserResponse` and `RoleResponse` records prevent entity leakage to API consumers
- **Granular Authorities** — Method-level security with `@EnableMethodSecurity` and authorities like `USER_DELETE`
- **Security Headers** — CSP, HSTS, Referrer-Policy, X-Content-Type-Options, X-Frame-Options
- **Flyway Migrations** — Versioned database schema management (H2 + PostgreSQL)
- **Docker** — Multi-stage Dockerfile + docker-compose for production and development
- **Constructor Injection** — Immutable dependencies following best practices
- **Structured Logging** — SLF4J with profile-specific levels, PII masking
- **Spring Profiles** — Environment-specific configurations (dev, test, prod)
- **JaCoCo** — Code coverage reporting
- **OpenAPI/Swagger** — API documentation (disabled in production)
- **Contract Testing** — Spring Cloud Contract support
- **Desktop Frontend** — Python/CustomTkinter desktop application

## Technology Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.5 |
| Security | Spring Security 6 + OAuth2 Resource Server |
| JWT | Nimbus JOSE (via `spring-boot-starter-oauth2-resource-server`) |
| Database (dev/test) | H2 in-memory (`runtime` scope) |
| Database (prod) | PostgreSQL (`runtime` scope) |
| Build | Maven 3.9+ |
| Cloud | Spring Cloud 2024.0.1 |
| Migrations | Flyway (H2 + PostgreSQL) |
| Tests | JUnit 5, Mockito, Spring Cloud Contract |
| CI/CD | GitHub Actions + JaCoCo + Codecov + OWASP Dependency-Check |
| Containers | Docker multi-stage + docker-compose |
| Frontend | Python 3, CustomTkinter, requests |

## Getting Started

### Prerequisites

- **JDK 21** or higher
- **Maven 3.9+**
- (Optional) PostgreSQL for production environment
- (Optional) Python 3.10+ for the desktop frontend

### Installation

1. Clone the repository:
```bash
git clone https://github.com/sergiovlvitorino/springboot-jwt-demo
cd springboot-jwt-demo
```

2. Run in development mode (H2 in-memory, JWT secret auto-configured):
```bash
mvn spring-boot:run
```

The application starts on `http://localhost:8080`.

### Running with Different Profiles

**Development** (default):
```bash
mvn spring-boot:run
# or explicitly
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Production**:
```bash
# Required environment variables — no defaults in prod
export JWT_SECRET=your-256-bit-secret-key-minimum-32-chars
export CORS_ALLOWED_ORIGINS=https://yourdomain.com
export DATABASE_URL=jdbc:postgresql://localhost:5432/yourdb
export DATABASE_USERNAME=your-username
export DATABASE_PASSWORD=your-password

mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Running Tests

```bash
mvn test
```

Generate coverage report:
```bash
mvn clean test
# Report: target/site/jacoco/index.html
```

### Running the Desktop Frontend

```bash
cd frontend
pip install -r requirements.txt

# CLI mode
python main.py

# Desktop GUI mode
python desktop_app.py
```

## API Endpoints

### Authentication

| Method | Endpoint | Body | Description | Auth |
|--------|----------|------|-------------|------|
| POST | `/login` | `{"username":"abc@def.com","password":"Test@1234"}` | Sign in — returns JWT in `Authorization` header + refresh token in body | No |
| POST | `/auth/refresh` | `{"refreshToken":"uuid-string"}` | Rotate refresh token — returns new access + refresh tokens | No |

**Refresh Token Rotation**: On login, a refresh token (7-day TTL) is returned alongside the access token. Use `POST /auth/refresh` to get a new access token without re-authenticating. Each refresh token is single-use — the old token is invalidated and a new one is issued (rotation). Expired, reused, or tokens for disabled/locked users are rejected.

### User Management

All user endpoints return `UserResponse` DTO (no password or internal fields exposed).

| Method | Endpoint | Body | Description | Auth |
|--------|----------|------|-------------|------|
| POST | `/rest/user` | `{"name":"John","email":"john@example.com","password":"Test@1234","roleId":"uuid"}` | Create user | Yes |
| PUT | `/rest/user` | `{"id":"uuid","name":"John Doe"}` | Update user name | Yes |
| DELETE | `/rest/user/{id}` | — | Disable user | Yes |
| GET | `/rest/user?pageNumber=0&pageSize=10&orderBy=name&asc=true` | — | List users (paginated) | Yes |
| GET | `/rest/user/count` | — | Count users | Yes |

**UserResponse fields**: `id`, `name`, `email`, `enabled`, `roleName`, `dateCreatedAt`, `dateUpdatedAt`

### Role Management

All role endpoints return `RoleResponse` DTO (no entity leakage).

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/rest/role?pageNumber=0&pageSize=10&orderBy=name&asc=true` | List roles (paginated, `pageSize` max 100) | Yes |
| GET | `/rest/role/count` | Count roles | Yes |

## Default Credentials

A default admin user is seeded on startup (dev/test only):

| Field | Value |
|-------|-------|
| Username | `abc@def.com` |
| Password | `Test@1234` |

Password requirements enforced by `@StrongPassword`:
- Minimum 8 characters
- At least 1 uppercase letter
- At least 1 lowercase letter
- At least 1 number
- At least 1 special character (`@$!%*?&`)

## Configuration

### Environment Variables

| Variable | Required | Default (dev) | Description |
|----------|----------|---------------|-------------|
| `JWT_SECRET` | Prod | auto (52 chars) | HMAC-SHA256 signing key (min 32 chars — validated at startup) |
| `JWT_EXPIRATION` | No | `3600000` (1h) | Access token TTL in milliseconds |
| `JWT_REFRESH_EXPIRATION` | No | `604800000` (7d) | Refresh token TTL in milliseconds |
| `RATE_LIMIT_MAX_ATTEMPTS` | No | `10` | Max login attempts per IP per minute |
| `CORS_ALLOWED_ORIGINS` | Prod | `http://localhost:3000,http://localhost:4200` | Comma-separated allowed origins |
| `DATABASE_URL` | Prod | H2 in-memory | JDBC connection URL |
| `DATABASE_USERNAME` | Prod | — | Database username |
| `DATABASE_PASSWORD` | Prod | — | Database password |

> **Production note**: `JWT_SECRET` and `CORS_ALLOWED_ORIGINS` have **no default** in the `prod` profile — the application will fail to start if they are not set. `JWT_SECRET` must be at least 32 characters.

### Application Profiles

| Profile | Database | H2 Console | Logging | Actuator | Swagger |
|---------|----------|------------|---------|----------|---------|
| `dev` | H2 in-memory | Enabled at `/h2-console` | DEBUG | health, info, metrics, env, beans, loggers | Enabled |
| `test` | H2 in-memory | Disabled | WARN | health | Enabled |
| `prod` | PostgreSQL | Disabled | INFO | health, metrics | **Disabled** |

## Security Features

1. **JWT via Spring OAuth2 Resource Server** — `BearerTokenAuthenticationFilter` validates tokens using Nimbus JOSE
2. **Refresh Token Rotation** — Single-use refresh tokens with automatic invalidation; rejects expired, reused, disabled, or locked users
3. **Granular Authorities** — `@EnableMethodSecurity` with fine-grained authorities (e.g., `USER_DELETE`); `UserDetailsAdapter` decouples User from Spring Security
4. **Login Rate Limiting** — `LoginRateLimitFilter` blocks IPs after configurable attempts per 60s (HTTP 429) with scheduled cache cleanup and max 10k entries; also protects `/auth/refresh`
5. **IP Source Validation** — Supports `X-Forwarded-For` for proxied environments (configure `server.forward-headers-strategy`)
6. **DTO Response Layer** — `UserResponse` and `RoleResponse` records prevent entity/password/internal field leakage
7. **Generic Error Responses** — Error codes (`NOT_FOUND`, `BUSINESS_ERROR`) instead of Java class names; 500 returns `"An unexpected error occurred"` with no detail leak
8. **Security Headers** — CSP (Content-Security-Policy), HSTS (Strict-Transport-Security), Referrer-Policy, X-Content-Type-Options (nosniff), X-Frame-Options (DENY)
9. **Actuator Restriction** — Only `/actuator/health` and `/actuator/info` are publicly accessible; all others require authentication
10. **Swagger Disabled in Prod** — `springdoc.api-docs.enabled=false` in production profile
11. **PII Masking** — Failed login attempts log masked email (`a***@def.com`) instead of full address
12. **JWT Secret Validation** — `@PostConstruct` validates `jwt.secret` >= 32 characters at startup — fails fast if too short
13. **CORS** — Explicit allowed-origins list; `allowCredentials` not set (not needed for Bearer tokens)
14. **Password Encryption** — BCrypt hashing via `BCryptPasswordEncoder`
15. **Strong Password Policy** — Enforced at request time via `@StrongPassword` + `ConstraintValidator`
16. **XSS Protection** — `@SafeHtml` strips HTML tags via regex before persistence
17. **Secrets Externalization** — No hardcoded credentials; prod profile fails fast without `JWT_SECRET`

## Architecture

```
src/main/java/com/sergiovitorino/springbootjwt/
├── application/
│   ├── command/
│   │   ├── user/                  # SaveCommand, UpdateCommand, UserResponse, CountCommand, ListCommand
│   │   └── role/                  # CountCommand, ListCommand, RoleResponse
│   └── service/                   # UserService, RoleService, RefreshTokenService (token rotation)
├── domain/
│   ├── exception/                 # BusinessException (sealed), EmailAlreadyExistsException, ResourceNotFoundException, InvalidRefreshTokenException
│   ├── model/                     # User, Role, Authority, RefreshToken, AbstractEntity (@PreUpdate)
│   └── repository/                # Spring Data JPA repositories + RefreshTokenRepository
├── infrastructure/
│   ├── security/
│   │   ├── JwtConfig.java              # JwtDecoder, JwtEncoder, secret validation (@PostConstruct)
│   │   ├── WebSecurityConfig.java      # @EnableMethodSecurity, CSP/HSTS headers, actuator/swagger rules
│   │   ├── DevSecurityConfig.java      # H2 console chain, @Profile("dev") @Order(1)
│   │   ├── LoginRateLimitFilter.java   # Configurable req/min per IP, X-Forwarded-For, protects /auth/refresh
│   │   ├── JWTLoginFilter.java         # Handles /login, issues refresh token, PII masking
│   │   ├── TokenAuthenticationService.java
│   │   ├── UserDetailsAdapter.java     # Decouples User entity from Spring Security UserDetails
│   │   └── UserLogged.java
│   ├── validations/               # @SafeHtml, @StrongPassword validators
│   ├── Config.java                # CORS + PasswordEncoder
│   ├── Initialize.java            # Database seed (@Profile("dev","test") only)
│   └── OpenApiConfig.java         # Swagger / OpenAPI 3 configuration
└── ui/rest/controller/
    ├── AuthController.java        # POST /auth/refresh (token rotation)
    ├── UserRestController.java    # Returns UserResponse DTO
    ├── RoleRestController.java    # Returns RoleResponse DTO
    └── RestExceptionHandler.java  # Sealed switch dispatch, error codes

src/main/resources/db/migration/
├── h2/                            # H2 migrations (dev/test)
│   ├── V1__baseline.sql           # Tables: users, roles, authorities
│   ├── V2__add_indexes.sql        # Performance indexes
│   └── V3__refresh_tokens.sql     # Refresh token table
└── postgresql/                    # PostgreSQL migrations (prod)
    ├── V1__baseline.sql
    ├── V2__add_indexes.sql
    └── V3__refresh_tokens.sql

Dockerfile                         # Multi-stage: maven build + JRE runtime (non-root user)
docker-compose.yml                 # Production: app + PostgreSQL
docker-compose.dev.yml             # Development: PostgreSQL only

frontend/                          # Python desktop client
├── api_client.py                  # HTTP client for all API endpoints
├── main.py                        # CLI interface
├── desktop_app.py                 # CustomTkinter GUI
└── requirements.txt
```

## API Documentation

- Swagger UI: `http://localhost:8080/swagger-ui.html` (dev only)
- OpenAPI spec: `http://localhost:8080/v3/api-docs` (dev only)

## Testing

| Category | Examples |
|----------|---------|
| Unit | Service, domain model, rate limiter, email masking, DTO mapping, validators (100%/96% coverage) |
| Integration | Repository, REST controller, security config, JWT config, refresh token flow |
| Contract | Spring Cloud Contract (shouldReturnUser) |
| Security | Actuator access, error code format, no class name leak, no password in response, security headers, account locked |

## Development

### Building

```bash
mvn clean compile          # Compile
mvn clean package          # Package JAR
mvn clean package -DskipTests  # Skip tests
```

### Code Quality

- Constructor injection with `final` fields
- SLF4J structured logging (no `System.out`, PII masking)
- Java Records for request/response DTOs
- `@UuidGenerator` (Hibernate 6+) for entity IDs
- Custom `ConstraintValidator` for domain validation
- JaCoCo for coverage reporting

### Java 21 Features

| Feature | Where |
|---------|-------|
| `instanceof` pattern matching | `equals()` in User, Role, Authority |
| Sealed classes | `BusinessException sealed permits ResourceNotFoundException, EmailAlreadyExistsException` |
| Switch pattern matching | `RestExceptionHandler` — single handler dispatches by sealed subtype |
| `List.of()` / `.toList()` | Config, JwtConfig, RestExceptionHandler, TokenAuthenticationService |
| `List.getFirst()` | Sequenced Collections API in tests |
| Optional chain | `UserLogged` — `Optional.ofNullable().filter().map().flatMap().orElse()` |

## Docker

### Production (app + PostgreSQL)

```bash
# Required environment variables
export DB_PASSWORD=your-db-password
export JWT_SECRET=your-256-bit-secret-key-minimum-32-chars

# Optional
export CORS_ALLOWED_ORIGINS=https://yourdomain.com
export HOST_PORT=8080

docker compose up -d
```

The app starts on `http://localhost:8080` with PostgreSQL 16, Flyway migrations applied automatically, and a 512MB memory limit.

### Development (PostgreSQL only)

```bash
docker compose -f docker-compose.dev.yml up -d
```

Starts a PostgreSQL instance on `localhost:5432` (user: `postgres`, password: `postgres`, db: `springjwt`). Run the app from your IDE with `spring.profiles.active=dev`.

### Build image manually

```bash
docker build -t springjwt-demo .
```

The Dockerfile uses a multi-stage build (Maven + JRE) and runs as a non-root user.

## CI/CD

GitHub Actions workflow (`.github/workflows/maven.yml`):

| Job | Description |
|-----|-------------|
| **build** | JDK 21, `mvn verify`, JaCoCo report upload, Codecov coverage |
| **dependency-check** | OWASP Dependency-Check for known CVEs |

**Required GitHub Secrets**:
- `CODECOV_TOKEN` — for coverage upload to Codecov
- `NVD_API_KEY` — for OWASP NVD vulnerability database access

## Database Migrations

Flyway manages schema versioning with separate migration paths for H2 (dev/test) and PostgreSQL (prod):

| Version | Description |
|---------|-------------|
| V1 | Baseline — users, roles, authorities tables |
| V2 | Performance indexes on frequently filtered columns |
| V3 | Refresh token table with unique constraint and user FK |

Migrations run automatically on startup. Files are in `src/main/resources/db/migration/{h2,postgresql}/`.

## Troubleshooting

### H2 Console Access (dev profile only)

```
URL:      http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:devdb
Username: sa
Password: (empty)
```

### JWT Secret missing on startup

If you see `JWT secret must be at least 32 characters`, either:
- Switch to `dev` profile (`-Dspring-boot.run.profiles=dev`), or
- Set `export JWT_SECRET=<your-secret-at-least-32-chars>` before starting

### Postman Collection

Import the files from the `postman/` directory:
- `SpringJWTDemo.postman_collection.json` — all endpoints with auto-login pre-request scripts
- `LOCAL.postman_environment.json` — localhost:8080
- `DEV.postman_environment.json` — remote dev server

The collection auto-saves `token`, `refreshToken`, and `roleId` via test scripts.

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Authors

* **Sergio Vitorino** — [GitHub](https://github.com/sergiovlvitorino)

## License

This project is licensed under the GPL-3.0 License — see the [LICENSE.md](LICENSE.md) file for details.

## Changelog

### v6 — Refresh Tokens, Docker, Flyway & Security Enhancements

- **Refresh Token**: `POST /auth/refresh` endpoint with token rotation (single-use, automatic invalidation)
- **Flyway Migrations**: V1 baseline, V2 indexes, V3 refresh_tokens (H2 + PostgreSQL)
- **Docker**: Multi-stage Dockerfile (non-root user, JVM container tuning) + docker-compose (prod + dev)
- **CI/CD**: GitHub Actions with JaCoCo + Codecov + OWASP Dependency-Check (Travis CI removed)
- **Granular Authorities**: `@EnableMethodSecurity`, `USER_DELETE` authority, `UserDetailsAdapter`
- **Security Headers**: Added CSP (Content-Security-Policy) and HSTS (Strict-Transport-Security)
- **Rate Limiting**: Configurable `maxAttempts` via property, `X-Forwarded-For` support, protects `/auth/refresh`
- **RoleResponse** DTO: Role entity no longer exposed to API consumers
- **`@Max(100)` on `pageSize`**: Prevents excessive page requests
- **`@PreUpdate`** automatic audit timestamp
- **`Initialize.java`** restricted to `@Profile({"dev", "test"})`
- **`BusinessException`**: Abstract sealed class with `final` subclasses including `InvalidRefreshTokenException`
- **User creation logic**: Extracted from controller to service layer
- **Tests**: `@MockitoBean`, validators with 100%/96% coverage, accountLocked test, SecurityHeaders test

### v5 — Java 21 Modernization

- `instanceof` pattern matching in `equals()` — User, Role, Authority
- Sealed exception hierarchy: `BusinessException sealed permits ResourceNotFoundException, EmailAlreadyExistsException`
- Switch pattern matching in `RestExceptionHandler` — 3 `@ExceptionHandler` methods merged into 1 with `switch (exception) { case ... }`
- `Arrays.asList()` → `List.of()` in Config.java (immutable, no null elements)
- `Collectors.toList()` → `.toList()` in JwtConfig, RestExceptionHandler
- `Collectors.joining()` → `String.join()` in TokenAuthenticationService
- Optional chain in `UserLogged` — replaced if/null/try-catch with `Optional.ofNullable().filter().map().flatMap().orElse()`
- `List.get(0)` → `List.getFirst()` (Sequenced Collections API) in tests
- Removed unused `Collectors` imports
- 92 tests, 0 failures

### v4 — Architecture Improvements

- Fixed `equals/hashCode` on all entities — now uses only `id` (was using mutable audit fields + collections, causing broken Sets/Maps and Hibernate issues)
- Added `findByEmailWithAuthorities` with `JOIN FETCH` — eliminates N+1 queries during login (3 queries → 1)
- Added `@Transactional` to all service methods (read-only on queries, read-write on mutations)
- Changed `UserRepository.findByEmail` to return `Optional<User>` — null safety enforced at compile time
- Extracted `getAuditUserId()` helper in `UserService` — removed 3x duplicated try/catch blocks
- Added `accountLocked` field to `User` — `isAccountNonLocked()` now functional for Spring Security account locking
- Sanitized `orderBy` parameter with whitelist in `UserService` and `RoleService` — prevents Sort injection
- Renamed `ErrorBean.className` → `errorCode` — semantic naming, no Java internals exposed
- Added `serialVersionUID` to `AbstractEntity` — stable serialization across JVM versions
- Removed `AbstractEntity.equals/hashCode` — subclasses define identity via `id`, not audit timestamps
- Deleted 8 dead-code files: `UserCommandHandler`, `RoleCommandHandler`, `ResponseEntityBuilder`, `Validator` + their tests
- 92 tests (7 dead-code tests removed), 0 failures

### v3 — Security Hardening

- Fixed 14 security vulnerabilities (3 critical, 5 high, 4 medium, 4 low)
- Upgraded Spring Cloud `2023.0.0` → `2024.0.1` (compatible with Boot 3.5.5)
- Added PostgreSQL driver, scoped H2 to `runtime`
- Restricted actuator to `/health` + `/info` only (was `/**`)
- Disabled Swagger in production profile
- Added security headers (Referrer-Policy, X-Content-Type-Options, X-Frame-Options)
- Fixed rate limiter: removed X-Forwarded-For trust, added scheduled cleanup + max cache size
- Added JWT secret minimum length validation (32 chars, fails fast at startup)
- Created `UserResponse` DTO — User entity no longer returned directly in API responses
- Replaced error class names with error codes (`NOT_FOUND`, `BUSINESS_ERROR`, etc.)
- Masked PII in login failure logs (`a***@def.com`)
- Replaced deprecated `@GenericGenerator` with `@UuidGenerator` (Hibernate 6+)
- Lowered default token expiration from 10 days to 1 hour
- Added `@EnableScheduling` for rate limiter cache cleanup
- Added `jwt.secret` to test profile
- 99 tests (25 new security tests), 0 failures

### v2 — Library Modernization

- Replaced `jjwt` with `spring-boot-starter-oauth2-resource-server` (Nimbus JOSE)
- Replaced `MapStruct` with inline manual mapping
- Replaced `jsoup` HTML sanitization with regex pattern
- Added `LoginRateLimitFilter`, `DevSecurityConfig`, `JwtConfig`
- Fixed CORS, JWT key derivation, health endpoint, exception handler
- Fixed `User.equals/hashCode`: removed `password` field
- Fixed multipart limits, GitHub Actions workflow

### v1 — Initial

- Externalized JWT secret to environment variables
- Implemented strong password validation
- Migrated to SLF4J structured logging
- Refactored to constructor injection pattern
- Added Spring Profiles support (dev, test, prod)
- Added JaCoCo, OpenAPI/Swagger, Spring Cloud Contract
