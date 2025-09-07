# 📝 Notes Application

A modern, feature-rich notes management system built with Spring Boot 3, PostgreSQL, and advanced search capabilities.

## ✨ Features

### 🔐 Authentication & Security
- JWT-based authentication with refresh tokens
- Email verification with OTP
- Password reset functionality
- Secure user isolation

### 📝 Notes Management
- Create, read, update, delete notes
- Full-text search with PostgreSQL FTS (Indonesian language support)
- Advanced filtering by category, tags, and date range
- Rich text content support

### 🏷️ Tags & Categories
- Organize notes with categories and tags
- Color-coded tags with hex color support
- Search and filter tags/categories
- Usage tracking and cascade protection

### 🔍 Advanced Search
- PostgreSQL Full-Text Search with ranking
- Multi-criteria filtering
- Real-time search suggestions
- Search result highlighting

### 📊 Additional Features
- Comprehensive pagination with metadata
- Structured logging with correlation IDs
- Email notifications with beautiful HTML templates
- Queue-based background processing
- Caching with Memcached
- API documentation with Swagger/OpenAPI

## 🛠️ Tech Stack

- **Backend**: Spring Boot 3.5.5, Java 21
- **Database**: PostgreSQL 17 with Full-Text Search
- **Cache**: Memcached
- **Queue**: Redis/Valkey
- **Security**: Spring Security with JWT
- **Documentation**: Swagger/OpenAPI 3
- **Build**: Maven
- **Containerization**: Docker & Docker Compose

## 🚀 Quick Start

### Prerequisites
- Java 21+
- Docker & Docker Compose
- Maven 3.9+

### 1. Clone Repository
```bash
git clone <repository-url>
cd notes
```

### 2. Start Infrastructure
```bash
make docker-up
# or
docker-compose up -d
```

### 3. Run Application
```bash
make run
# or
./mvnw spring-boot:run
```

### 4. Access Application
- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health

## 📋 API Endpoints

### Authentication
```http
POST /api/auth/register          # Register new user
POST /api/auth/login             # User login
POST /api/auth/verify-otp        # Verify email with OTP
POST /api/auth/resend-otp        # Resend OTP
POST /api/auth/refresh-token     # Refresh access token
POST /api/auth/forgot-password   # Request password reset
POST /api/auth/reset-password    # Reset password with OTP
```

### Notes
```http
GET    /api/notes                # List notes with filters
POST   /api/notes                # Create note
GET    /api/notes/{id}           # Get note by ID
PUT    /api/notes/{id}           # Update note
DELETE /api/notes/{id}           # Delete note
GET    /api/notes/search         # Full-text search
GET    /api/notes/category/{id}  # Notes by category
GET    /api/notes/tag/{id}       # Notes by tag
```

### Categories
```http
GET    /api/categories           # List categories
POST   /api/categories           # Create category
GET    /api/categories/{id}      # Get category
PUT    /api/categories/{id}      # Update category
DELETE /api/categories/{id}      # Delete category
```

### Tags
```http
GET    /api/tags                 # List tags with filters
POST   /api/tags                 # Create tag
GET    /api/tags/all             # All user tags
GET    /api/tags/{id}            # Get tag
PUT    /api/tags/{id}            # Update tag
DELETE /api/tags/{id}            # Delete tag
```

## 🔧 Configuration

### Environment Variables
Create `.env` file:
```env
# Database
DATABASE_USER=notes
DATABASE_PASSWORD=notes
DATABASE_DB=notes

# Email (Gmail App Password required)
MAIL_HOST=smtp.gmail.com
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password

# Redis/Valkey
SPRING_REDIS_HOST=localhost
SPRING_REDIS_PORT=6379

# Memcached
MEMCACHED_HOST=localhost
MEMCACHED_PORT=11211
```

### Application Profiles
- `default` - Development with mock email
- `docker` - Docker environment
- `prod` - Production settings

## 📖 Usage Examples

### Register & Login
```bash
# Register new user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "password123"
  }'

# Verify OTP (check console logs for OTP in dev mode)
curl -X POST "http://localhost:8080/api/auth/verify-otp?email=john@example.com&otpCode=123456"

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "johndoe",
    "password": "password123"
  }'
```

### Create Note with Category and Tags
```bash
# Create category
curl -X POST http://localhost:8080/api/categories \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Work",
    "description": "Work related notes"
  }'

# Create tag
curl -X POST http://localhost:8080/api/tags \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Important",
    "color": "#ff0000"
  }'

# Create note
curl -X POST http://localhost:8080/api/notes \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "My Important Note",
    "content": "This is a work-related note with important information.",
    "categoryId": "category-uuid",
    "tagIds": ["tag-uuid"]
  }'
```

### Search Notes
```bash
# Full-text search
curl "http://localhost:8080/api/notes/search?query=important&page=0&size=10" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Advanced filtering
curl "http://localhost:8080/api/notes?search=work&categoryId=uuid&tagIds=uuid1,uuid2&startDate=2025-01-01T00:00:00&page=0&size=10&sort=updatedAt,desc" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 🛠️ Development

### Available Make Commands
```bash
make help           # Show all commands
make setup          # Setup development environment
make dev            # Quick development cycle
make build          # Build application
make test           # Run tests
make docker-up      # Start Docker services
make docker-down    # Stop Docker services
make db-reset       # Reset database
make logs           # Show application logs
make api-test       # Test API endpoints
```

### Database Migrations
```bash
make db-migrate     # Run Flyway migrations
make db-info        # Show migration status
```

### Code Quality
```bash
make format         # Format code with Spotless
make format-check   # Check code formatting
```

## 🏗️ Architecture

### Project Structure
```
src/
├── main/java/blog/sammi/lab/notes/
│   ├── application/          # Use cases & DTOs
│   ├── domain/              # Entities & repositories
│   ├── infrastructure/      # External services & config
│   └── presentation/        # Controllers & DTOs
└── main/resources/
    ├── db/migration/        # Flyway migrations
    └── templates/           # Email templates
```

### Key Design Patterns
- **Clean Architecture** - Separation of concerns
- **CQRS** - Command Query Responsibility Segregation
- **Repository Pattern** - Data access abstraction
- **MapStruct** - Object mapping
- **Strategy Pattern** - Email service implementations

## 🔍 Full-Text Search

### PostgreSQL FTS Features
- **Indonesian Language Support** - Proper stemming and stop words
- **Weighted Search** - Title has higher relevance than content
- **Ranking** - Results sorted by relevance score
- **Performance** - GIN indexes for fast search

### Search Query Examples
```sql
-- Simple search
SELECT * FROM notes WHERE search_vector @@ to_tsquery('indonesian', 'catatan');

-- Phrase search
SELECT * FROM notes WHERE search_vector @@ to_tsquery('indonesian', 'catatan & penting');

-- Ranked results
SELECT *, ts_rank(search_vector, query) as rank 
FROM notes, to_tsquery('indonesian', 'catatan') query
WHERE search_vector @@ query
ORDER BY rank DESC;
```

## 📧 Email Configuration

### Gmail Setup
1. Enable 2-Factor Authentication
2. Generate App Password:
   - Go to [Google App Passwords](https://myaccount.google.com/apppasswords)
   - Select "Mail" → "Other (Notes App)"
   - Copy 16-character password
3. Update `.env` with credentials

### Development Mode
Set `app.email.mock: true` to see OTP codes in console logs instead of sending emails.

## 🐳 Docker Deployment

### Production Build
```bash
make prod-build
docker run -p 8080:8080 notes-app:latest
```

### Docker Compose
```yaml
version: '3.8'
services:
  app:
    image: notes-app:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
    depends_on:
      - db
      - redis
      - memcached
```

## 📊 Monitoring & Observability

### Health Checks
- `/actuator/health` - Application health
- `/actuator/info` - Application info
- `/actuator/metrics` - Application metrics

### Logging
- **Structured Logging** - JSON format with correlation IDs
- **Security Events** - Authentication and authorization logs
- **Performance Metrics** - Request timing and database queries

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- **Documentation**: [Swagger UI](http://localhost:8080/swagger-ui.html)
- **Issues**: Create GitHub issue
- **Email**: your-email@example.com

---

**Built with ❤️ using Spring Boot 3 and modern Java practices**
