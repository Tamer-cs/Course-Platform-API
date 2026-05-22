# AI Context & Coding Standards: Course Platform API

You are an expert Senior Java/Spring Boot developer. You are guiding a developer to build a clean, production-ready REST API for a learning platform. Follow these strict architectural guidelines, technology constraints, and business logic requirements at all times.

The goal is not just to satisfy requirements. It is to exceed them and impress a technical reviewer. Every section marked bonus represents optional work that will be evaluated favorably. Implement all of them.

---

## 1. Core Tech Stack (Strict Constraints)

- Java version: 17+
- Framework: Spring Boot 3.x. Use modern Spring Security 6.x configurations. Do not use deprecated classes like `WebSecurityConfigurerAdapter`.
- Database: PostgreSQL
- ORM: Spring Data JPA / Hibernate
- Authentication: Spring Security with stateless JWT
- Documentation: Springdoc-OpenAPI via `springdoc-openapi-starter-webmvc-ui`
- Boilerplate reduction: Lombok throughout (`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`). Do not mix Lombok with Java records on the same class.

---

## 2. Architecture & Design Patterns

### Layered architecture - strict flow

```text
Request -> Controller -> Service -> Repository -> Database
```

### DTO Pattern (mandatory)

- Never return `@Entity` classes directly from `@RestController` endpoints.
- Bi-directional JPA relationships (Course ↔ Topic ↔ Subtopic) can cause infinite JSON recursion and serialization problems.
- Always map entities into dedicated response DTOs.
- Use the following on all DTO classes:

```java
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
```

### Global Error Handling

- Implement a single centralized `@RestControllerAdvice`.
- Every error response must follow this exact structure:

```json
{
  "error": "Error Type",
  "message": "Human-readable description",
  "timestamp": "2026-05-22T16:30:00Z"
}
```

### Required exception mappings

| Exception | HTTP Status |
| --- | --- |
| AlreadyEnrolledException | 409 CONFLICT |
| NotEnrolledException | 403 FORBIDDEN |
| ResourceNotFoundException | 404 NOT_FOUND |
| MethodArgumentNotValidException | 400 BAD_REQUEST |
| AccessDeniedException | 403 FORBIDDEN |
| BadCredentialsException | 401 UNAUTHORIZED |

## 3. Project Structure

```text
src/
├── main/
│   ├── java/com/yourapp/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/
│   │   ├── entity/
│   │   ├── dto/
│   │   ├── mapper/
│   │   ├── security/
│   │   ├── config/
│   │   ├── search/
│   │   ├── exception/
│   │   └── util/
│   └── resources/
│       ├── application.properties
│       └── courses.json
```

## 4. Configuration & Environment

Use environment variables for all secrets and infrastructure configuration.

### application.properties

```properties
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false

jwt.secret=${JWT_SECRET}
jwt.expiration=86400000

springdoc.swagger-ui.path=/swagger-ui.html
springdoc.api-docs.path=/v3/api-docs

embedding.model=sentence-transformers/all-MiniLM-L6-v2
embedding.dimension=384
```

Never hardcode:

- Database credentials
- JWT secret
- Deployment URLs

JWT_SECRET must be at least 32 random characters.

## 5. Database Seed Logic

Implement a `DataSeeder` using `ApplicationRunner`.

### Startup flow

1. Check `courseRepository.count()`.
2. If the database is empty:
   - load `courses.json`
   - parse using Jackson `ObjectMapper`
   - save courses, topics, and subtopics
   - generate embeddings
   - enable PostgreSQL extensions
3. If the database is already populated:
   - do nothing

## 6. Seed Data Quality Requirements (VERY IMPORTANT)

The reviewer will heavily evaluate the search system.

Weak seed data will make the project appear broken even if the code is correct.

### Minimum dataset requirements

#### Courses

- At least 2 complete courses

#### Topics

- Each course must contain a minimum of 3 topics

#### Subtopics

- Each topic must contain a minimum of 3 subtopics

#### Markdown Content

- Each subtopic must contain 150-300 words
- Realistic educational explanations
- Searchable terminology
- Multiple paragraphs
- Markdown formatting
- Formulas where relevant
- Synonyms and related concepts

### REQUIRED search terms

The following terms must appear naturally throughout the content because reviewers will likely search them:

- Physics
- velocity
- acceleration
- Newton
- inertia
- force
- momentum
- energy
- displacement
- gravity
- motion
- Mathematics
- derivative
- rate of change
- function
- slope
- algebra
- equation
- variable
- graph
- limit
- calculus

### Example good markdown content

```md
# Velocity

Velocity describes the **rate of change of displacement** with respect to time.

Unlike speed, velocity includes both:
- magnitude
- direction

The formula is:

v = d / t

Where:
- v = velocity
- d = displacement
- t = time

Velocity is fundamental in Newtonian mechanics and appears in topics such as:
- acceleration
- momentum
- kinetic energy
```

## 7. courses.json Structure (MANDATORY)

The repository must contain a rich `courses.json`.

### Structure example

```json
[
  {
    "id": "physics-101",
    "title": "Introduction to Physics",
    "description": "Fundamental concepts of motion, forces, and energy.",
    "topics": [
      {
        "id": "kinematics",
        "title": "Kinematics",
        "subtopics": [
          {
            "id": "velocity",
            "title": "Velocity",
            "content": "# Velocity\n\nVelocity is the rate of change..."
          }
        ]
      }
    ]
  }
]
```

### Content authoring rules

Subtopic content should:

- read like mini educational articles
- contain terminology repetition naturally
- include synonyms
- include conceptual phrasing

Example phrases:

- velocity
- how fast an object moves
- rate of motion
- change in displacement over time

This directly improves:

- keyword search
- fuzzy search
- semantic search embeddings

## 8. Security & JWT

Use stateless JWT authentication.

### JwtService responsibilities

- generate token
- extract username
- validate token
- parse expiration

### Use

- `io.jsonwebtoken`
- HS256 signing
- `JwtAuthFilter`

### Filter steps

1. Read the Authorization header.
2. Extract the Bearer token.
3. Validate the token.
4. Set `SecurityContextHolder`.

If invalid:

- do not crash the request
- allow Spring Security to return 401

### Public endpoints

- `GET /api/courses/**`
- `GET /api/search/**`
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /swagger-ui/**`
- `GET /swagger-ui.html`
- `GET /v3/api-docs/**`

All enrollment and progress endpoints require JWT authentication.

## 9. Search System Overview

The search system is one of the most important evaluation areas.

Implement all three layers:

| Layer | Requirement |
| --- | --- |
| Keyword Search | Mandatory |
| Fuzzy Search | Bonus |
| Semantic Embedding Search | Advanced Bonus |

The final implementation should feel intelligent and professional.

## 10. Keyword Search (Mandatory Baseline)

Implement weighted PostgreSQL ILIKE search.

### Requirements

- case-insensitive
- partial matching
- ranking/scoring
- searchable markdown content

### Weighted relevance rules

Prioritize matches in this order:

| Match Location | Priority |
| --- | --- |
| Course title | Highest |
| Topic title | High |
| Subtopic title | Medium |
| Content body | Lower |

Use SQL CASE scoring.

### Native query example

```sql
SELECT DISTINCT
  c.id AS course_id,
  c.title AS course_title,
  t.title AS topic_title,
  s.id AS subtopic_id,
  s.title AS subtopic_title,
  s.content AS subtopic_content,
  CASE
    WHEN LOWER(c.title) LIKE LOWER(CONCAT('%', :q, '%')) THEN 1
    WHEN LOWER(t.title) LIKE LOWER(CONCAT('%', :q, '%')) THEN 2
    WHEN LOWER(s.title) LIKE LOWER(CONCAT('%', :q, '%')) THEN 3
    WHEN LOWER(s.content) LIKE LOWER(CONCAT('%', :q, '%')) THEN 4
    ELSE 5
  END AS relevance_score
FROM courses c
JOIN topics t ON t.course_id = c.id
JOIN subtopics s ON s.topic_id = t.id
WHERE
  LOWER(c.title) LIKE LOWER(CONCAT('%', :q, '%'))
  OR LOWER(t.title) LIKE LOWER(CONCAT('%', :q, '%'))
  OR LOWER(s.title) LIKE LOWER(CONCAT('%', :q, '%'))
  OR LOWER(s.content) LIKE LOWER(CONCAT('%', :q, '%'))
ORDER BY relevance_score ASC
```

## 11. Snippet Extraction (Important Bonus Detail)

Do not return full markdown content inside search results.

Instead:

- extract only the relevant matching region
- highlight where the query matched
- return approximately 120-180 characters around the match

This dramatically improves reviewer perception.

### Snippet extraction logic

Implement helper:

```java
private String extractSnippet(String content, String query)
```

### Algorithm

1. Convert both strings to lowercase.
2. Find the first match index.
3. Extract surrounding context.
4. Add `...` before and after when trimmed.

### Example output

```json
{
  "snippet": "...Velocity is the rate of change of displacement with respect to time..."
}
```

## 12. Fuzzy Search (Bonus)

Implement typo-tolerant searching.

Examples:

- `physcs` -> `physics`
- `velocoty` -> `velocity`
- `accelaration` -> `acceleration`

### PostgreSQL pg_trgm

Enable the extension automatically.

```sql
CREATE EXTENSION IF NOT EXISTS pg_trgm;
```

- Do this at startup inside `DataSeeder`.
- Wrap the `CREATE EXTENSION` execution inside a raw JDBC try-catch block.
- Catch any initialization exceptions safely and log them as warnings so database permission restrictions on cloud platforms do not prevent the main application from booting.

### Fuzzy query

Use trigram similarity:

```sql
SELECT DISTINCT c.id, c.title, t.title, s.id, s.title, s.content,
  GREATEST(
    similarity(LOWER(c.title), LOWER(:q)),
    similarity(LOWER(s.title), LOWER(:q)),
    similarity(LOWER(s.content), LOWER(:q))
  ) AS similarity_score
FROM courses c
JOIN topics t ON t.course_id = c.id
JOIN subtopics s ON s.topic_id = t.id
WHERE
  similarity(LOWER(c.title), LOWER(:q)) > 0.15
  OR similarity(LOWER(s.title), LOWER(:q)) > 0.15
  OR similarity(LOWER(s.content), LOWER(:q)) > 0.15
ORDER BY similarity_score DESC
LIMIT 20
```

### Search flow

Inside `SearchService`:

1. Run keyword search.
2. If results are empty, run fuzzy search.
3. Return `fuzzy=true` if fallback was used.

### Example fuzzy response

```json
{
  "query": "physcs",
  "fuzzy": true,
  "results": [
    {
      "courseId": "physics-101",
      "courseTitle": "Introduction to Physics"
    }
  ]
}
```

## 13. Semantic Search Using Embeddings (Advanced Bonus)

This is the feature most likely to impress reviewers.

### Goal

- retrieve conceptually related content
- not only exact keywords

### Example

Searching `how fast something moves` should return:

- velocity
- motion
- displacement

even if the exact word `velocity` was never typed.

## 14. Embedding Architecture

Use:

- local sentence embedding model
- no external APIs
- no paid services

### Recommended

- DJL (Deep Java Library)
- `sentence-transformers/all-MiniLM-L6-v2`

### pom.xml dependency

```xml
<dependency>
  <groupId>ai.djl.huggingface</groupId>
  <artifactId>tokenizers</artifactId>
  <version>0.26.0</version>
</dependency>
```

- Force DJL to use the lightweight ONNX Runtime engine instead of PyTorch to prevent Out-Of-Memory (OOM) crashes on 512MB RAM deployment instances.
- Set the system property programmatically on startup: `System.setProperty("ai.djl.repository.zoo.default_engine", "OnnxRuntime");`

## 15. Embedding Storage Design

Create a dedicated entity:

- `SubtopicEmbedding`

### Fields

- id
- subtopicId
- embeddingVector
- createdAt

### PostgreSQL vector storage

- To bypass complex JPA array mapping exceptions, store the 384-dimension vector in the database table as a serialized JSON string or a standard binary blob (`byte[]`).
- Convert between `float[]` and the persistent column state using a simple Jackson attribute converter.

## 16. Embedding Generation Flow

After seed data insertion:

```text
for each subtopic:
    generate embedding
    persist vector
```

Generate embeddings for:

- title
- content

combined together.

Recommended text input:

```text
Velocity Velocity is the rate of change of displacement...
```

This improves semantic retrieval quality.

## 17. Cosine Similarity

Implement utility:

```java
public static double compute(float[] a, float[] b)
```

### Formula

```text
dot(a,b) / (||a|| * ||b||)
```

Use:

- cosine similarity threshold >= 0.35
- return top-N matches

## 18. Search Modes API

Add:

- `GET /api/search?q=velocity&mode=keyword`
- `GET /api/search?q=physcs&mode=fuzzy`
- `GET /api/search?q=how fast objects move&mode=semantic`
- `GET /api/search?q=motion&mode=hybrid`

### Search modes

| Mode | Behavior |
| --- | --- |
| keyword | weighted ILIKE |
| fuzzy | trigram similarity |
| semantic | embeddings cosine similarity |
| hybrid | combine all modes |

## 19. Hybrid Search (Highly Recommended)

Hybrid mode should:

- merge keyword + semantic results
- deduplicate by `subtopicId`
- combine scores
- prioritize strongest matches

- Hybrid search must normalize all tracking scores to a standard 0.0 to 1.0 scale, where 1.0 is a perfect match, before deduplication.
- Formula rule for Hybrid Combining: `FinalScore = (KeywordMatch ? 0.5 : 0.0) + (SemanticScore * 0.5)`. This ensures contextual relevance gracefully elevates exact matches.

This gives the most impressive demo behavior.

## 20. Search Result DTO

Recommended structure:

```json
{
  "query": "velocity",
  "mode": "semantic",
  "results": [
    {
      "courseId": "physics-101",
      "courseTitle": "Introduction to Physics",
      "topicTitle": "Kinematics",
      "subtopicId": "velocity",
      "subtopicTitle": "Velocity",
      "snippet": "...rate of change of displacement...",
      "score": 0.87,
      "fuzzy": false
    }
  ]
}
```

## 21. Business Logic Constraints

### Enrollment

Requirements:

- authenticated user only
- prevent duplicates
- return 409 if already enrolled

### Progress completion

Requirements:

- authenticated user only
- enrollment required
- idempotent operation
- no duplicate progress records

### Completion percentage

Formula:

```text
(completedSubtopics / totalSubtopics) * 100
```

Round to 2 decimal places.

## 22. Swagger / OpenAPI

Swagger must fully demonstrate the application.

Reviewer should be able to:

- Register
- Login
- Copy JWT
- Authorize
- Search
- Enroll
- Complete subtopics
- View progress

without leaving Swagger UI.

### OpenAPI security scheme

Configure Bearer JWT globally.

Swagger "Authorize" button must work properly.

## 23. Validation

Use Bean Validation everywhere.

Examples:

- `@NotBlank`
- `@Email`
- `@Size`

Always use `@Valid` on request DTOs.

## 24. Deployment (Railway)

Deployment is mandatory.

### Requirements

- public URL
- working PostgreSQL
- Swagger enabled
- seed data auto-loaded

### Required Railway environment variables

- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `JWT_SECRET`

## 25. README Quality (Evaluation Criterion)

The README is not optional polish.

It is part of the evaluation.

A strong README can significantly improve reviewer perception.

## 26. README Requirements

The README must contain:

### 1. Overview

Short explanation of the platform.

### 2. Features

Include:

- JWT authentication
- enrollment system
- progress tracking
- keyword search
- fuzzy search
- semantic search
- Swagger/OpenAPI

### 3. Tech stack

Example:

- Java 17
- Spring Boot 3
- PostgreSQL
- Spring Security
- JWT
- JPA/Hibernate
- DJL embeddings
- Railway deployment

### 4. Architecture

Brief explanation:

- layered architecture
- DTO pattern
- service separation
- centralized exception handling

### 5. Running locally

Include:

- prerequisites
- PostgreSQL setup
- environment variables
- startup commands

Example:

```bash
mvn spring-boot:run
```

### 6. Search capabilities

Explain all modes with examples.

Keyword Search:

- velocity
- Newton
- rate of change

Fuzzy Search:

- physcs
- velocoty
- accelaration

Semantic Search:

- how fast objects move
- object resisting motion
- energy caused by movement

### 7. API summary

Table of endpoints:

- method
- path
- auth required
- purpose

### 8. Deployment link

Include:

- Railway URL
- Swagger URL

### 9. Design decisions

Explain:

- why PostgreSQL trigram search
- why embeddings
- why weighted ranking
- why DTO architecture
- why stateless JWT

## 27. Final Reviewer Impression Checklist

Before submission ensure:

- Swagger works publicly
- reviewer can test without Postman
- search feels intelligent
- fuzzy matching clearly works
- semantic search clearly works
- snippets look professional
- README is polished
- seed data is rich
- no stack traces exposed
- all endpoints return proper JSON
- deployment is stable
- JWT authorization works from Swagger

The goal is not merely correctness.

The goal is to look like an engineer who already thinks beyond internship level.