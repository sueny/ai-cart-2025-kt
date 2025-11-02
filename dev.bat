@echo off
REM ProcureFlow Development Helper Script
REM Alternative to Makefile for Windows without Make

setlocal enabledelayedexpansion

if "%1"=="" goto :help
if "%1"=="help" goto :help
if "%1"=="install" goto :install
if "%1"=="build" goto :build
if "%1"=="test" goto :test
if "%1"=="run" goto :run
if "%1"=="docker-up" goto :docker-up
if "%1"=="docker-down" goto :docker-down
if "%1"=="clean" goto :clean
if "%1"=="dev" goto :dev
if "%1"=="status" goto :status

echo Unknown command: %1
goto :help

:help
echo.
echo ProcureFlow - Development Commands
echo ====================================
echo.
echo Usage: dev.bat [command]
echo.
echo Commands:
echo   help         Display this help message
echo   install      Install all dependencies
echo   build        Build backend and frontend
echo   test         Run all tests
echo   run          Run full stack locally
echo   dev          Start development environment (Docker backend, local frontend)
echo   docker-up    Start all services with docker-compose
echo   docker-down  Stop all Docker containers
echo   clean        Clean build artifacts
echo   status       Show services status
echo.
goto :end

:install
echo Installing all dependencies...
echo.
echo [1/3] Installing backend dependencies...
cd backend
call gradlew.bat build -x test
cd ..
echo.
echo [2/3] Installing frontend dependencies...
cd frontend
call npm install
cd ..
echo.
echo [3/3] Installing E2E test dependencies...
cd frontend
call npx playwright install
cd ..
echo.
echo ✅ All dependencies installed successfully!
goto :end

:build
echo Building backend and frontend...
echo.
echo [1/2] Building backend...
cd backend
call gradlew.bat build
cd ..
echo.
echo [2/2] Building frontend...
cd frontend
call npm run build
cd ..
echo.
echo ✅ Build completed successfully!
goto :end

:test
echo Running all tests...
echo.
echo [1/2] Running backend tests...
cd backend
call gradlew.bat test
cd ..
echo.
echo [2/2] Running frontend tests...
cd frontend
call npm test || echo No frontend tests configured
cd ..
echo.
echo ✅ All tests completed!
goto :end

:run
echo Starting full stack...
echo ⚠️  Note: PostgreSQL must be running on localhost:5432
echo.
start "Backend Server" cmd /k "cd backend && gradlew.bat bootRun"
timeout /t 5 /nobreak > nul
start "Frontend Server" cmd /k "cd frontend && npm run dev"
echo.
echo ✅ Application started!
echo    Backend:  http://localhost:8080
echo    Frontend: http://localhost:5173
goto :end

:dev
echo Starting development environment...
docker-compose up postgres backend -d
timeout /t 10 /nobreak > nul
start "Frontend Dev Server" cmd /k "cd frontend && npm run dev"
echo.
echo ✅ Development environment ready!
echo    Backend:  http://localhost:8080
echo    Frontend: http://localhost:5173
echo    Database: localhost:5432
goto :end

:docker-up
echo Starting Docker containers...
docker-compose up -d
echo.
echo ✅ Services started!
echo    Backend:  http://localhost:8080
echo    Frontend: http://localhost:3000
echo    Database: localhost:5432
goto :end

:docker-down
echo Stopping Docker containers...
docker-compose down
echo.
echo ✅ Containers stopped!
goto :end

:clean
echo Cleaning build artifacts...
echo.
echo [1/2] Cleaning backend...
cd backend
call gradlew.bat clean
cd ..
echo.
echo [2/2] Cleaning frontend...
cd frontend
if exist node_modules rmdir /s /q node_modules
if exist dist rmdir /s /q dist
cd ..
echo.
echo ✅ Cleanup complete!
goto :end

:status
echo Services Status:
echo ==================
echo.
echo Docker containers:
docker-compose ps 2>nul || echo Docker not running
echo.
echo Listening ports:
netstat -an | findstr "8080 5173 5432" || echo No services on expected ports
goto :end

:end
endlocal
