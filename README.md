# Demo Spring Microservice

A production-ready Spring Boot microservice demonstrating best practices for enterprise applications.

## Features

- 🔐 **Security**: JWT-based authentication with role-based authorization
- 🗄️ **Database**: PostgreSQL with Flyway migrations
- 📊 **Monitoring**: Spring Boot Actuator with Prometheus metrics
- 📖 **Documentation**: OpenAPI 3.0 with Swagger UI
- 🐳 **Containerization**: Docker support with bootBuildImage
- 🧪 **Testing**: Comprehensive test suite with TestContainers
- 🏗️ **Architecture**: Clean architecture with proper layering

## Quick Start

### Prerequisites

- Java 25+
- Docker & Docker Compose

### Build and Run

1. **Build the application:**
   ```bash
   ./gradlew build
   ```

2. **Create Docker image:**
   ```bash
   ./gradlew bootBuildImage
   ```

3. **Run with Docker Compose (Production):**
   ```bash
   docker-compose up -d
   ```

4. **Run with Docker Compose (Development):**
   ```bash
   docker-compose -f docker-compose.dev.yml up -d
   ```

### Development

1. **Run locally with dev profile:**
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=dev'
   ```

2. **Run tests:**
   ```bash
   ./gradlew test
   ```

## API Documentation

Once the application is running, you can access:

- **Swagger UI**: http://localhost:8080/api/v1/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api/v1/v3/api-docs
- **Health Check**: http://localhost:8080/api/v1/public/health
- **Actuator**: http://localhost:8080/api/v1/actuator

## Monitoring

- **Prometheus**: http://localhost:9090
- **Grafana**: http://localhost:3000 (admin/admin)

## Authentication

### Register a new user:
```bash
curl -X POST http://localhost:8080/api/v1/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'
```

### Login:
```bash
curl -X POST http://localhost:8080/api/v1/auth/signin \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "testuser",
    "password": "password123"
  }'
```

### Use JWT token:
```bash
curl -X GET http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Project Structure

```
src/
├── main/
│   ├── java/com/gm2dev/demo_spring/
│   │   ├── config/                    # Configuration classes
│   │   ├── controller/
│   │   │   ├── user/                  # User-related REST controllers
│   │   │   └── HealthController.java  # Public health endpoints
│   │   ├── dto/
│   │   │   ├── user/                  # User-related DTOs
│   │   │   └── ApiGenericResponse.java # Generic API responses
│   │   ├── entity/
│   │   │   └── user/                  # User domain entities (User, Role)
│   │   ├── exception/                 # Global exception handling
│   │   ├── mapper/
│   │   │   └── user/                  # User domain mappers
│   │   ├── repository/
│   │   │   └── user/                  # User domain repositories
│   │   ├── security/                  # Security configuration & JWT
│   │   ├── service/
│   │   │   └── user/                  # User domain business logic
│   │   └── DemoSpringApplication.java # Main application class
│   └── resources/
│       ├── db/migration/              # Flyway migrations
│       ├── application.properties     # Base configuration
│       ├── application-dev.properties # Development profile
│       └── application-prod.properties # Production profile
└── test/                              # Test classes (mirrors main structure)
```

## Environment Variables

### Production
- `DATABASE_URL`: PostgreSQL connection URL
- `DATABASE_USERNAME`: Database username
- `DATABASE_PASSWORD`: Database password
- `JWT_SECRET`: JWT signing secret
- `JWT_EXPIRATION`: JWT expiration time in milliseconds
- `CORS_ALLOWED_ORIGINS`: Comma-separated list of allowed origins

## Technologies Used

- **Framework**: Spring Boot 3.5.6
- **Security**: Spring Security with JWT
- **Database**: PostgreSQL with JPA/Hibernate
- **Migration**: Flyway
- **Build Tool**: Gradle
- **Containerization**: Docker with bootBuildImage
- **Documentation**: OpenAPI 3.0
- **Monitoring**: Actuator, Prometheus, Grafana
- **Testing**: JUnit 5, TestContainers