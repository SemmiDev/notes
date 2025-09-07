# Notes Application - Development Makefile
.PHONY: help build run test clean docker-up docker-down logs db-reset

# Default target
help:
	@echo "Available commands:"
	@echo "  build        - Compile the application"
	@echo "  run          - Run the application"
	@echo "  test         - Run tests"
	@echo "  clean        - Clean build artifacts"
	@echo "  docker-up    - Start Docker services"
	@echo "  docker-down  - Stop Docker services"
	@echo "  logs         - Show application logs"
	@echo "  db-reset     - Reset database (drop and recreate)"
	@echo "  api-test     - Test API endpoints"
	@echo "  format       - Format code"

# Build application
build:
	./mvnw clean compile

# Run application
run:
	./mvnw spring-boot:run -DskipTests

# Run with specific profile
run-dev:
	./mvnw spring-boot:run -DskipTests -Dspring-boot.run.profiles=dev

run-docker:
	./mvnw spring-boot:run -DskipTests -Dspring-boot.run.profiles=docker

# Test application
test:
	./mvnw test

test-integration:
	./mvnw verify

# Clean build
clean:
	./mvnw clean
	docker system prune -f

# Docker operations
docker-up:
	docker-compose up -d

docker-down:
	docker-compose down

docker-restart:
	docker-compose restart

docker-logs:
	docker-compose logs -f

# Database operations
db-reset:
	docker-compose down
	docker volume rm notes_db_data || true
	docker-compose up -d db
	sleep 10
	./mvnw flyway:migrate

db-migrate:
	./mvnw flyway:migrate

db-info:
	./mvnw flyway:info

# Application logs
logs:
	tail -f logs/notes.log

logs-security:
	tail -f logs/security.log

logs-metrics:
	tail -f logs/metrics.log

# API testing
api-test:
	@echo "Testing health endpoint..."
	curl -s http://localhost:8080/actuator/health | jq .
	@echo "\nTesting API docs..."
	curl -s http://localhost:8080/v3/api-docs | jq . > /dev/null && echo "API docs OK"

api-register:
	curl -X POST http://localhost:8080/api/auth/register \
		-H "Content-Type: application/json" \
		-d '{"username":"testuser","email":"test@example.com","password":"password123"}' | jq .

api-health:
	curl -s http://localhost:8080/actuator/health | jq .

# Code formatting
format:
	./mvnw spotless:apply

format-check:
	./mvnw spotless:check

# Development setup
setup:
	@echo "Setting up development environment..."
	chmod +x mvnw
	docker-compose up -d
	sleep 15
	./mvnw clean compile
	@echo "Setup complete! Run 'make run' to start the application."

# Full application lifecycle
start: docker-up
	sleep 10
	./mvnw spring-boot:run -DskipTests

stop: docker-down

restart: stop start

# Package application
package:
	./mvnw clean package -DskipTests

package-docker:
	./mvnw clean package -DskipTests
	docker build -t notes-app .

# Monitoring
monitor:
	@echo "Application Status:"
	@curl -s http://localhost:8080/actuator/health | jq .status || echo "Application not running"
	@echo "\nDocker Services:"
	@docker-compose ps

# Cleanup everything
clean-all: docker-down clean
	docker volume prune -f
	rm -rf logs/
	rm -rf target/

# Quick development cycle
dev: clean build run

# Production build
prod-build:
	./mvnw clean package -DskipTests -Pprod
	docker build -t notes-app:latest .

# Show application info
info:
	@echo "Notes Application - Development Info"
	@echo "===================================="
	@echo "Application URL: http://localhost:8080"
	@echo "Swagger UI:      http://localhost:8080/swagger-ui.html"
	@echo "API Docs:        http://localhost:8080/v3/api-docs"
	@echo "Health Check:    http://localhost:8080/actuator/health"
	@echo "Database:        PostgreSQL on localhost:5432"
	@echo "Redis/Valkey:    localhost:6379"
	@echo "Memcached:       localhost:11211"
