@echo off
REM Ejecuta el JAR ya construido. Uso: ejecutar-jar.bat [perfil]   (por defecto: dev)
set PERFIL=%1
if "%PERFIL%"=="" set PERFIL=dev
cd /d "%~dp0..\backend-api\target"
if not exist buildzone-api-1.0.0.jar (
  echo No existe el JAR. Ejecute primero scripts\construir-jar.bat
  pause
  exit /b 1
)
java -jar buildzone-api-1.0.0.jar --spring.profiles.active=%PERFIL%
