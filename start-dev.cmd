@echo off
setlocal

set "ROOT=%~dp0"

echo Starting ATM auth server with the dev in-memory database...
start "ATM Auth Server" cmd /k "cd /d ""%ROOT%atm-server-auth"" && mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=dev"

echo Starting ATM frontend on http://localhost:5173 ...
start "ATM Frontend" cmd /k "cd /d ""%ROOT%frontend"" && npm install && npm run dev:real"

echo.
echo Frontend: http://localhost:5173
echo Backend:  http://localhost:8080/api/atm
echo Demo card: 6222020000000001
echo Demo PIN:  123456
echo.
echo Two terminal windows were opened. Close them to stop the services.
