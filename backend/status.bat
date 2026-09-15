@echo off
cd /d \"%~dp0\"
echo ===== Service Status (port 8083) =====
netstat -aon | findstr \":8083 \" | findstr \"LISTENING\" >nul 2>&1
if %errorlevel% equ 0 (
    echo   Status : RUNNING
    for /f \"tokens=5\" %%a in ('netstat -aon ^| findstr \":8083 \" ^| findstr \"LISTENING\"') do echo   PID    : %%a
    curl -s -o nul -w \"  HTTP   : %{http_code}\" http://localhost:8083/api/mini/assessment/questions 2>nul
    echo.
) else (
    echo   Status : STOPPED
)
echo ==================================
pause
