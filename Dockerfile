# syntax=docker/dockerfile:1

# --- Build stage: compile and package the fat jar ---
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build

# Cache dependencies first: copy the POM, warm the local repo, then the sources.
COPY pom.xml .
RUN mvn -q -B dependency:go-offline

COPY src ./src
RUN mvn -q -B clean package -DskipTests

# --- Runtime stage: minimal JRE, non-root, portable ---
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Run as an unprivileged user rather than root.
RUN groupadd --system app && useradd --system --gid app app

COPY --from=build /build/target/*.jar /app/app.jar
USER app

# Documented default; the app binds ${PORT:8080} on 0.0.0.0 so the same image
# runs locally (docker run -p 8080:8080) and on any PaAS that injects PORT.
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
