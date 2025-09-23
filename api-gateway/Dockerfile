FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy Maven configuration files
COPY pom.xml .
COPY src ./src

# Install Maven
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Build the application
RUN mvn clean package

# Expose port
EXPOSE 9080

# Run the application
CMD ["java", "-jar", "target/api-gateway-0.0.1-SNAPSHOT.jar"]