# Stage 1: Build stage using Maven and Eclipse Temurin
FROM maven:3.9.8-eclipse-temurin-21-alpine AS builder
WORKDIR /build

# Copy parent pom
COPY pom.xml .
# Copy common-library
COPY services/common-library services/common-library
# Copy wallet-service code
COPY services/wallet-service services/wallet-service

# Build common shared library first
RUN mvn -f services/common-library/pom.xml clean install -DskipTests
# Build specific microservice wallet-service
RUN mvn -f services/wallet-service/pom.xml clean package -DskipTests

# Stage 2: Minimal runtime image using Alpine JRE
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy packaged JAR file from builder stage
COPY --from=builder /build/services/wallet-service/target/wallet-service-1.0.0.jar app.jar

# Create and run as a non-root system user for production security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
