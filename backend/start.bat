@echo off
echo ==================================================
echo   START - AI Assessment Backend (port 8083)
echo ==================================================
echo.

set JAVA17=C:\Program Files\Java\jdk-17.0.19\bin\java.exe

echo [1/3] Stopping old process on port 8083 ...
for /f "tokens=5" %%a in ('netstat -aon ^| findstr ":8083 " ^| findstr "LISTENING" 2^>nul') do (
    taskkill /F /PID %%a >nul 2>&1
    echo Killed PID: %%a
)
echo.

echo [2/3] Starting JAR in background ...
for /f "delims=" %%i in ('dir /b target\*.jar 2^>nul') do set JAR_FILE=%%i
if "%JAR_FILE%"=="" (
    echo ERROR: No JAR found in target folder!
    pause
    exit /b 1
)
echo Using JAR: %JAR_FILE%
echo Using Java: %JAVA17%
echo.

start /min "%JAVA17%" -jar "target\%JAR_FILE%"

echo [3/3] Waiting 20s for startup...
timeout /t 20 /nobreak >nul
echo.

netstat -aon | findstr ":8083 " | findstr "LISTENING" >nul 2>&1
if %errorlevel% equ 0 (
    echo [SUCCESS] Backend is running on port 8083!
    echo Access: http://localhost:8083
) else (
    echo [FAILED] Backend did not start.
    echo.
    echo Run this to see errors:
    echo "%JAVA17%" -jar "target\%JAR_FILE%"
)
pause