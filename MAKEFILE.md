# Makefile Guide

## Overview

This project includes a comprehensive Makefile with common development tasks for building, testing, and running the ProcureFlow application.

## Prerequisites

### On Linux/Mac

Make is typically pre-installed. Verify with:
```bash
make --version
```

### On Windows

**Option 1: Install Make**

Install via Chocolatey:
```powershell
choco install make
```

Or via Scoop:
```powershell
scoop install make
```

**Option 2: Use dev.bat (No Make Required)**

```cmd
dev.bat help
dev.bat install
dev.bat run
```

## Quick Start

### View All Commands

```bash
make help
```

This displays all available commands with descriptions.

### First Time Setup

```bash
make quickstart
```

This will:
1. Install all dependencies (backend, frontend, E2E)
2. Start services with Docker
3. Open the application in your browser

## Common Commands

### Setup & Installation

```bash
make install              # Install all dependencies
make install-backend      # Install backend dependencies only
make install-frontend     # Install frontend dependencies only
make install-e2e          # Install E2E test dependencies (Playwright)
```

### Building

```bash
make build                # Build both backend and frontend
make build-backend        # Build backend only
make build-frontend       # Build frontend only
make build-backend-skip-tests  # Build backend without tests (faster)
```

### Testing

```bash
make test                 # Run all tests
make test-backend         # Run backend tests
make test-backend-unit    # Run only unit tests (faster)
make test-e2e             # Run E2E tests
make test-e2e-ui          # Run E2E tests with UI
make test-coverage        # Generate coverage report
```

### Running

```bash
make run                  # Run full stack locally (requires PostgreSQL)
make run-backend          # Run backend only
make run-frontend         # Run frontend only
make dev                  # Start development environment (Docker + local frontend)
```

### Docker

```bash
make docker-up            # Start all services
make docker-up-build      # Start and rebuild images
make docker-down          # Stop all containers
make docker-down-volumes  # Stop and remove volumes
make docker-logs          # View all logs
make docker-logs-backend  # View backend logs only
make docker-ps            # List running containers
make docker-build         # Build all images
make docker-clean         # Remove all containers, images, volumes
```

### Database

```bash
make db-start             # Start PostgreSQL with Docker
make db-stop              # Stop PostgreSQL
make db-psql              # Connect to database with psql
make db-migrate           # Run migrations
make db-clean             # Clean database (careful!)
```

### Development

```bash
make dev                  # Start dev environment
make dev-logs             # Follow dev logs
make lint                 # Run linters
make format               # Format code
```

### Cleaning

```bash
make clean                # Clean build artifacts
make clean-backend        # Clean backend only
make clean-frontend       # Clean frontend only
make clean-all            # Clean everything including Docker
```

### Health & Status

```bash
make health               # Check application health
make status               # Show services status
make ports                # Check port availability
```

### Utilities

```bash
make version              # Show versions of all tools
make check-env            # Check if required tools are installed
make docs                 # Open documentation
make api-docs             # Open API documentation
```

## Usage Examples

### Development Workflow

```bash
# First time setup
make install

# Start development
make dev

# Run tests while developing
make test-backend-unit

# View logs
make dev-logs
```

### Testing Workflow

```bash
# Run unit tests (fast)
make test-backend-unit

# Run all tests
make test

# Run E2E tests with UI
make test-e2e-ui

# Generate coverage
make test-coverage
```

### Production Build Workflow

```bash
# Clean everything
make clean-all

# Install fresh dependencies
make install

# Build for production
make build

# Start with Docker
make docker-up
```

### Docker Workflow

```bash
# Build and start
make docker-up-build

# View logs
make docker-logs

# Stop
make docker-down

# Clean up
make docker-clean
```

## Target Categories

### 📦 Setup & Installation
- Install dependencies for backend, frontend, and E2E tests

### 🔨 Building
- Build backend with Gradle
- Build frontend with npm

### 🧪 Testing
- Unit tests, integration tests, E2E tests
- Coverage reports

### 🚀 Running
- Run locally or with Docker
- Development and production modes

### 🐳 Docker
- Manage containers, images, volumes
- View logs and status

### 🗄️ Database
- Start/stop PostgreSQL
- Run migrations
- Connect with psql

### 🛠️ Development
- Linting, formatting
- Dev environment setup

### 🧹 Cleaning
- Clean build artifacts
- Remove Docker resources

### 🏥 Health & Status
- Check application health
- Port availability
- Service status

### 📚 Documentation
- Open docs and API documentation

## Environment Variables

The Makefile respects environment variables:

```bash
# Example: Override backend port
export SERVER_PORT=8081
make run-backend

# Example: Use different Docker Compose file
export COMPOSE_FILE=docker-compose.prod.yml
make docker-up
```

## Tips & Tricks

### Parallel Execution

Run multiple targets in parallel:

```bash
# Not recommended in Make, use separate terminals or:
make run-backend &
make run-frontend &
```

### Chain Commands

```bash
make clean build test    # Clean, then build, then test
```

### Check Before Running

```bash
make check-env ports    # Check environment and port availability
```

### Quick Demo

```bash
make demo               # Start everything and open browser
```

### Watch Mode

For continuous development:

```bash
# Terminal 1: Backend (auto-reload with devtools)
make run-backend

# Terminal 2: Frontend (Vite HMR)
make run-frontend

# Terminal 3: Watch tests
cd backend && ./gradlew.bat test --continuous
```

## Troubleshooting

### Make Command Not Found

**Windows**: Install via Chocolatey or use `dev.bat`

```cmd
dev.bat help
```

**Mac**: Install via Homebrew

```bash
brew install make
```

**Linux**: Install via package manager

```bash
sudo apt-get install make  # Debian/Ubuntu
sudo yum install make      # RHEL/CentOS
```

### Port Already in Use

```bash
make ports              # Check which ports are in use
```

Kill process on port:

```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <PID> /F

# Linux/Mac
lsof -ti:8080 | xargs kill
```

### Docker Issues

```bash
# Restart Docker
make docker-down
make docker-up

# Full cleanup and restart
make docker-clean
make docker-up-build
```

### PostgreSQL Connection Issues

```bash
# Check if PostgreSQL is running
make status

# Start PostgreSQL with Docker
make db-start

# Or start with docker-compose
make docker-up
```

### Build Failures

```bash
# Clean and rebuild
make clean
make build

# Or skip tests if they're failing
make build-backend-skip-tests
```

## Advanced Usage

### Override Targets

Create a `Makefile.local` for custom targets:

```makefile
include Makefile

# Custom target
my-task:
	@echo "Running custom task"
	@make build
	@make test
```

### CI/CD Integration

```yaml
# GitHub Actions example
- name: Run tests
  run: make test

- name: Build
  run: make build

- name: Deploy
  run: make docker-build
```

### Pre-commit Hooks

Add to `.git/hooks/pre-commit`:

```bash
#!/bin/bash
make lint
make test-backend-unit
```

## Performance Tips

1. **Skip tests during development build**:
   ```bash
   make build-backend-skip-tests
   ```

2. **Run only unit tests** (much faster):
   ```bash
   make test-backend-unit
   ```

3. **Use Docker for consistent environment**:
   ```bash
   make dev  # Faster than full local setup
   ```

4. **Parallel test execution**:
   ```bash
   cd backend && ./gradlew.bat test --parallel
   ```

## Windows-Specific Notes

### Using dev.bat (No Make Required)

All Makefile commands have equivalents in `dev.bat`:

```cmd
dev.bat help        → make help
dev.bat install     → make install
dev.bat build       → make build
dev.bat test        → make test
dev.bat run         → make run
dev.bat docker-up   → make docker-up
dev.bat docker-down → make docker-down
dev.bat clean       → make clean
dev.bat dev         → make dev
dev.bat status      → make status
```

### Path Separators

The Makefile uses forward slashes `/` which work on Windows with most tools.

### Terminal Colors

Some colors may not display correctly in cmd.exe. Use PowerShell or Windows Terminal for better output.

## Contributing

When adding new features:

1. Add corresponding Makefile targets
2. Update this documentation
3. Update `dev.bat` if applicable
4. Test on multiple platforms

## Resources

- [GNU Make Manual](https://www.gnu.org/software/make/manual/)
- [Makefile Tutorial](https://makefiletutorial.com/)
- [Project README](README.md)
- [Testing Guide](docs/BACKEND_TESTING.md)
- [E2E Testing](docs/E2E_TESTING.md)
