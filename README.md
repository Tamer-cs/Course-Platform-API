# Course Platform API

Course Platform API is a Spring Boot REST service for learning content, authentication, progress tracking, and search.

## Overview

The project is being built in staged sprints so the foundation stays stable while features are added.

## Implemented So Far

- Spring Boot 4 / Java 17 baseline with PostgreSQL, Spring Data JPA, Lombok, Validation, Spring Security, JWT, and Springdoc OpenAPI configured.
- Persistence layer for users, courses, topics, subtopics, enrollment, progress, and embeddings is in place.
- DTOs, mappers, and centralized exception handling are implemented so controllers can stay entity-free.
- Sprint 3 core text seeding is implemented with a transactional `DataSeeder` and a rich `courses.json` catalog.
- A fallback instructor user is created automatically for seeding, and seeded content is attached through parent-child JPA relationships.
- Sprint 4 course read endpoints are implemented with a public controller and read-only service layer.
- `GET /api/courses` returns lightweight course summaries.
- `GET /api/courses/{id}` returns a deeply nested course tree with topics and subtopics.
- Sprint 5 authentication and security are implemented with stateless JWT login and registration.
- `POST /api/auth/register` creates a user and returns a JWT.
- `POST /api/auth/login` authenticates a user and returns a JWT.
- JWT-protected requests must send `Authorization: Bearer <token>`.
- Passwords are hashed with `BCryptPasswordEncoder`.
- Swagger/OpenAPI is available through Springdoc 3.x and is compatible with Spring Boot 4.

## Security Model

- Public routes: `/api/auth/**`, `/api/courses/**`, `/api/search/**`, `/swagger-ui/**`, `/swagger-ui.html`, `/v3/api-docs`, and `/v3/api-docs/**`.
- All other routes require a valid JWT.
- Sessions are stateless and Spring Security form login / HTTP basic are disabled.
- API errors are returned through the shared `ErrorResponse` shape and the global exception handler.

## Authentication

- Registration uses `AuthRegisterRequest` and login uses `LoginRequest`.
- Successful authentication returns `AuthResponse` with a token.
- Email addresses are normalized to lowercase before persistence and authentication.
- Duplicate registration returns `409 Conflict`.
- Invalid credentials return `401 Unauthorized`.

## Configuration

Create a root `.env` file with the values used by the app:

- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION`

`JWT_SECRET` can be a raw secret string. `JWT_EXPIRATION` is the token lifetime in milliseconds.

## Seeding

On startup, the app checks whether courses already exist.

- If the database is empty, the seeder loads `src/main/resources/courses.json`, creates the fallback instructor owner, and inserts courses, topics, and subtopics.
- If courses already exist, the seeder logs a message and skips the import.
- The seed catalog currently includes 2 courses, 6 topics, and 18 subtopics.

## Local Setup

1. Install Java 17.
2. Create a root `.env` file with database and JWT values.
3. Use the Windows JDK 17 override if Maven reports a compiler error.
4. Run the app with:

```powershell
.\mvnw.cmd clean spring-boot:run
```

To verify the OpenAPI docs, open:

```text
http://localhost:8081/v3/api-docs
```

Swagger UI is available at:

```text
http://localhost:8081/swagger-ui.html
```

## Current Base Structure

- `controller`
- `service`
- `repository`
- `entity`
- `dto`
- `mapper`
- `security`
- `config`
- `search`
- `exception`
- `util`

## Current Status

- Sprint 0: done
- Sprint 1: done
- Sprint 2: done
- Sprint 3: done
- Sprint 4: done
- Sprint 5: done

## Next Step

Add auth-focused integration tests and Swagger bearer-token documentation.