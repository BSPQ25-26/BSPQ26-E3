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
