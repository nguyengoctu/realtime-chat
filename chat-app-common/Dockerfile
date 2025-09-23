FROM maven:3.8.5-openjdk-17 AS build

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean install

# Final stage - just copy the built jar to maven repository location
FROM alpine:latest
RUN mkdir -p /root/.m2/repository
COPY --from=build /root/.m2/repository/ /root/.m2/repository/
VOLUME ["/root/.m2/repository"]