# API Design

All endpoints are versioned under `/api/v1` and return the uniform envelope.

## Response envelope

Success:

```json
{ "success": true, "data": { ... }, "timestamp": "2026-06-06T12:00:00Z" }
```

Error:

```json
{
  "success": false,
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "AiModel not found: 42",
    "path": "/api/v1/models/42",
    "fieldErrors": [ { "field": "name", "message": "must not be blank" } ]
  },
  "timestamp": "2026-06-06T12:00:00Z"
}
```

Error codes: `VALIDATION_FAILED` (400), `AUTHENTICATION_FAILED` (401), `ACCESS_DENIED`
(403), `RESOURCE_NOT_FOUND` (404), `RESOURCE_CONFLICT` / `INVALID_STATE_TRANSITION`
(409), `INTERNAL_ERROR` (500).

## Pagination & filtering

List endpoints accept `page`, `size`, and `sort` (Spring `Pageable`) and return a
`PageResponse`: `content`, `page`, `size`, `totalElements`, `totalPages`, `first`,
`last`. Filters are passed as query parameters and ignored when omitted.

## Endpoints

### Health (public)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/health` | Liveness check |

### Auth & users
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/auth/register` | public | Register, returns access + refresh tokens |
| POST | `/auth/login` | public | Log in, returns access + refresh tokens |
| POST | `/auth/refresh` | public | Exchange a refresh token for a new pair (rotates) |
| POST | `/auth/logout` | bearer | Revoke a refresh token |
| GET | `/users/me` | bearer | Current user profile |
| PATCH | `/users/me` | bearer | Update profile (display name) |
| POST | `/users/me/change-password` | bearer | Change password (revokes refresh tokens) |

The refresh token is opaque and rotated on every `/auth/refresh`; the previous token is
revoked, so reuse is rejected. Tokens are stored only as SHA-256 hashes.

### Models
| Method | Path | Description |
|--------|------|-------------|
| POST | `/models` | Create model |
| GET | `/models` | List; filters: `modality`, `formatType`, `status`, `keyword`, `tagId` |
| GET | `/models/{id}` | Get by id |
| PATCH | `/models/{id}` | Partial update |
| DELETE | `/models/{id}` | Delete |
| POST | `/models/{id}/tags/{tagId}` | Attach a tag |
| DELETE | `/models/{id}/tags/{tagId}` | Detach a tag |

### Hardware profiles
`POST /hardware-profiles`, `GET /hardware-profiles`, `GET /hardware-profiles/{id}`,
`PATCH /hardware-profiles/{id}`, `DELETE /hardware-profiles/{id}`.

### Runtime backends
`POST /runtime-backends`, `GET /runtime-backends`, `GET /runtime-backends/{id}`,
`PATCH /runtime-backends/{id}`, `DELETE /runtime-backends/{id}`.

### Inference tasks
| Method | Path | Description |
|--------|------|-------------|
| POST | `/inference-tasks` | Create a `QUEUED` task (optional structured `parameters`) |
| GET | `/inference-tasks` | List; filter: `status` |
| GET | `/inference-tasks/{id}` | Get by id |
| POST | `/inference-tasks/{id}/run` | Execute asynchronously (mock adapter) |
| POST | `/inference-tasks/{id}/cancel` | Cancel a queued or running task |
| POST | `/inference-tasks/{id}/tags/{tagId}` | Attach a tag |
| DELETE | `/inference-tasks/{id}/tags/{tagId}` | Detach a tag |

`parameters` is a structured object: `{ temperature, topP, maxTokens, seed, stop[] }`,
validated and persisted as JSON.

### Benchmarks
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/benchmarks` | ADMIN | Create a benchmark definition |
| GET | `/benchmarks` | bearer | List; filters: `type`, `keyword`, `tagId` |
| GET | `/benchmarks/{id}` | bearer | Get by id |
| PATCH | `/benchmarks/{id}` | ADMIN | Partial update |
| DELETE | `/benchmarks/{id}` | ADMIN | Delete |
| POST/DELETE | `/benchmarks/{id}/tags/{tagId}` | ADMIN | Attach/detach a tag |

### Evaluations
| Method | Path | Description |
|--------|------|-------------|
| POST | `/evaluations` | Create an evaluation record |
| GET | `/evaluations` | List; filters: `modelId`, `benchmarkId`, `hardwareProfileId`, `runtimeBackendId`, `status` |
| GET | `/evaluations/aggregate` | Per-model aggregate (optional `benchmarkId`) |
| GET | `/evaluations/{id}` | Get by id |
| PATCH | `/evaluations/{id}` | Partial update |
| DELETE | `/evaluations/{id}` | Delete |

### Tags
| Method | Path | Auth | Description |
|--------|------|------|-------------|
| POST | `/tags` | ADMIN | Create a tag |
| GET | `/tags` | bearer | List tags |
| GET | `/tags/{id}` | bearer | Get by id |
| PATCH | `/tags/{id}` | ADMIN | Partial update |
| DELETE | `/tags/{id}` | ADMIN | Delete |

### Dashboard
| Method | Path | Description |
|--------|------|-------------|
| GET | `/dashboard/summary` | Counters and averages |
| GET | `/dashboard/recent-tasks` | Ten most recent tasks |
| GET | `/dashboard/model-distribution` | Counts by modality and format |
| GET | `/dashboard/fastest-models` | Top models by avg throughput (`limit`, default 5) |

### WebSocket
- Handshake: `GET /ws` (SockJS)
- Subscribe: `/topic/tasks` (all) and `/topic/tasks/{id}` (single task)
- Message: `TaskStatusMessage` `{ taskId, modelId, status, message, latencyMs, tokensPerSecond, timestamp }`

## Conventions

- Request bodies are validated with Bean Validation; failures return `VALIDATION_FAILED`
  with per-field detail.
- `PATCH` requests apply only non-null fields.
- Create endpoints respond `201 Created`; delete responds `204 No Content`.
- Enums are serialized as their names (e.g. `"GGUF"`, `"SUCCEEDED"`).
