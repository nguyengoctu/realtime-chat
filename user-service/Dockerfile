# Stage 1: Build the application
FROM maven:3.8.5-openjdk-17 AS build

# Copy shared dependencies from common lib
COPY --from=chat-app-chat-app-common /root/.m2/repository /root/.m2/repository

WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src src

# Build the application
RUN mvn clean package -Dmaven.test.skip=true

# Runtime stage
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy built jar from build stage
COPY --from=build /app/target/user-service-0.0.1-SNAPSHOT.jar app.jar

# Run the application
EXPOSE 8081
CMD ["java", "-jar", "app.jar"]