# Course Platform API

Course Platform API is a Spring Boot REST service for learning content, authentication, progress tracking, and search.

## Overview

The project is being built in staged sprints so the foundation stays stable while features are added.

## Implemented So Far

- Spring Boot 3 / Java 17 baseline with PostgreSQL, Spring Data JPA, Lombok, Validation, and Springdoc OpenAPI configured.
- Persistence layer for users, courses, topics, subtopics, enrollment, progress, and embeddings is in place.
- DTOs, mappers, and centralized exception handling are implemented so controllers can stay entity-free.
- Sprint 3 core text seeding is implemented with a transactional `DataSeeder` and a rich `courses.json` catalog.
- A fallback instructor user is created automatically for seeding, and seeded content is attached through parent-child JPA relationships.
- Sprint 4 course read endpoints are implemented with a public controller and read-only service layer.
- `GET /api/courses` returns lightweight course summaries.
- `GET /api/courses/{id}` returns a deeply nested course tree with topics and subtopics.

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

## Next Step

Build authentication and JWT security on top of the public course read API.