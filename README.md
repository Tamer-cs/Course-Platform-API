# Course Platform API

Course Platform API is a Spring Boot service for authenticated learning content, relational persistence, and intelligent search.

## Features

- JWT-based authentication with role-based access control.
- Request validation for auth, enrollment, progress, and search flows.
- Relational persistence for users, courses, topics, subtopics, enrollments, and progress.
- Swagger/OpenAPI documentation for quick API discovery and manual verification.
- Search across course metadata and semantic text embeddings.

## Search

- Traditional metadata filtering ranks matches across course, topic, and subtopic titles and content.
- Semantic Text Analysis uses the DJL, HuggingFace tokenizer, and ONNX Runtime pipeline to compare user queries against stored embeddings.
- Hybrid search combines keyword relevance with semantic similarity and deduplicates results by `subtopicId`.

## Quickstart

Build the application without running the local test lifecycle:

```bash
./mvnw clean package -DskipTests
```

Run the application:

```bash
./mvnw spring-boot:run
```

On Windows, use the wrapper command:

```powershell
.\mvnw.cmd clean package -DskipTests
.\mvnw.cmd spring-boot:run
```

## API Docs

- Swagger UI: `/swagger-ui/index.html`
- OpenAPI spec: `/v3/api-docs`

## Deployment Variables

Configure these environment variables in Railway or any equivalent cloud environment:

- `JDBC_DATABASE_URL` - full PostgreSQL JDBC URL for the managed database.
- `PGUSER` - PostgreSQL username.
- `PGPASSWORD` - PostgreSQL password.
- `JWT_SECRET` - signing secret for JWT generation and validation.
- `JWT_EXPIRATION` - token lifetime in milliseconds.

Optional local fallback variables are also supported:

- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`

## Railway Notes

- `system.properties` pins the runtime to Java 17.
- The application listens on `PORT` when Railway provides it.
- Hibernate runs with `ddl-auto=update` so schema changes are applied automatically at startup.

## Endpoint Highlights

- `POST /api/auth/register` - create a user and return a JWT.
- `POST /api/auth/login` - authenticate a user and return a JWT.
- `GET /api/search/hybrid?q={query}&limit={limit}` - blended keyword and semantic search.
- `POST /api/enrollments/join/{courseId}` - enroll the authenticated user in a course.

## Notes

- The application uses stateless JWT security.
- PostgreSQL is the production database.
- H2 is used only for tests.
