# Stage 1: Build the application
FROM maven:3.8.5-openjdk-17 AS build

WORKDIR /app

# Copy shared library from volume
COPY --from=chat-app-chat-app-common /app /root/.m2/repository/com/chatapp/chat-app-common

# Copy pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code and build
COPY src src
RUN mvn clean package

# Runtime stage
FROM openjdk:17-jdk-slim

WORKDIR /app

# Copy built jar from build stage
COPY --from=build /app/target/user-service-0.0.1-SNAPSHOT.jar app.jar

# Run the application
EXPOSE 8081
CMD ["java", "-jar", "app.jar"]