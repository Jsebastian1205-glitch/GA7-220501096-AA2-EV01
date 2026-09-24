#!/usr/bin/env sh
# Genera backend-api/target/buildzone-api-1.0.0.jar (ejecuta antes las pruebas)
cd "$(dirname "$0")/../backend-api" && mvn clean package
