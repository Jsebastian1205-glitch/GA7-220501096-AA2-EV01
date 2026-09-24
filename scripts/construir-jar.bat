@echo off
REM Genera el ejecutable backend-api\target\buildzone-api-1.0.0.jar (ejecuta antes las pruebas).
cd /d "%~dp0..\backend-api"
call mvn clean package
if errorlevel 1 (
  echo.
  echo *** La construccion fallo. Revise los mensajes de arriba. ***
) else (
  echo.
  echo JAR generado en: %cd%\target\buildzone-api-1.0.0.jar
)
pause
