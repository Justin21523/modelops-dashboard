# ModelOps Dashboard

A personal AI model registry, inference task manager, evaluation tracker, and monitoring
dashboard backend, built as a production-grade Spring Boot reference project.

It manages **metadata** about local AI models (formats, quantization, hardware
requirements, runtime backends) and simulates inference task execution. Phase 1 does not
load real models — execution is handled by a pluggable mock runtime adapter, with
interfaces prepared for real backends (llama.cpp, Ollama, ONNX Runtime, DJL) in later
phases.

## Tech stack

Java 21 · Spring Boot 3.4 · Maven · Spring Web / Security / Data JPA / Validation /
WebSocket / Actuator · PostgreSQL + Flyway · Redis · JWT (JJWT) · MapStruct · Lombok ·
springdoc OpenAPI · JUnit 5 · Mockito · Testcontainers · Docker Compose.

## Features

- **Auth & Users** — register, login, JWT access tokens, **DB-backed refresh tokens with
  rotation/revocation** (`/auth/refresh`, `/auth/logout`), BCrypt hashing, role-based
  authorization, `/users/me`, profile update, and password change.
- **Model Registry** — CRUD with filtering by modality, format, status, keyword, and tag.
- **Hardware Profiles** — CRUD for GPU/VRAM/RAM/backend configurations.
- **Runtime Backends** — CRUD for inference backends and their capabilities/status.
- **Inference Tasks** — create (with structured generation parameters), list (filter by
  status), run asynchronously through a mock adapter, **cancel** (cooperative), lifecycle
  `QUEUED → RUNNING → SUCCEEDED/FAILED/CANCELED`.
- **Benchmarks** — CRUD for reusable benchmark definitions (ADMIN-only mutations).
- **Evaluations** — CRUD linking model + hardware + runtime + benchmark, with filtering
  and per-model metric aggregation.
- **Tags** — CRUD (ADMIN-only mutations) and many-to-many associations to models,
  benchmarks, and inference tasks (attach/detach + tag filtering).
- **Dashboard** — summary counters, recent tasks, model distribution, fastest models.
- **WebSocket** — live task status broadcast over STOMP (`/topic/tasks`).
- **Cross-cutting** — uniform API response envelope, global exception handling, JPA
  auditing, audit log, Redis caching with eviction on mutation, OpenAPI docs.

## Quick start

```bash
# 1. Start PostgreSQL + Redis
cp .env.example .env
docker compose up -d postgres redis

# 2. Run the app (dev profile seeds sample data)
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

The dev profile seeds a demo user — **username `demo`, password `demo-password`** — plus
sample models, hardware, and runtimes.

- API base: `http://localhost:8080/api/v1`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health: `http://localhost:8080/api/v1/health`

### Example flow

```bash
# Register (or log in with the seeded demo user)
curl -s -X POST localhost:8080/api/v1/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"demo","password":"demo-password"}'

# Use the returned accessToken as: Authorization: Bearer <token>
curl -s localhost:8080/api/v1/dashboard/summary -H "Authorization: Bearer $TOKEN"
```

### Run everything in Docker

```bash
docker compose --profile full up --build
```

## Build & test

```bash
mvn clean compile          # compile (Lombok + MapStruct annotation processing)
mvn test                   # unit tests (Mockito, no Docker required)
mvn verify                 # + Testcontainers integration tests (requires Docker)
mvn test -Dtest=AuthServiceTest                 # a single test class
mvn test -Dtest=ModelServiceTest#get_throwsWhenModelMissing   # a single method
```

> **Note:** integration tests use Testcontainers and require a running Docker daemon.
> The Docker Engine API version is pinned (`api.version=1.40`) in the failsafe plugin so
> modern daemons that reject docker-java's legacy default work out of the box.

## Documentation

- [`docs/architecture.md`](docs/architecture.md) — architecture and design decisions
- [`docs/api-design.md`](docs/api-design.md) — endpoint reference and conventions
- [`docs/development-roadmap.md`](docs/development-roadmap.md) — phased roadmap

## Security & data safety

Sample data uses safe storage **aliases** (e.g. `alias://models/llama3-8b`); no real
private host paths or secrets are committed. The bundled JWT secret is for local
development only — override `APP_SECURITY_JWT_SECRET` in any real deployment.
