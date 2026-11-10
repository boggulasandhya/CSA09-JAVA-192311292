# ==============================================================================
# Multi-Stage Dockerfile for Campus Smart-Parking System
# Stage 1: Build & Compile Java Source
# Stage 2: Minimal Distroless / JRE Runtime
# ==============================================================================

# STAGE 1: Build Environment
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy source trees and tasks
COPY src ./src
COPY tasks ./tasks

# Create destination directory and compile
RUN mkdir -p bin && \
    javac -d bin $(find src tasks -name "*.java") && \
    jar cfe campus-parking.jar parking.system.main.ParkingWebServer -C bin .

# STAGE 2: Lightweight Runtime Image (<180MB)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as non-root user for cloud production security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copy compiled JAR from builder stage
COPY --from=builder /app/campus-parking.jar /app/campus-parking.jar

# Cloud standard PORT configuration
ENV PORT=8080
EXPOSE 8080

# Default entrypoint: Starts the web dashboard and REST API
# (Pass 'parking.system.main.ParkingDemo' as CMD to run console test suite instead)
ENTRYPOINT ["java", "-jar", "/app/campus-parking.jar"]
