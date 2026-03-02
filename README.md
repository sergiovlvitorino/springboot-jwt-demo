# Spring Boot JWT Demo

A RESTful API demonstration using Spring Boot 3.5, Spring Security 6, and JSON Web Token (JWT) authentication with enhanced security features and best practices.

![Java CI with Maven](https://github.com/sergiovlvitorino/springboot-jwt-demo/workflows/Java%20CI%20with%20Maven/badge.svg)
[![codecov](https://codecov.io/gh/sergiovlvitorino/springboot-jwt-demo/branch/master/graph/badge.svg)](https://codecov.io/gh/sergiovlvitorino/springboot-jwt-demo)

## Features

- ✅ **JWT Authentication** - Stateless authentication using JSON Web Tokens
- ✅ **Spring Security 6** - Role-based access control (RBAC)
- ✅ **Strong Password Validation** - Enforces secure password policies
- ✅ **Constructor Injection** - Immutable dependencies following best practices
- ✅ **Structured Logging** - SLF4J for better observability
- ✅ **Spring Profiles** - Environment-specific configurations (dev, test, prod)
- ✅ **MapStruct** - Type-safe DTO mapping
- ✅ **JaCoCo** - Code coverage reporting (94% coverage)
- ✅ **OpenAPI/Swagger** - API documentation
- ✅ **Contract Testing** - Spring Cloud Contract support

## Technology Stack

- **Java**: 21
- **Spring Boot**: 3.5.5
- **Spring Security**: 6.x
- **Database**: H2 (dev/test), PostgreSQL (production)
- **JWT**: JJWT 0.13.0
- **Build Tool**: Maven 3.9+
- **Testing**: JUnit 5, Mockito, 78 tests with 100% success rate

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

2. Run the application (development mode):
```bash
mvn spring-boot:run
```

The application will start on `http://localhost:8080` using the H2 in-memory database.

### Running with Different Profiles

**Development** (default):
```bash
mvn spring-boot:run
# or
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

**Production**:
```bash
# Set environment variables first
export JWT_SECRET=your-secret-key-here
export DATABASE_URL=jdbc:postgresql://localhost:5432/yourdb
export DATABASE_USERNAME=your-username
export DATABASE_PASSWORD=your-password

mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Running Tests

Execute all tests:
```bash
mvn test
```

Generate coverage report:
```bash
mvn clean test
# Report available at: target/site/jacoco/index.html
```

## API Endpoints

### Authentication

| Method | Endpoint | Body | Description | Auth Required |
|--------|----------|------|-------------|---------------|
| POST | `/login` | `{"username":"abc@def.com","password":"Test@1234"}` | Sign in and get JWT token | No |

### User Management

| Method | Endpoint | Body | Description | Auth Required |
|--------|----------|------|-------------|---------------|
| POST | `/rest/user` | `{"name":"John","email":"john@example.com","password":"Test@1234","roleId":"uuid"}` | Create user | Yes |
| PUT | `/rest/user` | `{"id":"uuid","name":"John Doe"}` | Update user | Yes |
| DELETE | `/rest/user/{id}` | - | Disable user | Yes |
| GET | `/rest/user?pageNumber=0&pageSize=10&orderBy=name&asc=true&user.enabled=true` | - | List users | Yes |
| GET | `/rest/user/count?user.enabled=true` | - | Count users | Yes |

### Role Management

| Method | Endpoint | Body | Description | Auth Required |
|--------|----------|------|-------------|---------------|
| GET | `/rest/role?pageNumber=0&pageSize=10&orderBy=name&asc=true` | - | List roles | Yes |
| GET | `/rest/role/count?role.name=GUEST` | - | Count roles | Yes |

## Default Credentials

For testing purposes, a default admin user is created on startup:

**Username**: `abc@def.com`
**Password**: `Test@1234`

> **Note**: The password must meet security requirements:
> - Minimum 8 characters
> - At least 1 uppercase letter
> - At least 1 lowercase letter
> - At least 1 number
> - At least 1 special character (@$!%*?&)

## Configuration

### Environment Variables

For security, sensitive configurations use environment variables:

```bash
# JWT Configuration (Required for production)
export JWT_SECRET=your-256-bit-secret-key
export JWT_EXPIRATION=3600000  # 1 hour in milliseconds

# Database Configuration (Required for production)
export DATABASE_URL=jdbc:postgresql://localhost:5432/mydb
export DATABASE_USERNAME=dbuser
export DATABASE_PASSWORD=dbpassword
```

### Application Profiles

**Development** (`application-dev.properties`):
- H2 in-memory database
- H2 console enabled at `/h2-console`
- Debug logging
- All Actuator endpoints exposed

**Production** (`application-prod.properties`):
- PostgreSQL database (via environment variables)
- Minimal logging (INFO level)
- Limited Actuator endpoints (health, metrics)
- Optimized for performance

**Test** (`application-test.properties`):
- H2 in-memory database
- Minimal logging (WARN level)
- Fast test execution

## API Documentation

Access Swagger UI at: `http://localhost:8080/swagger-ui.html`

OpenAPI specification: `http://localhost:8080/v3/api-docs`

## Architecture

```
src/main/java
├── application/          # Application layer
│   ├── command/         # Commands (DTOs)
│   ├── mapper/          # MapStruct mappers
│   └── service/         # Business logic
├── domain/              # Domain layer
│   ├── exception/       # Custom exceptions
│   ├── model/           # Entities
│   └── repository/      # JPA repositories
├── infrastructure/      # Infrastructure layer
│   ├── security/        # JWT & Security config
│   └── validations/     # Custom validators
└── ui/rest/controller/  # REST controllers
```

## Security Features

1. **JWT Token Authentication** - Stateless authentication
2. **Strong Password Policy** - Enforced by custom validator
3. **Password Encryption** - BCrypt hashing
4. **Role-Based Access Control** - Fine-grained permissions
5. **CORS Configuration** - Customizable cross-origin settings
6. **Secret Externalization** - No hardcoded credentials

## Testing

The project includes comprehensive test coverage:

- **Unit Tests**: 42 tests
- **Integration Tests**: 36 tests
- **Total**: 78 tests with 100% success rate
- **Code Coverage**: 94%

Test categories:
- Repository tests
- Service layer tests
- Controller tests (REST API)
- Contract tests
- Security tests

## Development

### Building the Project

```bash
# Clean and compile
mvn clean compile

# Package as JAR
mvn clean package

# Skip tests
mvn clean package -DskipTests
```

### Code Quality

The project follows best practices:
- Constructor injection for dependencies
- Immutable fields with `final`
- SLF4J for structured logging
- Java Records for DTOs
- MapStruct for type-safe mapping

## Troubleshooting

### MapStruct Compilation Issues

If you encounter `ClassNotFoundException` for generated mappers:
```bash
mvn clean compile
mvn compile
```

### H2 Console Access

For development, access H2 console at: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:devdb`
- Username: `sa`
- Password: (empty)

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Authors

* **Sergio Vitorino** - [GitHub](https://github.com/sergiovlvitorino)

See also the list of [contributors](https://github.com/sergiovlvitorino/springboot-jwt-demo/contributors) who participated in this project.

## License

This project is licensed under the GPL-3.0 License - see the [LICENSE.md](LICENSE.md) file for details.

## Changelog

### Recent Improvements

- ✅ Externalized JWT secret to environment variables
- ✅ Implemented strong password validation
- ✅ Migrated to SLF4J structured logging
- ✅ Refactored to constructor injection pattern
- ✅ Added Spring Profiles support (dev, test, prod)
- ✅ Fixed MapStruct compilation issues
- ✅ Increased code coverage from 86% to 94%
- ✅ All 78 tests passing with 100% success rate
