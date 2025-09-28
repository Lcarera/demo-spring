# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Essential Commands

### Build & Run
```bash
# Development with H2 database
./gradlew bootRun --args='--spring.profiles.active=dev'

# Build application
./gradlew build

# Create Docker image
./gradlew bootBuildImage

# Run tests
./gradlew test

# Run specific test class
./gradlew test --tests "AuthIntegrationTest"

# Run integration tests only
./gradlew test --tests "*Integration*"
```

### Docker Deployment
```bash
# Development stack (H2 database)
docker-compose -f docker-compose.dev.yml up -d

# Production stack (PostgreSQL + Prometheus + Grafana)
docker-compose up -d

# Stop containers
docker-compose down
```

## Architecture Overview

This Spring Boot 3.5.6 application implements Clean Architecture with the following layers:

- **Controllers** (`controller/`): REST API endpoints with OpenAPI documentation
- **Services** (`service/`): Business logic with `@Transactional` annotations
- **Repositories** (`repository/`): Data access with custom JPA queries and `JOIN FETCH` for lazy loading
- **Entities** (`entity/`): Domain models with bidirectional JPA relationships
- **DTOs** (`dto/`): API contracts mapped with MapStruct
- **Configuration** (`config/`): Security, OpenAPI, and application configuration

## Security & Authentication

### JWT Implementation
- Stateless JWT authentication with configurable secrets (minimum 256 bits)
- Token claims: username, authorities, userId
- Environment-specific expiration times

### Authorization Patterns
- `@PreAuthorize("hasRole('ADMIN')")` for admin-only endpoints
- Public endpoints: `/auth/**`, `/public/**`, `/actuator/health/**`
- Admin endpoints: `/actuator/**`

### User Roles
- **USER**: Basic user access
- **ADMIN**: Full system access including user management
- **MODERATOR**: Intermediate privileges

## Database Configuration

### Development Profile
- H2 in-memory database with console at `/h2-console`
- DDL mode: `create-drop`
- DataInitializer creates test users: admin/admin123, user/user123, moderator/moderator123

### Production Profile
- PostgreSQL with Flyway migrations
- DDL mode: `validate`
- Migrations in `src/main/resources/db/migration/`

### Important JPA Patterns
- Use `JOIN FETCH` queries to prevent LazyInitializationException
- Repository methods like `findByUsernameWithRoles()` load related entities
- Example: `@Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.username = :username")`

## Testing Strategy

### Test Structure
- **Unit tests**: Service layer testing with mocks
- **Integration tests**: Full Spring context with TestContainers
- **TestContainers**: PostgreSQL for database integration tests

### Running Tests
- Test profile uses separate H2 database
- Integration tests use `@Testcontainers` with PostgreSQL
- Use `@DirtiesContext` for tests that modify application state

## Environment Profiles

### Development (`application-dev.properties`)
- H2 database with DataInitializer
- Enhanced logging and debugging
- Relaxed security settings

### Production (`application-prod.properties`)
- PostgreSQL with connection pooling
- Flyway migrations enabled
- Security hardening and SSL support

### Environment Variables (Production)
- `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`
- `JWT_SECRET` (must be 32+ characters)
- `JWT_EXPIRATION`, `CORS_ALLOWED_ORIGINS`

## Monitoring & Observability

### Actuator Endpoints
- `/api/v1/actuator/health` - Application health
- `/api/v1/actuator/prometheus` - Metrics (admin only)
- `/api/v1/public/health` - Public health check

### Monitoring Stack
- **Prometheus**: http://localhost:9090 (metrics collection)
- **Grafana**: http://localhost:3000 (admin/admin - visualization)
- Configuration in `docker/prometheus/` and `docker/grafana/`

## API Documentation

### OpenAPI/Swagger
- **Swagger UI**: http://localhost:8080/api/v1/swagger-ui.html
- **OpenAPI spec**: http://localhost:8080/api/v1/v3/api-docs
- JWT Bearer authentication configured in OpenAPI

### Key Endpoints
- `POST /api/v1/auth/signin` - Authentication (requires `usernameOrEmail` field)
- `POST /api/v1/auth/signup` - User registration
- `GET /api/v1/users/me` - Current user profile
- `GET /api/v1/users` - Admin-only user listing with pagination

## Development Notes

### Common Issues
- JWT secrets must be 256+ bits (32+ characters) or authentication fails
- Use `JOIN FETCH` in repository queries to avoid LazyInitializationException
- DataInitializer only runs in dev profile - production needs manual user creation or migrations
- DevTools hot-reload works in IDE but not with `./gradlew bootRun`

### Code Patterns
- Services use `@Transactional(readOnly = true)` for read operations
- DTOs use validation annotations like `@NotBlank`
- MapStruct mappers handle entity-DTO conversion
- Exception handling with custom `ResourceNotFoundException`

### Build Configuration
- Java 25 toolchain
- Spring Boot buildpacks for containerization
- TestContainers BOM for version management
- MapStruct annotation processing configured