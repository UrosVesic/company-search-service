# Company Search Service

A Spring Boot backend service that searches for companies across two mock third-party data sources (FREE and PREMIUM), handles failures with automatic fallback, and stores verification records in PostgreSQL.

Built with **Kotlin**, **Spring Boot 3.5**, **Spring Data JDBC**, **Liquibase**, and **PostgreSQL**.

## Prerequisites

- **Java 21**
- **Docker** (required for both run options below)
- **Maven** (or use the included `./mvnw` wrapper)

## How to Run

### Option 1: Full Docker Setup (app + database)

This builds the application into a Docker image and runs it alongside PostgreSQL. No Java installation needed on the host.

```bash
docker compose -f docker/docker-compose.yaml up --build
```

The app will be available at `http://localhost:8080`.

To stop:

```bash
docker compose -f docker/docker-compose.yaml down
```

### Option 2: Maven Spring Boot Run (app starts the database container automatically)

Spring Boot Docker Compose support is included as a dependency. When you run the app with Maven, it automatically starts the PostgreSQL container defined in `compose.yaml` and stops it when the app shuts down.

```bash
./mvnw spring-boot:run
```

That's it -- no need to manually start the database. Spring Boot detects `compose.yaml` in the project root, starts the Postgres container, and configures the datasource connection automatically.

The app will be available at `http://localhost:8080`.

## API Endpoints

### Backend Service (main endpoint)

```
GET /backend-service?verificationId={uuid}&query={searchText}
```

Searches for active companies by CIN (company identification number). Calls the FREE service first, falls back to PREMIUM if the FREE service returns 503 or no results. Stores a verification record for every request.

**Example:**

```bash
curl "http://localhost:8080/backend-service?verificationId=550e8400-e29b-41d4-a716-446655440000&query=123"
```

### FREE Third-Party Service

```
GET /free-third-party?query={searchText}
```

Returns companies (snake_case format) whose CIN contains the query. Simulates a 40% failure rate (HTTP 503).

### PREMIUM Third-Party Service

```
GET /premium-third-party?query={searchText}
```

Returns companies (camelCase format) matching the query. Simulates a 10% failure rate (HTTP 503).

### Verification Retrieval

```
GET /verifications/{verificationId}
```

Retrieves a stored verification record by its ID.

### Swagger UI

API documentation is available at:

```
http://localhost:8080/swagger-ui/index.html
```

## Running Tests

Tests use Testcontainers to spin up a PostgreSQL container automatically -- Docker must be running.

```bash
./mvnw test
```

## Project Structure

```
src/main/kotlin/com/code/companysearchservice/
  backend/          -- Main backend service (controller, service, client, mapper)
  free/             -- FREE third-party mock service
  premium/          -- PREMIUM third-party mock service
  verification/     -- Verification storage and retrieval
  common/           -- Shared exception handling
```

## Configuration

Key properties in `src/main/resources/application.yaml`:

| Property | Default | Description |
|---|---|---|
| `app.free-service.failure-rate` | `0.40` | Probability of FREE service returning 503 |
| `app.premium-service.failure-rate` | `0.10` | Probability of PREMIUM service returning 503 |
