# LLM_PROVIDER.md

This file provides guidance to LLMProvider Tooling (llm_provider.ai/code) when working with code in this repository.

## Project status

Phase 1 and Phase 2 are implemented and tested (`mvn verify` green: unit + Testcontainers
IT). Phase 1 = secure foundation + model/hardware/runtime/mock-inference/dashboard slice;
Phase 2 = benchmark/evaluation/tag CRUD, tag associations, DB-backed refresh tokens,
inference cancellation + structured parameters, dashboard fastest-models + cache eviction.
See `docs/development-roadmap.md` for what remains (real runtime adapters, observability,
CI, frontend). Keep this file in sync as the code evolves.

## Language rules (non-negotiable)

These are the easiest rules to violate and the most important to honor:

- **All code artifacts are English only**: source code, comments, package/class/method
  names, documentation files, README, API descriptions, and commit messages.
- **Conversational explanations to the user are Traditional Chinese** (architecture
  discussion, plans, implementation guidance). The split is: anything that lands in the
  repo = English; anything said *about* the repo in chat = Traditional Chinese.

## What this project is

ModelOps Dashboard — a Spring Boot backend that acts as a personal AI model registry,
inference task manager, evaluation/benchmark tracker, and monitoring dashboard. It
manages *metadata* about local AI models and simulates inference; it does not load real
models in Phase 1.

## Tech stack

Java 21, Spring Boot 3.x, Maven. Spring Web / Security / Data JPA / Validation /
WebSocket / Actuator. PostgreSQL + Flyway, Redis. Springdoc OpenAPI, MapStruct, Lombok.
Testing: JUnit 5, Mockito, Testcontainers. Docker + Docker Compose.

## Common commands

```bash
# Build (skip tests)
mvn -DskipTests package

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=AuthServiceTest

# Run a single test method
mvn test -Dtest=InferenceTaskServiceTest#runTask_marksTaskSucceeded

# Run the app locally against the dev profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Bring up PostgreSQL + Redis for local dev
docker compose up -d

# Full app + dependencies via Docker
docker compose up --build
```

Testcontainers-based integration tests require a running Docker daemon. The `test`
profile (`application-test.yml`) is what those tests load.

## Architecture

**Feature-based modular packages** under root `com.justin.modelops`. Each feature
module (auth, user, model, hardware, runtime, inference, evaluation, benchmark,
dashboard, notification, tag) contains its own `controller / service / repository /
entity / dto / mapper / enums`. Cross-cutting concerns live in `config`, `security`,
and `common` (`exception`, `response`, `pagination`, `validation`, `audit`).

Key layering rules — enforce these in every change:

- Controllers expose **DTOs only**; never serialize JPA entities out of a controller.
- **No business logic in controllers** — it belongs in services.
- Use **constructor injection** (no field `@Autowired`).
- All mutations of important state should produce an **audit log** entry.
- Entity ↔ DTO conversion goes through **MapStruct mappers**, not hand-rolled in services.

**API contract**: all endpoints under `/api/v1`. Consistent wrapper response format
(`common.response`), global exception handling (`@RestControllerAdvice` in
`common.exception`), Bean Validation on request DTOs, pagination + filtering on list
endpoints (models filter by modality/format/status/keyword; tasks by status/etc.).

**Runtime adapters are behind interfaces.** Phase 1 ships a `MockRuntimeAdapter` that
simulates inference task execution and drives the task status lifecycle
(QUEUED → RUNNING → SUCCEEDED/FAILED/CANCELED). Real backends (ONNX Runtime, DJL,
llama.cpp, Ollama, Python worker, custom HTTP) are future work — keep the adapter
interface stable so they can slot in without touching the inference service.

**WebSocket** pushes live task status updates (start/complete/fail) via the
notification module.

**Caching**: Redis backs dashboard summary and/or task status where it makes sense in
Phase 1 — do not over-cache.

**Database**: PostgreSQL, schema owned by **Flyway migrations** (never `ddl-auto=update`
for the real schema). Core entities: User, Role, AiModel, ModelFormat, HardwareProfile,
RuntimeBackend, InferenceTask, EvaluationRecord, BenchmarkDefinition, Tag, AuditLog.
Entities share base auditing fields (created/updated timestamps + actor).

## Security flow

Spring Security + JWT access tokens, BCrypt password hashing, role-based authorization.

- **Public**: `/api/v1/auth/register`, `/api/v1/auth/login`, `/api/v1/health`,
  `/swagger-ui/**`, `/v3/api-docs/**`.
- **Protected** (require valid JWT): everything else — model, hardware, runtime,
  inference, evaluation, benchmark, dashboard endpoints.

Login returns a JWT access token plus an opaque, DB-backed refresh token (SHA-256 hashed,
rotated on `/auth/refresh`, revocable via `/auth/logout`). A JWT filter authenticates
subsequent requests from the access token's claims. ADMIN-only mutations (benchmarks,
tags) use method-level `@PreAuthorize`.

## Phasing discipline

Build a strong foundation, not every feature at once. Specifically in Phase 1:

- Do **not** implement real large-model loading — simulate with the mock adapter.
- Prepare adapter interfaces for real backends; don't implement them yet.
- Add TODO comments only when they carry real signal.

## Data safety

Never put real private local file paths, secrets, or host environment details in sample
data, migrations, or seed fixtures. Use safe path aliases or storage notes for model
locations.
