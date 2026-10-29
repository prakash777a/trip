# TripCraft

TripCraft is a full-stack tourist trip planning and package booking application with a Spring Boot API, MySQL/Flyway schema, React/Vite frontend, JWT authentication, mock payments, admin workspace, and backend-only AI planning with a rule-based fallback.

## Run locally

1. Ensure MySQL 8 is running and create/select the `travel_app` schema (or let the JDBC URL create it).
2. Copy `.env.example` to `.env` and export the values in your shell (PowerShell example: `$env:DB_URL=...`). Never commit secrets.
3. Start the API: `cd backend && mvn spring-boot:run` (or use the bundled Maven executable under `.tools` on this machine).
4. Start the web app: `cd frontend && npm install && npm run dev`.
5. Open `http://localhost:5173`. Swagger is at `http://localhost:8080/swagger-ui.html`.

PowerShell local run example:

```powershell
$env:DB_URL = 'jdbc:mysql://localhost:3306/travel_app?serverTimezone=UTC'
$env:DB_USER = 'tripcraft'
$env:DB_PASSWORD = 'tripcraft_dev'
$env:JWT_SECRET = 'change-this-development-secret-to-at-least-32-characters'
cd backend; mvn spring-boot:run
```

Run the frontend in a second terminal:

```powershell
cd frontend
npm install
npm run dev
```

For isolated backend tests, create a separate MySQL schema and run with the test profile. The test profile never points at the shared application schema:

```text
CREATE DATABASE travel_app_test;
cd backend
mvn test -Dspring.profiles.active=test
```

Provide `TEST_DB_URL`, `TEST_DB_USER`, `TEST_DB_PASSWORD`, and `TEST_JWT_SECRET` in the test environment. `JWT_SECRET` is required for normal application startup and must contain at least 32 characters.

MySQL must be running locally on port `3306`. The backend creates the configured
database when the configured MySQL user has permission to do so, then Flyway
creates and seeds the tables on startup.

Flyway applies the schema and realistic seed data automatically. Default users are `admin@tripcraft.test` and `tourist@tripcraft.test`, both with password `password`.

## Project tree

```text
backend/   Spring Boot, security, API controllers, Flyway migrations
frontend/  React 18, Vite, Tailwind, responsive travel UI
docs/      ER diagram and endpoint contract
```

## Notes

The AI endpoints work without an LLM key using a deterministic database-backed fallback. If `LLM_API_KEY` and `LLM_MODEL` are configured, the backend parses natural-language requests and asks the LLM only for structured extraction/explanations; all inventory selection and price arithmetic remains in Java. The key is never sent to the browser. Payments are intentionally mock-only. Uploaded files are stored under `uploads/`, which is ignored by Git.
