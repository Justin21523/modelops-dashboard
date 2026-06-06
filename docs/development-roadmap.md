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

## Phase 2 — Evaluation, benchmarks, tags & token refresh

- Evaluation record CRUD: link model + hardware + runtime + benchmark; metrics (latency,
  tokens/sec, memory, score, notes); filtering and aggregation.
- Benchmark definition CRUD (coding, Chinese writing, VLM image understanding,
  summarization, custom).
- Tag CRUD and many-to-many associations to models, benchmarks, and tasks.
- Refresh tokens and token revocation; account management.
- Inference task cancellation endpoint and richer parameter handling.
- Cache eviction wiring on mutations; dashboard "fastest models".

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
