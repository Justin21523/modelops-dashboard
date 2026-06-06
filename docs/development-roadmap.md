# Development Roadmap

## Phase 1 — Secure foundation & vertical slice (current)

Delivered:

- Maven project, Docker Compose (PostgreSQL + Redis), Dockerfile, `.env.example`.
- Uniform API response envelope, global exception handling, JPA auditing + audit log.
- JWT security (register/login, BCrypt, role-based authorization, `/users/me`).
- Model registry CRUD with modality/format/status/keyword filtering.
- Hardware profile and runtime backend CRUD.
- Inference task lifecycle with a mock runtime adapter and async execution.
- Dashboard summary / recent tasks / model distribution.
- WebSocket (STOMP) live task status broadcasting.
- Flyway baseline migration; dev data seeder using safe storage aliases.
- OpenAPI/Swagger UI.
- Unit tests (Mockito) + Testcontainers repository integration test.

Schema and entities are also in place for `evaluation`, `benchmark`, and `tag` so Phase 2
can build their APIs without a migration redesign.

## Phase 2 — Evaluation, benchmarks, tags & token refresh (delivered)

- Benchmark definition CRUD with ADMIN-restricted mutations (`@PreAuthorize`).
- Evaluation record CRUD: links model + hardware + runtime + benchmark; metrics
  (latency, tokens/sec, memory, score, notes); specification filtering and per-model
  aggregation.
- Tag CRUD (ADMIN-restricted mutations) and many-to-many associations to models,
  benchmarks, and tasks (`@BatchSize` to avoid N+1), with attach/detach endpoints and
  tag-based filtering.
- DB-backed refresh tokens (SHA-256 hashed) with rotation and revocation; `/auth/refresh`,
  `/auth/logout`; account management (`PATCH /users/me`, password change that revokes
  refresh tokens).
- Inference task cancellation (cooperative, status-guarded) and structured generation
  parameters (validated, stored as JSON).
- Dashboard "fastest models" endpoint and Redis cache eviction on model / evaluation /
  task mutations.
- Added unit tests (benchmark, evaluation, refresh token, cancellation), a Testcontainers
  tag-association and refresh-token IT, and an RBAC web-layer IT.

## Phase 3 — Real runtime adapters

Implement `RuntimeAdapter` for real backends behind the existing interface:

- llama.cpp server adapter
- Ollama adapter
- ONNX Runtime (Java) adapter
- DJL adapter
- OpenAI-compatible / custom HTTP adapter

Add capability negotiation and health checks for runtime backends.

## Phase 4 — Observability & delivery

- Prometheus metrics + Grafana dashboards.
- GitHub Actions CI (build, test with Testcontainers, image publish).
- Hardened deployment configuration and secrets management.

## Phase 5 — Frontend

- React dashboard consuming the REST API and the WebSocket task feed.
