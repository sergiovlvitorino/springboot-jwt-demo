# Spring Boot JWT Demo

A RESTful API demonstration using Spring Boot 3.5.5, Spring Security 6, and JSON Web Token (JWT) authentication with enhanced security features and best practices.

![Java CI with Maven](https://github.com/sergiovlvitorino/springboot-jwt-demo/workflows/Java%20CI%20with%20Maven/badge.svg)
[![codecov](https://codecov.io/gh/sergiovlvitorino/springboot-jwt-demo/branch/master/graph/badge.svg)](https://codecov.io/gh/sergiovlvitorino/springboot-jwt-demo)

## Features

- **JWT Authentication** - Stateless authentication via Spring OAuth2 Resource Server (Nimbus JOSE)
- **Spring Security 6** - Role-based access control (RBAC) with `@PreAuthorize`
- **Rate Limiting** - 10 login attempts per minute per IP (returns 429)
- **Strong Password Validation** - Enforces secure password policies via custom validator
- **Constructor Injection** - Immutable dependencies following best practices
- **Structured Logging** - SLF4J with profile-specific levels
- **Spring Profiles** - Environment-specific configurations (dev, test, prod)
- **JaCoCo** - Code coverage reporting
- **OpenAPI/Swagger** - API documentation at `/swagger-ui.html`
- **Contract Testing** - Spring Cloud Contract support

## Technology Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.5 |
| Security | Spring Security 6 + OAuth2 Resource Server |
| JWT | Nimbus JOSE (via `spring-boot-starter-oauth2-resource-server`) |
| Database (dev/test) | H2 in-memory |
| Database (prod) | PostgreSQL |
| Build | Maven 3.9+ |
| Tests | JUnit 5 · Mockito · Spring Cloud Contract — **74 tests, 0 failures** |

## Getting Started

### Prerequisites

- **JDK 21** or higher
- **Maven 3.9+**
- (Optional) PostgreSQL for production environment

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

## API Endpoints

### Authentication

| Method | Endpoint | Body | Description | Auth |
|--------|----------|------|-------------|------|
| POST | `/login` | `{"username":"abc@def.com","password":"Test@1234"}` | Sign in — returns JWT in `Authorization` header | No |

### User Management

| Method | Endpoint | Body | Description | Auth |
|--------|----------|------|-------------|------|
| POST | `/rest/user` | `{"name":"John","email":"john@example.com","password":"Test@1234","roleId":"uuid"}` | Create user | Yes |
| PUT | `/rest/user` | `{"id":"uuid","name":"John Doe"}` | Update user name | Yes |
| DELETE | `/rest/user/{id}` | — | Disable user | Yes |
| GET | `/rest/user?pageNumber=0&pageSize=10&orderBy=name&asc=true` | — | List users (paginated) | Yes |
| GET | `/rest/user/count` | — | Count users | Yes |

### Role Management

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/rest/role?pageNumber=0&pageSize=10&orderBy=name&asc=true` | List roles | Yes |
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
| `JWT_SECRET` | Prod | auto | HMAC-SHA256 signing key (min 32 chars) |
| `JWT_EXPIRATION` | No | `864000000` (10d) | Token TTL in milliseconds |
| `CORS_ALLOWED_ORIGINS` | Prod | `http://localhost:3000,http://localhost:4200` | Comma-separated allowed origins |
| `DATABASE_URL` | Prod | H2 in-memory | JDBC connection URL |
| `DATABASE_USERNAME` | Prod | — | Database username |
| `DATABASE_PASSWORD` | Prod | — | Database password |

> **Production note**: `JWT_SECRET` and `CORS_ALLOWED_ORIGINS` have **no default** in the `prod` profile — the application will fail to start if they are not set.

### Application Profiles

| Profile | Database | H2 Console | Logging | Actuator | Health details |
|---------|----------|------------|---------|----------|----------------|
| `dev` | H2 in-memory | Enabled at `/h2-console` | DEBUG | health, info, metrics, env, beans, loggers | always |
| `test` | H2 in-memory | Disabled | WARN | health | never |
| `prod` | PostgreSQL | Disabled | INFO | health, metrics | when-authorized |

## Security Features

1. **JWT via Spring OAuth2 Resource Server** — `BearerTokenAuthenticationFilter` validates tokens using Nimbus JOSE; no third-party JWT library required
2. **Login Rate Limiting** — `LoginRateLimitFilter` blocks IPs after 10 failed attempts per 60s (HTTP 429), honours `X-Forwarded-For`
3. **CORS** — Explicit allowed-origins list; `allowCredentials` disabled (not needed for Bearer tokens)
4. **Password Encryption** — BCrypt hashing via `BCryptPasswordEncoder`
5. **Strong Password Policy** — Enforced at request time via `@StrongPassword` + `ConstraintValidator`
6. **XSS Protection** — `@SafeHtml` strips HTML tags via regex before persistence
7. **H2 Console Isolation** — `DevSecurityConfig` creates a dedicated security chain (`@Order(1)`) for `/h2-console/**` only active in `dev` profile
8. **Secrets Externalization** — No hardcoded credentials; `jwt.secret` fails fast in `prod` without `JWT_SECRET`
9. **Generic 500 Responses** — `RestExceptionHandler` returns `"An unexpected error occurred"` — no exception class or stack trace leaked to clients
10. **Role-Based Access Control** — Fine-grained `@PreAuthorize` on service methods

## Architecture

```
src/main/java/com/sergiovitorino/springbootjwt/
├── application/
│   ├── command/          # Request DTOs (Java Records)
│   └── service/          # Business logic (UserService, RoleService)
├── domain/
│   ├── exception/        # BusinessException, EmailAlreadyExistsException, ResourceNotFoundException
│   ├── model/            # JPA entities (User, Role, Authority)
│   └── repository/       # Spring Data JPA repositories
├── infrastructure/
│   ├── security/
│   │   ├── JwtConfig.java              # JwtDecoder, JwtEncoder, JwtAuthenticationConverter beans
│   │   ├── WebSecurityConfig.java      # Main security filter chain (@Order 2)
│   │   ├── DevSecurityConfig.java      # H2 console chain, @Profile("dev") @Order(1)
│   │   ├── LoginRateLimitFilter.java   # 10 req/min per IP on POST /login
│   │   ├── JWTLoginFilter.java         # Handles /login and issues JWT
│   │   ├── TokenAuthenticationService.java  # Builds JwtClaimsSet and writes Authorization header
│   │   └── UserLogged.java             # Resolves authenticated user UUID from SecurityContext
│   ├── validations/      # @SafeHtml, @StrongPassword validators
│   ├── Config.java        # CORS configuration
│   ├── Initialize.java    # Database seed (dev/test)
│   └── OpenApiConfig.java # Swagger / OpenAPI 3 configuration
└── ui/rest/controller/
    ├── UserRestController.java
    ├── RoleRestController.java
    └── RestExceptionHandler.java
```

## API Documentation

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI spec: `http://localhost:8080/v3/api-docs`

## Testing

```
74 tests — 0 failures
```

| Category | Examples |
|----------|---------|
| Unit | Service, command handler, validator, domain model |
| Integration | Repository, REST controller, security |
| Contract | Spring Cloud Contract (shouldReturnUser) |

## Development

### Building

```bash
mvn clean compile          # Compile
mvn clean package          # Package JAR
mvn clean package -DskipTests  # Skip tests
```

### Code Quality

- Constructor injection with `final` fields
- SLF4J structured logging (no `System.out`)
- Java Records for request DTOs
- Custom `ConstraintValidator` for domain validation
- JaCoCo for coverage reporting

## Troubleshooting

### H2 Console Access (dev profile only)

```
URL:      http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:devdb
Username: sa
Password: (empty)
```

### JWT Secret missing on startup

If you see `Could not resolve placeholder 'JWT_SECRET'`, you are running with the `prod` profile without the environment variable set. Either:
- Switch to `dev` profile (`-Dspring-boot.run.profiles=dev`), or
- Set `export JWT_SECRET=<your-secret>` before starting

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

### Current

- Replaced `jjwt` with `spring-boot-starter-oauth2-resource-server` (Nimbus JOSE) — one less third-party JWT dependency
- Replaced `MapStruct` with inline manual mapping — removed annotation processor complexity
- Replaced `jsoup` HTML sanitization with regex pattern — no external parser needed
- Added `LoginRateLimitFilter` — brute-force protection on `/login`
- Added `DevSecurityConfig` — isolated H2 console security chain for dev profile
- Fixed CORS: removed `allowCredentials(true)` wildcard, origins now configurable via env var
- Fixed JWT key derivation: explicit `StandardCharsets.UTF_8` in `SecretKeySpec`
- Fixed health endpoint: `show-details=when-authorized` (was `always`)
- Fixed `RestExceptionHandler`: generic 500 body — no internal details leaked
- Fixed `User.equals/hashCode`: removed `password` field (BCrypt hash must not define identity)
- Fixed multipart limits: `10MB` instead of `-1` (unlimited)
- Fixed GitHub Actions: upgraded to `actions/checkout@v4`, `actions/setup-java@v4`, JDK 21, Maven cache

### Previous

- Externalized JWT secret to environment variables
- Implemented strong password validation
- Migrated to SLF4J structured logging
- Refactored to constructor injection pattern
- Added Spring Profiles support (dev, test, prod)
- Added JaCoCo code coverage
- Added OpenAPI/Swagger documentation
- Added Spring Cloud Contract testing
