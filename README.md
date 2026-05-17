# BSPQ26-E3

Full-stack web application for user and item management. Built with a **Spring Boot** REST API backend connected to **Supabase PostgreSQL**, and a **React** frontend.

## Features

- User registration, login, and profile management
- Item listing and management
- REST API with CRUD operations
- Swagger UI for API documentation

## Project Structure

```
BSPQ26-E3/
├── backend/           # Spring Boot REST API (Java 21)
├── frontend/          # React SPA
├── docker-compose.yml # Run the full stack with Docker
└── .env.example       # Environment variables template
```

See each folder's README for details on that part of the project.

---

## Quick Start with Docker

The easiest way to run the entire project is with Docker.

### Prerequisites

- [Docker](https://docs.docker.com/get-docker/) and Docker Compose

### Steps

1. **Configure environment variables:**

   ```bash
   cp .env.example backend/.env
   ```

   Edit `backend/.env` with your Supabase credentials.

2. **Build and start all services:**

   ```bash
   docker compose up --build
   ```

3. **Access the application:**

   | Service      | URL                                      |
   |--------------|------------------------------------------|
   | Frontend     | http://localhost                          |
   | Backend API  | http://localhost:8080                     |
   | Swagger UI   | http://localhost:8080/swagger-ui.html     |

4. **Stop the services:**

   ```bash
   docker compose down
   ```

---

## Local Development (without Docker)

### Prerequisites

- Java 21
- Maven
- Node.js 20+ and npm

### 1. Configure environment variables

Create `backend/.env` from the template:

```bash
cp .env.example backend/.env
```

Edit it with your Supabase credentials.

### 2. Run the backend

```bash
cd backend
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

### 3. Run the frontend

```bash
cd frontend
npm install
npm start
```

The frontend will be available at `http://localhost:3000`.

---

## Testing

- **Backend unit and integration tests:** `cd backend && mvn test`
- **Selenium acceptance tests (UI end-to-end):** requires the stack running
  (`docker compose up -d`) and a confirmed Supabase user. See
  [backend/README.md](backend/README.md#acceptance-tests-selenium) for setup and
  the `mvn test -Pacceptance` command.

## Git Workflow

See [GitWorkflow.md](GitWorkflow.md) for the branching and contribution strategy.
