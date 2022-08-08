# SpringBoot-Kafka-Redis-Mongo

## Build

The application can be built and started from the command line by executing mvn clean install. Integration tests use TestContainers so docker is required to run them.

## How to run
```
docker compose up -d
mvnw spring-boot:run
```
Swagger-ui is available at http://localhost:8080/swagger-ui

Additionally, ColorCountingApplicationTests implements full integration tests


