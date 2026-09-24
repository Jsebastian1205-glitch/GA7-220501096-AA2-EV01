@echo off
REM Ejecuta las 59 pruebas unitarias y de integracion del backend.
cd /d "%~dp0..\backend-api"
call mvn test
pause
