# meetingScheduler

A small scheduling backend inspired by Doodle.

The application allows users to define availability slots in their personal calendars, manage those slots, schedule meetings with other users, and query aggregated free/busy availability.

The project focuses on correctness around concurrent booking, time handling, persistence, and a clean production-oriented design.

## Tech Stack

- Java 21
- Spring Boot
- Spring MVC
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway
- Gradle
- Docker / Docker Compose
- Testcontainers
- JUnit 5 / Mockito
- Spring Boot Actuator
- Prometheus
- GitHub Actions

## Running the Application

The easiest way to start the complete application is with Docker Compose.

```bash
docker compose up --build
```

The API will then be available at:

```text
http://localhost:8080
```

To stop the application:

```bash
docker compose down
```

To also remove the local PostgreSQL volume:

```bash
docker compose down -v
```

## Running Tests

Run all unit, controller, and integration tests with:

```bash
./gradlew clean test
```

Integration tests use Testcontainers and run against a real PostgreSQL instance.

Docker must therefore be available when running the integration tests.

## Domain Model

The main domain concepts are:

```text
User
  |
  └── Calendar
        |
        └── TimeSlot
              |
              └── Meeting

Meeting
  ├── Organizer
  └── Participants
```

Each user owns exactly one calendar.

A `TimeSlot` belongs to a calendar and represents an explicitly defined interval of availability.

A `Meeting` is created from an existing free slot and may involve multiple users.

## Database Migrations

Flyway manages the database schema.

The initial schema creates:

- users
- calendars
- time slots
- meetings
- meeting participants
- indexes and integrity constraints

Hibernate schema generation is disabled:

```text
ddl-auto: validate
```

This allows Hibernate to verify the mappings while Flyway remains responsible for schema ownership.

## Observability

Spring Boot Actuator exposes operational endpoints.

```text
GET /actuator/health
GET /actuator/info
GET /actuator/metrics
GET /actuator/prometheus
```

Prometheus-compatible metrics are exposed through Micrometer.

Example:

```bash
curl "http://localhost:8080/actuator/health"
```
## API
[API Documentation](docs/API.md)


## Continuous Integration

GitHub Actions runs the automated test suite on pushes and pull requests targeting `main`.

Integration tests use Testcontainers, so PostgreSQL does not need to be separately configured in the CI workflow.

## Possible Future Improvements

With more time, possible extensions include:

- arbitrary interval matching and slot splitting
- meeting cancellation and rescheduling
- pagination for larger availability queries
- authentication and authorization
- richer timezone-aware client APIs
- structured application logging and tracing
- additional business metrics
- load and performance testing
- API documentation using OpenAPI

## Summary

The implementation prioritizes:

- transactional correctness
- concurrency safety
- explicit domain modeling
- UTC-safe time handling
- database-level integrity
- automated testing against PostgreSQL
- local reproducibility with Docker
- basic production observability
- continuous integration