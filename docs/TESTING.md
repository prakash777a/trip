# Verification checklist

## Automated checks

```powershell
cd backend
mvn test
mvn package

cd ../frontend
npm install
npm run build
```

## Manual acceptance flow

1. Configure `DB_URL`, `DB_USER`, and `DB_PASSWORD` for MySQL 8 and start the API. Confirm Flyway applies `V1__create_schema.sql` and `V2__seed_data.sql`.
2. Open Swagger and verify public destination/package endpoints, then log in with `tourist@tripcraft.test / password`.
3. Create a draft, save trip details, select places, rooms, vehicles, and travelers, then request a price preview.
4. Confirm payment twice concurrently for the same scarce room/package date; one request must receive a 409 availability conflict.
5. Download the invoice and verify it is a PDF containing booking, travelers, places, rooms, vehicles, and total.
6. Cancel bookings at 7+, 3–6, and fewer than 3 days before travel and verify 100%, 50%, and 0% refunds.
7. Call the AI planner with no `LLM_API_KEY` and verify a rule-based plan plus an `ai_logs` row. Configure the key only on the backend and verify the browser never receives it.
8. Log in as admin and verify `/api/admin/**` rejects the tourist token. Upload valid and invalid room/vehicle CSV/XLSX files and inspect row-level errors.
9. Check the frontend at 360px, 768px, 1024px, and 1440px widths; keyboard-tab every form and confirm visible focus rings.
