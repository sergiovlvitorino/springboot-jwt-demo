# ============================================
# Stage 1: Build
# ============================================
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy POM first for dependency caching
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn package -DskipTests -B

# ============================================
# Stage 2: Runtime
# ============================================
FROM eclipse-temurin:21-jre

# Security: run as non-root user
RUN groupadd --system appgroup && useradd --system --gid appgroup --no-create-home appuser

WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /app/target/springbootjwt-1.0.0.jar app.jar

# Set ownership
RUN chown -R appuser:appgroup /app

USER appuser

# Default environment
ENV SPRING_PROFILES_ACTIVE=prod
ENV PORT=8080

EXPOSE 8080

# JVM tuning for containers
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
