@echo off
cd /d \"%~dp0\"
echo [DEV] Starting Spring Boot on port 8083 ...
echo Press Ctrl+C to stop.
echo.
mvn spring-boot:run
pause
