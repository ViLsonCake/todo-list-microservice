# Todo List project

The application is a hackneyed todo list idea implemented on a microservice architecture.

### Backend technology stack
+ Java 17
+ Gradle
+ Spring Boot 3.2.2
+ Eureka Server
+ Postgresql
+ MongoDB
+ Elasticsearch
+ Keycloak
+ Docker

### Application modules
+ Eureka server - microservices deployment server.
+ API Gateway - the entry point to the application.
+ Authorization server - responsible for retrieving and updating jwt tokens, and adding and/or modifying users to the keycloak repository.
+ Category service - microservice responsible for adding user's own categories for todo, MongoDB will be used for storage.
+ Todo service - microservice responsible for CRUD operations with todo, Elasticsearch will be used for storage.