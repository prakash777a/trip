# Public deployment

Recommended hosting:

- Railway: backend and MySQL
- Vercel: frontend

## Railway backend variables

Create a Railway MySQL service and connect it to the backend service. Set these variables in the backend service. Use Railway's variable references or paste the generated MySQL values into the corresponding fields.

```text
DB_URL=jdbc:mysql://${MYSQLHOST}:${MYSQLPORT}/${MYSQLDATABASE}?serverTimezone=UTC
DB_USER=${MYSQLUSER}
DB_PASSWORD=${MYSQLPASSWORD}
JWT_SECRET=<long-random-production-secret>
CORS_ALLOWED_ORIGINS=https://<vercel-domain>
```

The backend runs Flyway migrations automatically. Railway supplies the `PORT` variable; Spring Boot already reads it from `application.properties`.

Set the Railway service root directory to `backend`. The included `backend/railway.toml` builds the Maven application and starts the generated JAR.

## Vercel frontend

Import the repository into Vercel and set the project root directory to `frontend`. Set:

```text
VITE_API_URL=https://<railway-backend-domain>/api
VITE_ALLOW_DEMO_FALLBACK=false
```

The included `frontend/vercel.json` keeps React Router routes working after refresh.

## Optional local database migration

The local MySQL database is not copied automatically. Create a backup before any migration:

```powershell
mysqldump -u root -p travel_app > travel_app_backup.sql
```

Import that file into the hosted MySQL service only after confirming the destination and backup. Do not commit the backup or any credentials to Git.
