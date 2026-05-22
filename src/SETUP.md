## Local Environment Setup Guide

This guide records the local development setup for the Course Platform API.

### 1. Prerequisites

- Java 17 installed locally.
- Eclipse Adoptium OpenJDK is recommended.
- PostgreSQL-compatible database access.
- Maven wrapper available in the project root.

### 2. Environment Variables

Create a `.env` file in the project root and do not commit it to source control.

```env
DATABASE_URL=jdbc:postgresql://<your-neon-cluster-url>:5432/neondb?sslmode=require
DATABASE_USERNAME=<your-username>
DATABASE_PASSWORD=<your-password>
JWT_SECRET=<your-32-character-secret>
```

The application is configured to load this file automatically through `spring.config.import=optional:file:.env[.properties]`.

### 3. Windows PowerShell JDK Fix

If Maven reports `No compiler is provided in this environment`, PowerShell is likely using a JRE instead of a JDK.

Run these commands in the active terminal session:

```powershell
$env:JAVA_HOME="C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
$env:Path="$env:JAVA_HOME\bin;$env:Path"
```

Verify that the directory name matches the JDK installation on your machine.

### 4. Run the Application

Use the Windows-native Maven wrapper script from the project root:

```powershell
.\mvnw.cmd clean spring-boot:run
```

### 5. Port and Firewall Notes

- The application runs on port `8081`.
- Windows Defender Firewall may prompt for network access on first run.
- Allow access so the local server can bind to the port.

### 6. Verify the App

Once the startup log shows `Started ApiApplication`, check these URLs:

- Swagger UI: http://localhost:8081/swagger-ui.html
- OpenAPI JSON: http://localhost:8081/v3/api-docs

### 7. Setup Notes

- `.env` keeps secrets out of the repository.
- The JDK 17 path fix was required because the default terminal session was pointing to Java 8.
- `application.properties` already imports local dotenv values, so the app can boot without manually exporting variables each time.
- After setup, continue with the next implementation phase: core entities and repositories.
