# BSPQ26-E3

## About the project

Full-stack application for user management. The backend is built with **Spring Boot** and exposes a REST API connected to a **Supabase PostgreSQL** database via Spring Data JPA. The frontend is built with **React** and allows managing users through a web interface.

Main features:
- User registration, login, and profile management
- REST API with CRUD operations on users
- Swagger UI for API documentation

---

## How to run

### Prerequisites

- Java 17+
- Maven
- Node.js and npm

### 1. Configure environment variables

Create a `.env` file in the project root with your Supabase credentials:

```env
DB_URL=jdbc:postgresql://db.<your-project-ref>.supabase.co:5432/postgres?sslmode=require
DB_USERNAME=postgres
DB_PASSWORD=<your-supabase-database-password>
```

### 2. Run the backend

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.
Swagger UI: `http://localhost:8080/swagger-ui.html`

### 3. Run the frontend

```bash
cd frontend
npm install
npm start
```

The frontend will be available at `http://localhost:3000`.
