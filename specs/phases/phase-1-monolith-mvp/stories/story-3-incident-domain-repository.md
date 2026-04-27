# Story 3 — Incident domain and manual persistence

## 1. Status

Planned

## 2. Goal

Implement the **incident** domain and persistence for **manually created** rows only: **`source = MANUAL`**, statuses **`DRAFT` | `OPEN` | `CLOSED` | `CANCELLED`**, optimistic **`version`** for later **ETag** mapping, validation aligned with **1a** API rules.

## 3. User Value

Core incident data is stored reliably with correct invariants before HTTP controllers expose it.

## 4. Spec References

| Document | Relevance |
|----------|-----------|
| [`../README.md`](../README.md) | Phase ordering |
| [`../../phase-1a-monolith-core/spec.md`](../../phase-1a-monolith-core/spec.md) | State machine; create is **DRAFT** only; **1a** ignores **1b** columns |
| [`../../phase-1a-monolith-core/data-model.md`](../../phase-1a-monolith-core/data-model.md) | Column usage for **1a** |
| [`../../phase-1a-monolith-core/api-contract.md`](../../phase-1a-monolith-core/api-contract.md) | Field constraints for create/patch (title, description, severity) |
| [`../../phase-1a-monolith-core/test-plan.md`](../../phase-1a-monolith-core/test-plan.md) | Unit: validation, transition matrix (can start here or complete in Story 6) |

## 5. In Scope

- Domain types / entities / value objects for incident fields and status.
- Persistence layer (repository or DAO) inserting/updating **manual** incidents only.
- Enforcement: **`source`** remains **`MANUAL`** for all writes from this layer; never **`SIGNAL`**.
- **`version`** increment rules for updates (foundation for **412** behavior in HTTP layer).
- Title (1–200 trimmed), description (max 32_768), severity (**SEV1**–**SEV4**) validation at domain or service boundary.

## 6. Out of Scope

- REST controllers and **OpenAPI** (Stories 4–7).
- **`ETag` / `If-Match`** HTTP headers (handled in Story 5; domain may still expose `version`).
- **Signal ingest**, **fingerprint**, **telemetry_context**, **`created_by_rule_id`** writes.
- **AI**, **RAG**, **MCP**, **Docker**, **Kubernetes**, **microservices**.

## 7. API Changes

None (internal/domain + persistence only).

## 8. Data Model Changes

None beyond Story **2** (uses existing **`incidents`** table).

## 9. Business Rules

- States: **`DRAFT` → `OPEN` → `CLOSED`**; **`CANCELLED`** from **`DRAFT`** or **`OPEN`**; **no** transition from **`CLOSED`** back to **`OPEN`** in v1.
- **`POST` create** semantics at domain level: new row **`DRAFT`** only (no “create as **OPEN**”).
- **`source`**: always **`MANUAL`** for code paths in this story.

## 10. Acceptance Criteria

- [ ] Can persist a new incident as **`DRAFT`** with valid title/severity via service/repository API used by tests.
- [ ] Invalid title/severity rejected with clear domain/service errors mappable later to **HTTP 400**.
- [ ] **`source`** column stored as **`MANUAL`**; **1b** columns remain untouched (null).
- [ ] **`version`** behavior documented for updates (increment on successful mutation).

## 11. Test Requirements

- Unit tests: validation; illegal state transitions return errors suitable for later **409** mapping.
- Integration test (optional with Testcontainers): round-trip insert/select **MANUAL** row.

## 12. Files Expected to Change

- **`src/main/java/**`** domain, persistence (e.g. JPA entities, JDBC, Spring Data), **`src/test/java/**`** unit tests.

## 13. Implementation Notes

- Keep a port/repository boundary so **1b** ingest can reuse storage patterns without rewriting **1a** manual flows (**1b** `data-model.md` “SignalIngestPort” note is design guidance for later).

## 14. Human Review Checklist

- [ ] State machine matches **1a** spec table.
- [ ] No accidental write to **1b**-reserved columns.

## 15. Completion Notes

*(Fill when implemented.)*
