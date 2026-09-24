@echo off
REM Inicia la API contra MySQL/MariaDB de XAMPP (base "buildzone").
REM Requisito: XAMPP encendido y script database\mysql\01_usuarios_suscripciones.sql ejecutado.
cd /d "%~dp0..\backend-api"
call mvn spring-boot:run
