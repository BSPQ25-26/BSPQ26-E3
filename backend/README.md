# Backend - REST API

Spring Boot REST API that provides the server-side logic and data access for the application.

## Tech Stack

- **Java 21** with **Spring Boot 3.4.2**
- **Spring Data JPA** for database access
- **PostgreSQL** (Supabase hosted)
- **SpringDoc OpenAPI** for API documentation (Swagger UI)

## Project Structure

```
src/main/java/com/example/
├── config/              # CORS and app configuration
├── restapi/
│   ├── controller/      # REST endpoints (AppUserController, ItemController)
│   ├── service/         # Business logic (AppUserService, ItemService)
│   ├── repository/      # Spring Data JPA repositories
│   ├── model/           # JPA entities (Profile, Item, Category)
│   ├── dto/             # Request/response DTOs
│   └── client/          # CLI client (AppUserManager)
```

## API Endpoints

| Method | Endpoint                          | Description              |
|--------|-----------------------------------|--------------------------|
| POST   | `/api/users`                      | Register a new user      |
| POST   | `/api/users/login`                | Login                    |
| GET    | `/api/users`                      | List all users           |
| GET    | `/api/users/{id}`                 | Get user by ID           |
| GET    | `/api/users/profile`              | Get profile by email/username |
| PUT    | `/api/users/{id}`                 | Update user              |
| DELETE | `/api/users/{id}`                 | Delete user              |

## Running Locally

1. Create a `.env` file in this directory (see `../.env.example` for the template):

   ```env
   DB_URL=jdbc:postgresql://your-host:5432/postgres?sslmode=require
   DB_USERNAME=your_username
   DB_PASSWORD=your_password
   SUPABASE_URL=https://your-project.supabase.co
   SUPABASE_ANON_KEY=your_anon_key
   SUPABASE_SERVICE_ROLE_KEY=your_service_role_key
   ```

2. Run the application:

   ```bash
   mvn spring-boot:run
   ```

3. The API will be available at `http://localhost:8080`

4. Swagger UI: `http://localhost:8080/swagger-ui.html`

## Testing

### Unit and integration tests

```bash
mvn test
```

Runs every test under `src/test/java` **except** acceptance tests, which are excluded
via a surefire filter on `**/*AcceptanceTest.java`.

### Acceptance tests (Selenium)

End-to-end tests that drive the React UI in headless Chrome. They mirror the
[book-api-acceptance-selenium](https://github.com/dipina/book-api-acceptance-selenium)
example with one structural difference: the frontend is a separate container, so the
tests do **not** boot the app via `@SpringBootTest`. The full stack must be running
beforehand.

Tests live in `src/test/java/com/example/restapi/acceptance/`:

| File | What it covers |
|---|---|
| `BaseAcceptanceTest.java` | Shared setup: health-check, headless Chrome driver, `loginAsTestUser()` helper |
| `RegisterLoginAcceptanceTest.java` | Login with valid and invalid credentials |
| `PlantBrowsingAcceptanceTest.java` | Plant grid and details modal |
| `CartCheckoutAcceptanceTest.java` | Add to cart, simulated checkout, receipt |
| `PurchaseSalesHistoryAcceptanceTest.java` | Purchases and Sales tabs render |

#### Prerequisites

1. **Google Chrome** installed locally (WebDriverManager handles the driver automatically).
2. **The stack must be running** at `http://localhost` (frontend nginx) and
   `http://localhost:8080` (backend). The easiest way is Docker:

   ```bash
   docker compose up -d --build
   ```

3. **A confirmed Supabase user.** The tests log in with credentials read from the
   environment:

   ```env
   TEST_USER_EMAIL=acceptance.tester@example.com
   TEST_USER_PASSWORD=ChangeMe!123
   ```

   The user must exist in `auth.users` with `email_confirmed_at` set, and have a
   matching row in `public.profiles`. The repository ships with one already seeded
   in the shared Supabase project (see `backend/.env`).

#### Running

```bash
cd backend
mvn test -Pacceptance
```

Only `**/*AcceptanceTest.java` runs under this profile. The default `mvn test` skips
them so CI doesn't need a browser.

#### Notes

- The fake `PaymentService` rejects ~10% of payments at random. `CartCheckoutAcceptanceTest`
  retries the checkout up to 4 times to keep flakiness negligible.
- The catalogue test assumes at least one item exists; the project ships with 8.
- The shared Supabase project must allow the seeded user to remain confirmed —
  do not delete the user manually.
