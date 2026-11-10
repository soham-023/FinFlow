# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
# Run as an unprivileged user so a compromised app cannot modify the image or bind privileged ports
RUN addgroup -S finflow && adduser -S -G finflow finflow
COPY --from=build --chown=finflow:finflow /app/target/*.jar app.jar
USER finflow
EXPOSE 8080
# Mark the container unhealthy if the app stops answering its health check (wget ships with Alpine/BusyBox)
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]
