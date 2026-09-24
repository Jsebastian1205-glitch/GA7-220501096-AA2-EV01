# =====================================================================
# Imagen Docker de buildzone-api (usada para el despliegue en Render).
# Etapa 1: compila el JAR con Maven. Etapa 2: imagen liviana solo con JRE.
#   docker build -t buildzone-api .
#   docker run -p 8080:8080 buildzone-api
# =====================================================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# Las pruebas se ejecutan en GitHub Actions (ci.yml); aqui solo se empaqueta.
RUN mvn -B -q package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --no-create-home buildzone
COPY --from=build /app/target/buildzone-api-*.jar app.jar
USER buildzone
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
# MaxRAMPercentage evita que la JVM exceda la memoria del plan gratuito (512 MB).
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
