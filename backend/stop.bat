@echo off
cd /d "%~dp0"
echo Stopping service on port 8083 ...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":8083 " ^| findstr "LISTENING"') do (
    taskkill /F /PID %%a >nul 2>&1
    echo Killed PID: %%a
)
echo Done.
pause