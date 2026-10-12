# Bites — Backend

Spring Boot 3 / Java 21 REST API for the Bites restaurant ordering app.

## Layout
- `config/` — Security, CORS, OpenAPI, Cloudinary beans
- `security/` — JWT provider + filter
- `controller/admin`, `controller/customer` — REST endpoints (thin, delegate to services)
- `service`, `service/impl` — business logic
- `repository/` — Spring Data JPA interfaces
- `entity/` — JPA entities
- `dto/request`, `dto/response` — request/response DTOs
- `mapper/` — MapStruct entity <-> DTO mappers
- `exception/` — domain exceptions + global handler
- `util/` — shared response wrapper (`ApiResponse`, `ApiError`)

## Run locally
1. Copy `.env.example` to `.env` and fill in values (or export as real env vars).
2. Create a local MySQL database named `bites`.
3. `./mvnw spring-boot:run` — Flyway will apply `V1__init.sql` on startup.
4. API docs at `http://localhost:8080/docs`.

## Status
Scaffolding stage: build config, security skeleton, exception handling, and
folder structure are in place. Entities, repositories, DTOs, and controllers
are implemented module by module next.
