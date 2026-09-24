#!/usr/bin/env sh
# Inicia la API en el perfil dev (H2 en memoria) en http://localhost:8080
cd "$(dirname "$0")/../backend-api" && mvn spring-boot:run -Dspring-boot.run.profiles=dev
