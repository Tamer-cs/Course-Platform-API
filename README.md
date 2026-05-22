# Course Platform API

Course Platform API is a Spring Boot REST service for learning content, authentication, progress tracking, and search.

## Overview

The project is being built in staged sprints so the foundation stays stable while features are added.

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

## Next Step

Build the domain model for users, courses, topics, subtopics, enrollment, progress, and embeddings.