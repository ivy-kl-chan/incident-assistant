# Test plan — Phase 1a

**Principle:** Default CI: **no** OpenTelemetry stack, **no** signal ingest tests (those are **`phase-1b-signal-ingest/test-plan.md`**).

## Docker / CI image

| Policy | Default |
|--------|---------|
| **B** | Document manual `docker compose` smoke; optional nightly image build |
| **A** | PR builds image — optional strict mode |

**Phase 1a complete** = Policy **B** unless upgraded.

## Required tests (1a)

### Unit

- State transition matrix; illegal → **409** domain / HTTP mapping.
- Title, description, severity validation.
- **412** on missing/wrong `If-Match` for `PATCH` and `transitions`.

### Integration

- `POST` → `GET` with `ETag`; `PATCH` success/failure; `transitions` with `If-Match`.
- List: pagination, bad query → **400**; `source=SIGNAL` on list → **400** in **1a-only** build.
- `GET` unknown id → **404**.
- Flyway migration on empty DB; forward-only.

### OpenAPI

- `specs/openapi/openapi-1a.yaml` matches implemented **1a** surface.

### Error coverage

- At least one test each: **400**, **404**, **409**, **412**, **413**/validation as applicable.

## Tooling

- JUnit 5, AssertJ, Spring Boot Test, **Testcontainers PostgreSQL** recommended.
