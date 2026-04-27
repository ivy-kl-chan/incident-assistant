# Test plan — Phase 1a

**Principle:** Default CI: **no** OpenTelemetry stack, **no** signal ingest tests (those are **`phase-1b-signal-ingest/test-plan.md`**). **PostgreSQL via Testcontainers** is the **default** integration-test database from day one (`docs/adr/0001-kickoff-tooling-testing-and-1a-scope.md`).

## Docker / CI image

| Policy | Default |
|--------|---------|
| **B** | Document manual `docker compose` smoke; optional nightly image build |
| **A** | PR builds image — optional strict mode |

**Phase 1a complete** = Policy **B** unless upgraded.

## Required tests (1a)

### Unit

- State transition matrix; illegal from-state → **409**; invalid **`to`** / body → **400**.
- Title, description, severity validation.
- **412** on missing/wrong `If-Match` for `PATCH` and `transitions`.

### Integration

- `POST` → `GET` with `ETag`; `PATCH` success/failure; `transitions` with `If-Match`.
- List: pagination, bad query → **400**; unknown query key (e.g. **`source`**) → **400**; verify OpenAPI has **no** `source` parameter.
- `GET` unknown id → **404**.
- Flyway migration on empty DB; forward-only.

### OpenAPI

- `specs/openapi/openapi-1a.yaml` matches implemented **1a** surface.

### Error coverage

- At least one test each: **400**, **404**, **409**, **412**, **413**/validation, **415** (wrong `Content-Type`), **503** (persistence unavailable where specified) as applicable per `api-contract.md`.

## Tooling

- JUnit 5, AssertJ, Spring Boot Test, **Testcontainers PostgreSQL** (required for default CI integration tests unless an ADR documents an exception).
