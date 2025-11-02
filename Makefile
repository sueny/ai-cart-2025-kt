# ProcureFlow - Makefile
# AI-powered procurement application with Spring Boot + React

.PHONY: help install build test clean run docker-up docker-down docker-build docker-clean

# Default target
.DEFAULT_GOAL := help

##@ General

help: ## Display this help message
	@echo "ProcureFlow - Development Commands"
	@echo "===================================="
	@echo ""
	@awk 'BEGIN {FS = ":.*##"; printf "\nUsage:\n  make \033[36m<target>\033[0m\n"} /^[a-zA-Z_-]+:.*?##/ { printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2 } /^##@/ { printf "\n\033[1m%s\033[0m\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

##@ Setup & Installation

install: install-backend install-frontend install-e2e ## Install all dependencies
	@echo "✅ All dependencies installed successfully"

install-backend: ## Install backend dependencies (Gradle wrapper)
	@echo "📦 Setting up backend..."
	@cd backend && ./gradlew.bat build -x test

install-frontend: ## Install frontend dependencies (npm)
	@echo "📦 Installing frontend dependencies..."
	@cd frontend && npm install

install-e2e: ## Install E2E test dependencies (Playwright)
	@echo "📦 Installing E2E test dependencies..."
	@cd frontend && npm install
	@cd frontend && npx playwright install

##@ Building

build: build-backend build-frontend ## Build both backend and frontend
	@echo "✅ Build completed successfully"

build-backend: ## Build backend (Gradle)
	@echo "🔨 Building backend..."
	@cd backend && ./gradlew.bat build

build-backend-skip-tests: ## Build backend without running tests
	@echo "🔨 Building backend (skipping tests)..."
	@cd backend && ./gradlew.bat build -x test

build-frontend: ## Build frontend (npm)
	@echo "🔨 Building frontend..."
	@cd frontend && npm run build

##@ Testing

test: test-backend test-frontend ## Run all tests
	@echo "✅ All tests completed"

test-backend: ## Run backend unit and integration tests
	@echo "🧪 Running backend tests..."
	@cd backend && ./gradlew.bat test

test-backend-unit: ## Run only backend unit tests (fast)
	@echo "🧪 Running backend unit tests..."
	@cd backend && ./gradlew.bat test --tests "*Test" -x integrationTest

test-frontend: ## Run frontend tests (if available)
	@echo "🧪 Running frontend tests..."
	@cd frontend && npm test || echo "No frontend tests configured"

test-e2e: ## Run end-to-end tests with Playwright
	@echo "🧪 Running E2E tests..."
	@cd frontend && npm run test:e2e

test-e2e-ui: ## Run E2E tests in UI mode
	@echo "🧪 Running E2E tests in UI mode..."
	@cd frontend && npm run test:e2e:ui

test-e2e-headed: ## Run E2E tests in headed mode (visible browser)
	@echo "🧪 Running E2E tests in headed mode..."
	@cd frontend && npm run test:e2e:headed

test-coverage: ## Generate test coverage report
	@echo "📊 Generating test coverage..."
	@cd backend && ./gradlew.bat test jacocoTestReport
	@echo "Coverage report: backend/build/reports/jacoco/test/html/index.html"

##@ Running

run: ## Run full stack (requires PostgreSQL)
	@echo "⚠️  Note: PostgreSQL must be running on localhost:5432"
	@echo "🚀 Starting backend and frontend..."
	@start cmd /k "cd backend && ./gradlew.bat bootRun"
	@timeout /t 5 /nobreak > nul
	@start cmd /k "cd frontend && npm run dev"
	@echo "✅ Application started!"
	@echo "   Backend:  http://localhost:8080"
	@echo "   Frontend: http://localhost:5173"

run-backend: ## Run backend only
	@echo "🚀 Starting backend..."
	@cd backend && ./gradlew.bat bootRun

run-frontend: ## Run frontend only
	@echo "🚀 Starting frontend..."
	@cd frontend && npm run dev

run-frontend-preview: ## Run frontend production preview
	@echo "🚀 Starting frontend preview..."
	@cd frontend && npm run preview

##@ Docker

docker-up: ## Start all services with docker-compose
	@echo "🐳 Starting Docker containers..."
	@docker-compose up -d
	@echo "✅ Services started!"
	@echo "   Backend:  http://localhost:8080"
	@echo "   Frontend: http://localhost:3000"
	@echo "   Database: localhost:5432"

docker-up-build: ## Start all services and rebuild images
	@echo "🐳 Building and starting Docker containers..."
	@docker-compose up -d --build

docker-down: ## Stop all Docker containers
	@echo "🐳 Stopping Docker containers..."
	@docker-compose down

docker-down-volumes: ## Stop containers and remove volumes
	@echo "🐳 Stopping containers and removing volumes..."
	@docker-compose down -v

docker-logs: ## View Docker logs
	@docker-compose logs -f

docker-logs-backend: ## View backend logs
	@docker-compose logs -f backend

docker-logs-frontend: ## View frontend logs
	@docker-compose logs -f frontend

docker-ps: ## List running containers
	@docker-compose ps

docker-build: ## Build Docker images
	@echo "🐳 Building Docker images..."
	@docker-compose build

docker-build-backend: ## Build backend Docker image
	@echo "🐳 Building backend image..."
	@docker build -t procureflow-backend -f backend/Dockerfile backend/

docker-build-frontend: ## Build frontend Docker image
	@echo "🐳 Building frontend image..."
	@docker build -t procureflow-frontend -f frontend/Dockerfile frontend/

docker-clean: ## Remove Docker containers, images, and volumes
	@echo "🐳 Cleaning Docker resources..."
	@docker-compose down -v --rmi all
	@echo "✅ Docker cleanup complete"

##@ Database

db-start: ## Start PostgreSQL with Docker
	@echo "🐘 Starting PostgreSQL..."
	@docker run --name procureflow-postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=procureflow -p 5432:5432 -d postgres:16-alpine
	@echo "✅ PostgreSQL started on port 5432"

db-stop: ## Stop PostgreSQL container
	@echo "🐘 Stopping PostgreSQL..."
	@docker stop procureflow-postgres
	@docker rm procureflow-postgres

db-psql: ## Connect to PostgreSQL with psql
	@docker exec -it procureflow-postgres psql -U postgres -d procureflow

db-migrate: ## Run database migrations
	@echo "🗄️  Running migrations..."
	@cd backend && ./gradlew.bat flywayMigrate

db-clean: ## Clean database (run with caution!)
	@echo "⚠️  Cleaning database..."
	@cd backend && ./gradlew.bat flywayClean

##@ Development

dev: ## Start development environment (Docker)
	@echo "🚀 Starting development environment..."
	@docker-compose up postgres backend -d
	@timeout /t 10 /nobreak > nul
	@start cmd /k "cd frontend && npm run dev"
	@echo "✅ Development environment ready!"
	@echo "   Backend:  http://localhost:8080"
	@echo "   Frontend: http://localhost:5173"
	@echo "   Database: localhost:5432"

dev-logs: ## Follow development logs
	@docker-compose logs -f postgres backend

lint: ## Run linters
	@echo "🔍 Running linters..."
	@cd frontend && npm run lint

format: ## Format code
	@echo "✨ Formatting code..."
	@cd backend && ./gradlew.bat ktlintFormat || echo "ktlint not configured"
	@cd frontend && npx prettier --write "src/**/*.{ts,tsx,js,jsx,json,css}"

##@ Cleaning

clean: clean-backend clean-frontend ## Clean all build artifacts
	@echo "✅ Cleanup complete"

clean-backend: ## Clean backend build artifacts
	@echo "🧹 Cleaning backend..."
	@cd backend && ./gradlew.bat clean

clean-frontend: ## Clean frontend build artifacts
	@echo "🧹 Cleaning frontend..."
	@cd frontend && if exist node_modules rmdir /s /q node_modules
	@cd frontend && if exist dist rmdir /s /q dist
	@cd frontend && if exist build rmdir /s /q build

clean-all: clean docker-clean ## Clean everything including Docker
	@echo "🧹 Deep clean complete"

##@ Health & Status

health: ## Check application health
	@echo "🏥 Checking application health..."
	@curl -s http://localhost:8080/actuator/health || echo "Backend not running"
	@curl -s http://localhost:5173 || echo "Frontend not running"

status: ## Show services status
	@echo "📊 Services Status:"
	@echo "=================="
	@docker-compose ps 2>nul || echo "Docker not running"
	@echo ""
	@netstat -an | findstr "8080 5173 5432" || echo "No services listening on expected ports"

##@ Documentation

docs: ## Open documentation in browser
	@echo "📚 Opening documentation..."
	@start README.md
	@start TESTING.md
	@start E2E_TESTING.md

api-docs: ## Open API documentation (Swagger/OpenAPI)
	@echo "📖 Opening API docs..."
	@start http://localhost:8080/swagger-ui.html || echo "Backend must be running"

##@ Utilities

version: ## Show versions of all tools
	@echo "🔧 Tool Versions:"
	@echo "================"
	@java -version 2>&1 | findstr "version"
	@node --version
	@npm --version
	@cd backend && ./gradlew.bat --version | findstr "Gradle"
	@docker --version
	@docker-compose --version

check-env: ## Check if all required tools are installed
	@echo "✅ Checking environment..."
	@where java >nul 2>&1 && echo "[OK] Java installed" || echo "[MISSING] Java not found"
	@where node >nul 2>&1 && echo "[OK] Node.js installed" || echo "[MISSING] Node.js not found"
	@where npm >nul 2>&1 && echo "[OK] npm installed" || echo "[MISSING] npm not found"
	@where docker >nul 2>&1 && echo "[OK] Docker installed" || echo "[MISSING] Docker not found"
	@where docker-compose >nul 2>&1 && echo "[OK] docker-compose installed" || echo "[MISSING] docker-compose not found"

ports: ## Check if required ports are available
	@echo "🔍 Checking ports..."
	@netstat -an | findstr ":8080" && echo "⚠️  Port 8080 (backend) is in use" || echo "✅ Port 8080 available"
	@netstat -an | findstr ":5173" && echo "⚠️  Port 5173 (frontend) is in use" || echo "✅ Port 5173 available"
	@netstat -an | findstr ":5432" && echo "⚠️  Port 5432 (postgres) is in use" || echo "✅ Port 5432 available"

##@ Quick Start

quickstart: install docker-up ## Quick start: install deps and start with Docker
	@echo "🎉 ProcureFlow is ready!"
	@echo "   Frontend: http://localhost:3000"
	@echo "   Backend:  http://localhost:8080"
	@echo "   API Docs: http://localhost:8080/swagger-ui.html"

demo: docker-up ## Start demo environment
	@echo "🎬 Demo environment starting..."
	@timeout /t 15 /nobreak > nul
	@start http://localhost:3000
	@echo "✅ Demo ready at http://localhost:3000"
