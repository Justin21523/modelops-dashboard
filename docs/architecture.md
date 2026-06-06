# Architecture

## Overview

ModelOps Dashboard is a modular Spring Boot backend. It is organized by **feature
module** rather than by technical layer, so each domain owns its full vertical stack and
can evolve independently.

```
com.justin.modelops
├── config          # OpenAPI, Redis cache, WebSocket, JPA auditing, async, dev seeder
├── security        # JWT provider/filter, security config, user details, REST error handlers
├── common          # response envelope, exception handling, pagination, audit
├── auth            # register / login
├── user            # user + role entities, /users/me
├── model           # AI model registry (+ model formats)
├── hardware        # hardware profiles
├── runtime         # runtime backends
├── inference       # inference tasks + runtime adapters + async executor
├── evaluation      # evaluation records (schema only in Phase 1)
├── benchmark       # benchmark definitions (schema only in Phase 1)
├── dashboard       # aggregation endpoints
├── notification    # WebSocket task status broadcasting
└── tag             # reusable tags (schema only in Phase 1)
```

Each feature module typically contains `controller`, `service`, `repository`, `entity`,
`dto`, `mapper`, and `enums`.

## Layering rules

- **Controllers** expose DTOs only — JPA entities never cross the controller boundary.
- **Business logic** lives in services; controllers only adapt HTTP ↔ service calls.
- **Constructor injection** everywhere (via Lombok `@RequiredArgsConstructor`).
- **MapStruct** handles entity ↔ DTO conversion (`*MapperImpl` generated at build time).
- **Flyway** owns the schema; `spring.jpa.hibernate.ddl-auto=validate` guards drift.

## Cross-cutting concerns

### Uniform response envelope
Every endpoint returns `ApiResponse<T>` (`success`, `data`, `error`, `timestamp`). List
endpoints wrap a `PageResponse<T>` decoupled from Spring Data's `Page`.

### Global exception handling
`GlobalExceptionHandler` (`@RestControllerAdvice`) maps `BusinessException` /
`ResourceNotFoundException` (via `ErrorCode`), bean-validation failures, and
authentication/authorization errors to the envelope. Security filter-chain failures are
rendered by `RestAuthenticationEntryPoint` (401) and `RestAccessDeniedHandler` (403).

### Auditing
All domain entities extend `BaseAuditEntity` (`id`, `createdAt`, `updatedAt`,
`createdBy`, `updatedBy`, optimistic-lock `version`). Spring Data JPA auditing populates
these; the actor is resolved from the security context. Important actions
(create/update/delete/run, login/register) also write an append-only `AuditLog` row via
`AuditService`.

## Security flow

1. `POST /auth/register` or `/auth/login` → `AuthService` verifies credentials through
   the `AuthenticationManager` (BCrypt) and `JwtTokenProvider` issues an HS256 access
   token carrying the subject and `roles` claim.
2. `JwtAuthenticationFilter` extracts the bearer token, validates it, and populates the
   `SecurityContext` **from the token claims** — no database hit per request.
3. `SecurityConfig` is stateless; public endpoints are whitelisted, everything else
   requires authentication. Method-level security is enabled for future fine-grained
   rules.

**Refresh tokens** are opaque, DB-backed, and stored only as SHA-256 hashes
(`RefreshTokenService`). Login/registration issue an access token plus a refresh token;
`/auth/refresh` validates the token, **revokes it, and issues a new pair (rotation)** so a
replayed token is rejected; `/auth/logout` revokes a single token and a password change
revokes all of a user's tokens. ADMIN-only operations (benchmark and tag mutations) are
enforced with method-level `@PreAuthorize("hasRole('ADMIN')")`.

## Inference execution

Execution is abstracted behind the `RuntimeAdapter` interface
(`supports(BackendType)` + `run(InferenceTask)`). `RuntimeAdapterRegistry` selects an
adapter by the task's backend type and falls back to `MockRuntimeAdapter` when no real
adapter is registered — so Phase 1 tasks always execute.

The lifecycle is event-driven to keep the request thread fast and avoid a service ↔
executor cyclic dependency:

```
POST /inference-tasks/{id}/run
   └─ InferenceTaskService.run()  (validates QUEUED, audits, publishes event, returns)
        └─ InferenceTaskQueuedEvent
             └─ InferenceTaskExecutor.onTaskQueued()   @Async @TransactionalEventListener
                  ├─ markRunning()    → persist + broadcast RUNNING
                  ├─ adapter.run()
                  └─ markSucceeded()/markFailed() → persist metrics + broadcast terminal
```

Each state transition is its own transaction (`markRunning` / `markSucceeded` /
`markFailed`) so progress is persisted and observable, and each broadcasts a
`TaskStatusMessage` over STOMP (`/topic/tasks` and `/topic/tasks/{id}`).

**Cancellation is cooperative and race-safe.** `cancel()` moves a non-terminal task to
`CANCELED`; the transition methods are guarded by the current status (`markRunning` only
acts on `QUEUED`, `markSucceeded`/`markFailed` only on `RUNNING`) and return `null` when
the guard fails, so the executor skips the corresponding step and a task canceled mid-run
is not overwritten. Generation parameters are accepted as a structured
`InferenceParameters` object, validated, and persisted as JSON in the task's
`parameters` column (no schema change).

## Caching

`DashboardService` methods (`summary`, `modelDistribution`, `fastestModels`) are
`@Cacheable` in Redis with a short TTL (30s, see `RedisConfig`). Model, evaluation, and
inference-task mutations carry `@CacheEvict(allEntries = true)` on the `dashboard` cache so
aggregates stay fresh between TTL windows. The serializer embeds type information so the
immutable record DTOs round-trip correctly.

## Tagging

`Tag` is associated to `AiModel`, `BenchmarkDefinition`, and `InferenceTask` via
`@ManyToMany` join tables (`model_tags`, `benchmark_tags`, `task_tags`). The collections
are `LAZY` with `@BatchSize(50)` so list endpoints that project tags issue a few batched
`IN` queries instead of N+1, avoiding the in-memory pagination pitfall of collection
fetch-joins. Tag-based filtering joins the association inside the JPA `Specification` with
`query.distinct(true)`.

## Persistence model

Enums are stored as `VARCHAR` (`@Enumerated(STRING)`). Key relationships:

- `ai_models.format_id → model_formats`
- `inference_tasks.model_id → ai_models`, `inference_tasks.runtime_backend_id → runtime_backends`
- `evaluation_records` → model / hardware_profile / runtime_backend / benchmark
- `users` ↔ `roles` via `user_roles`

## Testing strategy

- **Unit tests** (Mockito) cover service logic and the inference lifecycle without a
  Spring context or Docker.
- **Integration tests** (`*IT`, Testcontainers PostgreSQL) run real Flyway migrations and
  verify schema validity, JPA auditing, and specification-based filtering. They execute
  in the Maven `verify` phase via the failsafe plugin.
