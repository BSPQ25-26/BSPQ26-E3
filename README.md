BSPQ26-E3 - Supabase user API
=============================

This project uses Spring Boot to expose a REST API backed by a Supabase PostgreSQL database through Spring Data JPA.

Launching the application
-------------------------

Check the dependencies in `pom.xml` and the database configuration in `src/main/resources/application.properties`.

Create a `.env` file in the project root with your Supabase database credentials:

```env
DB_URL=jdbc:postgresql://db.<your-project-ref>.supabase.co:5432/postgres?sslmode=require
DB_USERNAME=postgres
DB_PASSWORD=<your-supabase-database-password>
```

The application maps the existing Supabase table `"user"` with the columns `id`, `created_at`, `phone`, and `mail`.

Compile the project:

```bash
mvn compile
```

Run the server:

```bash
mvn spring-boot:run
```

If the database credentials are correct and the `user` table already exists, the API will be available at `http://localhost:8080/`.

REST API
--------

Retrieve all users:

```http
GET http://localhost:8080/api/users
```

Create a user:

```http
POST http://localhost:8080/api/users
Content-Type: application/json

{
  "phone": "+34123456789",
  "mail": "user@example.com"
}
```

Delete a user:

```http
DELETE http://localhost:8080/api/users/1
```

Swagger is available at `http://localhost:8080/swagger-ui.html`.

Command line client
-------------------

There is a sample REST client in `AppUserManager.java`. You can launch it with:

```bash
mvn exec:java
```

Packaging the application
-------------------------

Package the application with:

```bash
mvn package
```

Run the packaged server with:

```bash
java -jar target/rest-api-0.0.1-SNAPSHOT.jar
```
