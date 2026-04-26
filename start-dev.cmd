@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "ROOT=%~dp0"
set "FRONTEND_PORT=5173"

set "PORT_BUSY="
for /f "tokens=5" %%P in ('netstat -ano ^| findstr /R /C:":%FRONTEND_PORT% .*LISTENING"') do (
    set "PORT_BUSY=1"
    echo Port %FRONTEND_PORT% is already used by PID %%P:
    tasklist /fi "PID eq %%P"
)

if defined PORT_BUSY (
    echo.
    choice /C YN /M "Stop the process using port %FRONTEND_PORT% and continue"
    if errorlevel 2 (
        echo Start canceled. Close the old frontend terminal, then run start-dev.cmd again.
        pause
        exit /b 1
    )

    for /f "tokens=5" %%P in ('netstat -ano ^| findstr /R /C:":%FRONTEND_PORT% .*LISTENING"') do (
        taskkill /F /PID %%P
    )
    echo.
)

echo Starting ATM auth server with the dev in-memory database...
start "ATM Auth Server" cmd /k "cd /d ""%ROOT%atm-server-auth"" && mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev"

echo Starting ATM frontend on http://localhost:%FRONTEND_PORT% ...
start "ATM Frontend" cmd /k "cd /d ""%ROOT%frontend"" && npm install && npm run dev:real"

echo.
echo Frontend: http://localhost:%FRONTEND_PORT%
echo Backend:  http://localhost:8080/api/atm
echo Demo card: 6222020000000001
echo Demo PIN:  123456
echo.
echo Two terminal windows were opened. Close them to stop the services.
