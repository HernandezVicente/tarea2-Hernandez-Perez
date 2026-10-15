# Etapa 1: compilar dentro de Docker con Java 21
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /build

COPY pom.xml .
COPY src ./src

# Las pruebas se ejecutan en el job test del pipeline.
RUN mvn -B -DskipTests package


# Etapa 2: ejecutar únicamente con Java 21
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Crear un usuario sin privilegios de root
RUN addgroup -S appgroup \
    && adduser -S -G appgroup appuser \
    && mkdir -p /data \
    && chown appuser:appgroup /app /data

COPY --from=build --chown=appuser:appgroup \
    /build/target/*.jar /app/app.jar

ENV APP_PORT=3001 \
    APP_ENV=production \
    CSV_PATH=/data/compras.csv

USER appuser

EXPOSE 3001

ENTRYPOINT ["java", "-jar", "/app/app.jar"]