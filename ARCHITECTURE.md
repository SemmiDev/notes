# Notes Application - Clean Architecture Implementation

## Overview
This document describes the comprehensive Clean Architecture implementation for the Notes application, including authentication, queue processing, object mapping, caching, and advanced logging systems.

## Architecture Layers

### 1. Domain Layer (Enterprise Business Rules)
**Location**: `src/main/java/blog/sammi/lab/notes/domain/`

Contains the core business logic and entities that are independent of any framework or external concerns.

#### Entities
- `BaseUuidEntity` - Base entity with UUID primary key and auditing
- `Auditable` - Auditing fields (created/updated timestamps and users)
- `User` - User entity with authentication and profile fields
- `Note` - Note entity with title, content, and relationships
- `Category` - Category entity for organizing notes
- `Tag` - Tag entity for labeling notes with colors
- `RefreshToken` - JWT refresh token entity with expiration

#### Repository Interfaces (Ports)
- `UserRepository` - User data access interface
- `RefreshTokenRepository` - Refresh token data access interface

#### Domain Services (Ports)
- `EmailService` - Email sending interface
- `JwtService` - JWT token operations interface
- `PasswordEncoder` - Password encoding interface
- `QueueService` - Background job queue interface
- `CacheService` - Caching operations interface
- `JobProcessor` - Job processing interface

### 2. Application Layer (Application Business Rules)
**Location**: `src/main/java/blog/sammi/lab/notes/application/`

Contains application-specific business logic and orchestrates domain entities.

#### DTOs (Data Transfer Objects)
- `RegisterRequest` - User registration data
- `LoginRequest` - User login data
- `AuthResponse` - Authentication response with tokens
- `UserDto` - User data transfer object
- `NoteDto` - Note data transfer object
- `CategoryDto` - Category data transfer object
- `TagDto` - Tag data transfer object

#### Use Cases (Application Services)
- `AuthUseCase` - Authentication business logic orchestration
  - User registration with async OTP email
  - User login with JWT token generation
  - OTP verification and resending
  - Password reset functionality
  - Token refresh mechanism
- `CachedUserService` - User operations with caching strategies

#### Mappers (Object Mapping)
- `UserMapper` - User entity ↔ DTO transformations
- `NoteMapper` - Note entity ↔ DTO transformations
- `CategoryMapper` - Category entity ↔ DTO transformations
- `TagMapper` - Tag entity ↔ DTO transformations

### 3. Infrastructure Layer (Frameworks & Drivers)
**Location**: `src/main/java/blog/sammi/lab/notes/infrastructure/`

Contains framework-specific implementations and external service integrations.

#### Persistence
- `JpaUserRepository` - Spring Data JPA repository for User
- `UserRepositoryAdapter` - Adapter implementing domain UserRepository
- `JpaRefreshTokenRepository` - Spring Data JPA repository for RefreshToken
- `RefreshTokenRepositoryAdapter` - Adapter implementing domain RefreshTokenRepository

#### Email System
- `EmailServiceImpl` - SMTP email service with HTML templates
- `otp-email.html` - Beautiful Tailwind CSS OTP verification template
- `password-reset-email.html` - Professional password reset template

#### Security
- `JwtServiceImpl` - JWT token service implementation using JJWT library
- `PasswordEncoderImpl` - BCrypt password encoder implementation
- `JwtAuthenticationFilter` - JWT authentication filter for Spring Security
- `CustomUserDetailsService` - Spring Security UserDetailsService implementation

#### Queue Processing (Valkey/Redis)
- `ValkeyQueueService` - Queue service implementation with Redisson
- `QueueWorker` - Background job worker with retry policies
- `QueueJob` - Job model with retry count and scheduling
- `EmailJobProcessor` - Email job processor implementation

#### Caching (Memcached)
- `MemcachedCacheService` - Cache service implementation
- `MemcachedConfig` - Memcached client configuration
- Cache strategies: read-through, write-through, cache invalidation

#### Logging & Monitoring
- `RequestCorrelationFilter` - Request correlation ID tracking
- `RequestLoggingInterceptor` - Request/response logging
- `StructuredLogger` - Business events, security events, performance metrics
- `logback-spring.xml` - Advanced logging configuration with JSON output

#### Configuration
- `SecurityConfig` - Spring Security configuration with Swagger access
- `JpaAuditingConfig` - JPA auditing configuration
- `AuditorAwareImpl` - Current user provider for auditing
- `GlobalExceptionHandler` - Global exception handling
- `OpenApiConfig` - Swagger/OpenAPI configuration
- `WebMvcConfig` - Web MVC interceptor configuration

### 4. Presentation Layer (Interface Adapters)
**Location**: `src/main/java/blog/sammi/lab/notes/presentation/`

Contains controllers and DTOs for handling HTTP requests and responses.

#### Controllers
- `AuthController` - REST endpoints for authentication operations with OpenAPI documentation

#### DTOs
- `RegisterRequestDto` - Registration request with validation and schema annotations
- `LoginRequestDto` - Login request with validation and schema annotations
- `ApiResponse<T>` - Generic API response wrapper with schema documentation

## Clean Architecture Ports Pattern

### What are Ports?
**Ports** are interfaces that define contracts between layers. They represent the "holes" in your architecture where external systems can plug in.

### When to Use Ports Folder?
You can organize ports in a dedicated folder when:

```
src/main/java/blog/sammi/lab/notes/
├── domain/
│   ├── entity/          # Domain entities
│   ├── port/           # 📁 PORTS FOLDER (Optional)
│   │   ├── in/         # Inbound ports (use cases)
│   │   └── out/        # Outbound ports (repositories, services)
│   └── service/        # Current approach - ports as services
└── application/
    └── usecase/        # Use case implementations
```

### Current Implementation vs Ports Folder

#### Current Approach (Used in this project):
```java
// Domain services act as outbound ports
domain/service/EmailService.java
domain/service/QueueService.java
domain/service/CacheService.java

// Use cases act as inbound ports
application/usecase/AuthUseCase.java
```

#### Alternative Ports Folder Approach:
```java
// Explicit ports organization
domain/port/in/AuthUseCasePort.java
domain/port/out/EmailPort.java
domain/port/out/QueuePort.java
domain/port/out/CachePort.java
```

### When to Use Ports Folder?
1. **Large Applications**: When you have many interfaces and want clear separation
2. **Team Conventions**: When your team prefers explicit port naming
3. **Hexagonal Architecture**: When strictly following hexagonal architecture patterns
4. **Complex Integrations**: When you have many external system integrations

### Current Project Decision
This project uses **domain services as ports** because:
- ✅ **Simpler structure** for medium-sized applications
- ✅ **Clear naming** (`EmailService`, `QueueService`)
- ✅ **Less folder nesting**
- ✅ **Standard Spring Boot conventions**

## Authentication Flow

### 1. User Registration (Async)
```
POST /api/auth/register
{
  "username": "user123",
  "email": "user@example.com",
  "password": "password123"
}
```
- Creates user with `isActive=false`, `isVerified=false`
- Generates 6-digit OTP with 10-minute expiry
- **Queues email job** for async processing
- Returns immediate response

### 2. Background Email Processing
- **Queue Worker** processes email jobs
- **Retry Policy**: Exponential backoff (2^retry seconds)
- **Dead Letter Queue**: Failed jobs after max retries
- **HTML Templates**: Beautiful Tailwind CSS emails

### 3. OTP Verification (Cached)
```
POST /api/auth/verify-otp?email=user@example.com&otpCode=123456
```
- Validates OTP code and expiry
- Sets `isActive=true`, `isVerified=true`
- **Caches user data** for future lookups
- Clears OTP fields

### 4. User Login (Cached)
```
POST /api/auth/login
{
  "usernameOrEmail": "user123",
  "password": "password123"
}
```
- **Cache lookup** for user data (read-through)
- Validates credentials and account status
- Returns JWT access token and refresh token
- **Updates cache** with fresh data (write-through)

## Queue Processing Architecture

### Producer Pattern
```java
// Enqueue email job
Map<String, Object> payload = new HashMap<>();
payload.put("type", "OTP_VERIFICATION");
payload.put("to", "user@example.com");
payload.put("otpCode", "123456");
queueService.enqueue("email", "EMAIL", payload);
```

### Consumer Pattern
```java
// Background worker processes jobs
@Component
public class QueueWorker {
    // Multiple worker threads
    // Retry policies with exponential backoff
    // Dead letter queue for failed jobs
    // Graceful shutdown handling
}
```

### Queue Types
- **default**: General purpose jobs
- **email**: Email sending jobs
- **notifications**: Push notification jobs

## Caching Strategies

### Read-Through Cache
```java
public Optional<UserDto> findById(UUID id) {
    // 1. Check cache first
    Optional<UserDto> cached = cacheService.get(cacheKey, UserDto.class);
    if (cached.isPresent()) return cached;
    
    // 2. Cache miss - fetch from database
    Optional<User> user = userRepository.findById(id);
    if (user.isPresent()) {
        UserDto userDto = userMapper.toDto(user.get());
        // 3. Write to cache
        cacheService.put(cacheKey, userDto, CACHE_TTL_SECONDS);
        return Optional.of(userDto);
    }
    return Optional.empty();
}
```

### Write-Through Cache
```java
public UserDto save(User user) {
    // 1. Save to database
    User savedUser = userRepository.save(user);
    UserDto userDto = userMapper.toDto(savedUser);
    
    // 2. Update all cache entries
    cacheService.put(USER_CACHE_PREFIX + userDto.id(), userDto, TTL);
    cacheService.put(USER_EMAIL_CACHE_PREFIX + userDto.email(), userDto, TTL);
    cacheService.put(USER_USERNAME_CACHE_PREFIX + userDto.username(), userDto, TTL);
    
    return userDto;
}
```

### Cache Invalidation
```java
public void invalidateUserCache(UUID userId, String email, String username) {
    cacheService.delete(USER_CACHE_PREFIX + userId);
    cacheService.delete(USER_EMAIL_CACHE_PREFIX + email);
    cacheService.delete(USER_USERNAME_CACHE_PREFIX + username);
}
```

## Object Mapping with MapStruct

### Compile-Time Generation
```java
@Mapper(
    componentModel = "spring",
    uses = {UserMapper.class, CategoryMapper.class, TagMapper.class},
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface NoteMapper {
    NoteDto toDto(Note note);
    Note toEntity(NoteDto dto);
    void updateNoteFromDto(NoteDto dto, @MappingTarget Note note);
}
```

### Benefits
- ✅ **Compile-time safety**: No runtime reflection
- ✅ **Performance**: Generated code is optimized
- ✅ **Type safety**: Compile-time error detection
- ✅ **Null handling**: Configurable null value strategies

## Logging & Observability

### Request Correlation
```json
{
  "timestamp": "2025-09-07T21:27:22.123+07:00",
  "level": "INFO",
  "message": "User login successful",
  "correlationId": "abc123-def456",
  "requestId": "req789",
  "userId": "user-uuid",
  "method": "POST",
  "uri": "/api/auth/login",
  "userAgent": "Mozilla/5.0...",
  "remoteAddr": "192.168.1.100"
}
```

### Log Categories
- **Application Logs**: General application flow (`logs/notes.log`)
- **Security Logs**: Authentication events (`logs/security.log`)
- **Performance Logs**: Metrics and timing (`logs/metrics.log`)

### Structured Events
- **Business Events**: User actions and workflows
- **Security Events**: Failed logins, suspicious activity
- **Performance Metrics**: Operation timing and metrics
- **Error Context**: Detailed error information with context

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT FALSE,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    otp_code VARCHAR(6),
    otp_expiry TIMESTAMP,
    reset_token VARCHAR(6),
    reset_token_expiry TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID
);
```

### Notes Table
```sql
CREATE TABLE notes (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    content TEXT,
    user_id UUID NOT NULL,
    category_id UUID,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    created_by UUID,
    updated_by UUID,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (category_id) REFERENCES categories(id)
);
```

## Docker Architecture

### Services
- **app**: Spring Boot application
- **db**: PostgreSQL 17 database
- **valkey**: Redis-compatible queue backend
- **memcached**: High-performance caching layer

### Health Checks
- **Database**: `pg_isready` command
- **Valkey**: `valkey-cli ping`
- **Application**: `/actuator/health` endpoint

### Volumes
- **notes_db_data**: Database persistence
- **notes_valkey_data**: Queue data persistence
- **app_logs**: Application logs

## API Documentation

### Swagger UI
The application includes comprehensive API documentation using OpenAPI 3.0 specification.

**Access URLs:**
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

### Features
- Interactive API testing interface
- JWT Bearer token authentication support
- Request/response schema documentation
- Example values for all parameters
- Organized by functional tags

### Authentication in Swagger
1. Click "Authorize" button in Swagger UI
2. Enter JWT token in format: `Bearer <your-jwt-token>`
3. Test protected endpoints with authentication

## Configuration

### Application Properties
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/notes
    username: ${DB_USERNAME:notes}
    password: ${DB_PASSWORD:notes}
  
  redis:
    host: ${SPRING_REDIS_HOST:localhost}
    port: ${SPRING_REDIS_PORT:6379}
  
  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}

memcached:
  host: ${MEMCACHED_HOST:localhost}
  port: ${MEMCACHED_PORT:11211}

app:
  jwt:
    secret: "your-jwt-secret"
    expiration-ms: 3600000
```

## Dependencies

### Core Dependencies
- Spring Boot 3.5.5
- Spring Security
- Spring Data JPA
- Spring Boot Mail
- Spring Boot Thymeleaf
- PostgreSQL Driver
- Flyway Migration

### Queue Processing
- Spring Boot Data Redis
- Redisson Spring Boot Starter 3.24.3

### Object Mapping
- MapStruct 1.5.5.Final
- MapStruct Processor 1.5.5.Final

### Caching
- Spymemcached 2.12.3
- Spring Boot Cache

### JWT & Security
- JJWT API 0.13.0
- JJWT Implementation 0.13.0
- JJWT Jackson 0.13.0

### Documentation
- SpringDoc OpenAPI UI 2.7.0

## Security Features

### JWT Token Security
- HS256 algorithm with configurable secret
- 1-hour access token expiry
- 7-day refresh token expiry
- Automatic token cleanup on password reset

### Password Security
- BCrypt hashing with default strength
- Minimum 6-character password requirement

### OTP Security
- 6-digit numeric codes
- 10-minute expiry window
- Single-use tokens (cleared after verification)

### Request Security
- CORS configuration
- CSRF protection disabled for API
- JWT authentication filter
- Request correlation tracking

## Performance Optimizations

### Caching Strategy
- **User lookups**: ID, email, username caching
- **Cache TTL**: 1 hour for user data
- **Cache invalidation**: On user updates
- **Read-through**: Automatic cache population
- **Write-through**: Cache updates on writes

### Queue Processing
- **Async email sending**: Non-blocking user registration
- **Retry policies**: Exponential backoff for failed jobs
- **Dead letter queue**: Failed job handling
- **Multiple workers**: Parallel job processing

### Database Optimizations
- **JPA auditing**: Automatic timestamp management
- **Connection pooling**: HikariCP configuration
- **Query optimization**: Proper indexing on email/username

## Monitoring & Observability

### Health Checks
- Database connectivity
- Redis/Valkey connectivity
- Application health status
- Custom health indicators

### Metrics
- Request/response timing
- Cache hit/miss ratios
- Queue processing metrics
- Error rates and patterns

### Logging
- Structured JSON logging
- Request correlation tracking
- Security event monitoring
- Performance metric collection

## Clean Architecture Benefits

### Dependency Rule
- Dependencies point inward toward the domain
- Domain layer has no external dependencies
- Infrastructure depends on domain interfaces
- Application orchestrates domain logic

### Testability
- Business logic isolated in use cases
- Domain entities are framework-independent
- Easy to mock external dependencies
- Clear separation of concerns

### Flexibility
- Can swap implementations without changing business logic
- Database, email service, or cache can be changed independently
- Framework-agnostic domain layer
- Plugin architecture with ports and adapters

### Maintainability
- Clear separation of concerns
- Single responsibility principle
- Open/closed principle for extensions
- Dependency inversion principle

## Future Enhancements

### Planned Features
- **Notes CRUD**: Complete note management system
- **Real-time collaboration**: WebSocket integration
- **File attachments**: Document and image support
- **Search functionality**: Full-text search with Elasticsearch
- **API rate limiting**: Request throttling
- **Audit logging**: Complete audit trail

### Architecture Improvements
- **Event-driven architecture**: Domain events with event sourcing
- **CQRS pattern**: Read/write separation
- **Microservices decomposition**: Service extraction
- **API versioning**: Backward compatibility
- **Circuit breaker**: Resilience patterns
- **Distributed tracing**: Request tracing across services

### Security Enhancements
- **Role-based access control**: User permissions
- **Multi-factor authentication**: TOTP support
- **OAuth2 integration**: Social login
- **Account lockout policies**: Brute force protection
- **Password complexity requirements**: Enhanced security
- **Session management**: Concurrent session control
