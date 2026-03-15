BSPQ26-E3 - Supabase user API
==============================
This project uses Spring Boot to expose a REST API backed by a Supabase PostgreSQL database
through Spring Data JPA, with a React frontend for user management.

Launching the application
-------------------------
Check the dependencies in `pom.xml` and the database configuration in
`src/main/resources/application.properties`.

Create a `.env` file in the project root with your Supabase database credentials:
```env
DB_URL=jdbc:postgresql://db.<your-project-ref>.supabase.co:5432/postgres?sslmode=require
DB_USERNAME=postgres
DB_PASSWORD=<your-supabase-database-password>
```

The application maps the existing Supabase table `"user"` with the columns
`id`, `created_at`, `username`, `email`, `password`, and `phone`.

Compile and run the backend:
```bash
mvn compile
mvn spring-boot:run
```

If the database credentials are correct and the `user` table already exists,
the API will be available at `http://localhost:8080/`.

Frontend
--------
The React frontend is located in the `frontend/` folder.
```bash
cd frontend
npm install
npm start
```

The frontend will be available at `http://localhost:3000/`.

REST API
--------
Retrieve all users:
```http
GET http://localhost:8080/api/users
```

Create a user:
```http
POST http://localhost:8080/api/users

{
  "username": "user0",
  "email": "user0@example.com",
  "phone": "+34123456789",
  "password": "secret"
}
```

Update a user:
```http
PUT http://localhost:8080/api/users/{id}

{
  "username": "user",
  "email": "user@example.com",
  "phone": "+34123456789",
  "password": "secret"
}
```

Delete a user:
```http
DELETE http://localhost:8080/api/users/{id}
```

Login:
```http
POST http://localhost:8080/api/users/login

{
  "username": "user",
  "password": "user"
}
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
