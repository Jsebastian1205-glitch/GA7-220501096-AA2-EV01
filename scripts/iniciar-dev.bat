@echo off
REM Inicia la API en el perfil dev (H2 en memoria, sin XAMPP) en http://localhost:8080
cd /d "%~dp0..\backend-api"
call mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
