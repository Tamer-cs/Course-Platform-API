# Course Platform API Plan

This document is the execution checklist for building the project in stable sprints.

## Guiding Rules

- Build in the same order every time so the project stays testable.
- Finish one layer before moving to the next.
- Keep every sprint shippable: code, run, verify, then continue.
- Do not mix UI output with entities; use DTOs end to end.
- Split content seeding from embedding generation so the app can be verified early.

## Sprint 0: Foundation

Goal: make the project compile and boot with the correct structure.

- [ ] Initialize the Spring Boot Maven project.
- [ ] Add dependencies for Spring Web, Spring Data JPA, Security, Validation, Lombok, Springdoc OpenAPI, Jackson, PostgreSQL, JWT, and DJL.
- [ ] Create the package structure from `ai-context.md`.
- [ ] Configure `application.properties` with environment variables only.
- [ ] Add the base README sections for setup and overview.

Validation:

- [ ] Application starts locally.
- [ ] Environment variables are read correctly.
- [ ] No missing dependency errors on boot.

## Sprint 1: Domain Model

Goal: define the persistence layer before writing business logic.

- [ ] Create entities for `User`, `Course`, `Topic`, `Subtopic`, `Enrollment`, `SubtopicProgress`, and `SubtopicEmbedding`.
- [ ] Define JPA relationships and constraints.
- [ ] Add repository interfaces for all entities.
- [ ] Confirm schema creation works with PostgreSQL.

Validation:

- [ ] Hibernate creates tables successfully.
- [ ] Repository beans load without errors.

## Sprint 2: DTOs, Mapping, and Errors

Goal: establish API contracts before controllers.

- [ ] Create request and response DTOs for auth, courses, search, enrollment, progress, and errors.
- [ ] Add mapper classes or dedicated conversion methods.
- [ ] Implement centralized `@RestControllerAdvice`.
- [ ] Add the required exception-to-status mappings.
- [ ] Add bean validation annotations to request DTOs.

Validation:

- [ ] Invalid requests return the correct JSON error shape.
- [ ] Controllers never return entities directly.

## Sprint 3: Core Text Seeding

Goal: load meaningful course data without semantic complexity yet.

- [ ] Add `courses.json` to `src/main/resources/`.
- [ ] Implement `DataSeeder` with `ApplicationRunner`.
- [ ] Load and parse `courses.json` with Jackson.
- [ ] Seed only when `courseRepository.count() == 0`.
- [ ] Save courses, topics, and subtopics only.
- [ ] Leave embedding generation as a later step.

Validation:

- [ ] App starts and seeds data on an empty database.
- [ ] App starts and skips seeding on a populated database.
- [ ] Seeded content can be queried from the database.

## Sprint 4: Course Read API

Goal: expose the course hierarchy through DTO-based endpoints.

- [ ] Implement public read endpoints for courses, topics, and subtopics.
- [ ] Return nested DTOs, not entities.
- [ ] Add snippet-safe response shapes where needed.

Validation:

- [ ] `GET /api/courses` returns seeded content.
- [ ] `GET /api/courses/{id}` returns the expected nested structure.
- [ ] JSON output is stable and recursion-free.

## Sprint 5: Authentication and Security

Goal: secure the platform with stateless JWT.

- [ ] Implement registration and login.
- [ ] Hash passwords correctly.
- [ ] Add `JwtService` for token generation, parsing, validation, and expiration.
- [ ] Add `JwtAuthFilter`.
- [ ] Configure the Spring Security 6 filter chain.
- [ ] Mark public and protected endpoints explicitly.

Validation:

- [ ] Login returns a valid JWT.
- [ ] Protected routes reject missing or invalid tokens.
- [ ] Swagger public endpoints remain accessible.

## Sprint 6: Enrollment and Progress

Goal: add authenticated learning actions.

- [ ] Implement enrollment with duplicate protection.
- [ ] Return 409 when a user is already enrolled.
- [ ] Implement idempotent subtopic completion.
- [ ] Require enrollment before completion.
- [ ] Compute completion percentage rounded to 2 decimals.

Validation:

- [ ] Duplicate enrollment is blocked.
- [ ] Repeating completion does not create duplicate records.
- [ ] Progress values are correct.

## Sprint 7: Keyword Search

Goal: ship the baseline search first.

- [ ] Implement weighted native SQL search.
- [ ] Score matches by course title, topic title, subtopic title, and content.
- [ ] Add snippet extraction for relevant excerpts.
- [ ] Return a search result DTO with score and fuzzy flag fields.

Validation:

- [ ] Search returns ranked results for obvious terms like velocity and Newton.
- [ ] Snippets are short and relevant.
- [ ] Full markdown is not returned in search results.

## Sprint 8: Fuzzy Search

Goal: make search typo tolerant.

- [ ] Enable `pg_trgm` safely at startup.
- [ ] Wrap `CREATE EXTENSION` in a raw JDBC try-catch.
- [ ] Implement trigram similarity search.
- [ ] Fallback to fuzzy search only when keyword search returns no results.

Validation:

- [ ] Misspellings like `physcs` still return useful results.
- [ ] Permission issues on extension creation do not crash startup.

## Sprint 9: Semantic Embeddings

Goal: add the advanced content understanding layer.

- [ ] Integrate DJL.
- [ ] Force ONNX Runtime on startup.
- [ ] Create the embedding service.
- [ ] Compute embeddings from title plus content.
- [ ] Store embeddings using the chosen JSON string or binary blob approach.
- [ ] Add cosine similarity utilities.
- [ ] Fill in the embedding generation step in `DataSeeder`.

Validation:

- [ ] Embeddings are generated after seeding.
- [ ] Semantic similarity returns conceptually related subtopics.
- [ ] The app boots without memory issues on constrained environments.

## Sprint 10: Hybrid Search and Swagger

Goal: combine search modes and make the API demo-ready.

- [ ] Implement hybrid search.
- [ ] Normalize keyword and semantic scores to a 0.0 to 1.0 scale.
- [ ] Deduplicate by `subtopicId`.
- [ ] Add OpenAPI annotations to all controllers.
- [ ] Configure global JWT bearer auth in Swagger.

Validation:

- [ ] Hybrid search merges results cleanly.
- [ ] Swagger Authorize works with JWT.
- [ ] The full public API can be tested from Swagger UI.

## Sprint 11: Deployment, and README

Goal: finish with verification and production readiness.

- [x] Prepare Railway deployment settings and environment variables.
- [x] Finalize the README with setup, features, search modes, API summary, and deployment details.

Validation:

- [x] Railway deployment starts successfully.
- [x] README matches the actual implementation.

## Done Criteria

The project is complete only when:

- [ ] The app boots cleanly.
- [ ] Seeding works.
- [ ] Auth works.
- [ ] Enrollment and progress work.
- [ ] Search feels intelligent.
- [ ] Swagger works publicly.
- [ ] Deployment is stable.
