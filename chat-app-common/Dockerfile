FROM maven:3.8.5-openjdk-17 AS build

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean install

FROM alpine:latest
WORKDIR /app
COPY --from=build /root/.m2/repository/com/chatapp/chat-app-common /app/
VOLUME ["/root/.m2/repository"]