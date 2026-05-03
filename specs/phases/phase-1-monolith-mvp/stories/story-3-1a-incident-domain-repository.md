# Story 3: Phase 1a — Incident domain and manual persistence

*Label: Domain & persistence*

## 1. Status

Reviewed

## 2. Goal

Implement the **incident** domain and persistence for **manually created** rows only: **`source = MANUAL`**, statuses **`DRAFT` | `OPEN` | `CLOSED` | `CANCELLED`**, optimistic **`version`** for later **ETag** mapping, validation aligned with **1a** API rules.

## 3. User Value

Core incident data is stored reliably with correct invariants before HTTP controllers expose it.

## 4. Spec References

| Document | Relevance |
|----------|-----------|
| [`../README.md`](../README.md) | Phase ordering |
| [`../../phase-1a-monolith-core/spec.md`](../../phase-1a-monolith-core/spec.md) | State machine; create is **DRAFT** only; **1a** ignores **1b** columns |
| [`../../phase-1a-monolith-core/data-model.md`](../../phase-1a-monolith-core/data-model.md) | Column usage for **1a**; **`created_at`** / **`updated_at`** |
| [`../../phase-1a-monolith-core/api-contract.md`](../../phase-1a-monolith-core/api-contract.md) | Field constraints for create/patch (title, description, severity); **PATCH** when status not **`DRAFT`**/**`OPEN`** |
| [`../../phase-1a-monolith-core/test-plan.md`](../../phase-1a-monolith-core/test-plan.md) | Unit: validation; full **state transition matrix** and **HTTP** mapping — [**Story 6**](../stories/story-6-1a-incident-lifecycle-transitions.md) |

## 5. In Scope

- Domain types / entities / value objects for incident fields and status.
- Persistence layer (repository or DAO) inserting/updating **manual** incidents only.
- Enforcement: **`source`** remains **`MANUAL`** for all writes from this layer; never **`SIGNAL`**.
- **`version`** increment rules for updates (foundation for **412** behavior in HTTP layer).
- Title (1–200 trimmed), description (max 32_768 after trim), severity (**SEV1**–**SEV4**) validation at domain or service boundary.
- **Update invariants (align with `api-contract.md` `PATCH` rules):** reject changes to **`title`**, **`description`**, or **`severity`** when current status is **`CLOSED`** or **`CANCELLED`**, with clear domain/service errors mappable later to **HTTP 409** (transitions that change status are [**Story 6**](../stories/story-6-1a-incident-lifecycle-transitions.md), not this story).
- On insert: set **`created_at`** and **`updated_at`**; on successful update: advance **`updated_at`** (per **`data-model.md`**).

## 6. Out of Scope

- REST controllers and **OpenAPI** (Stories 4–7).
- **`ETag` / `If-Match`** HTTP headers (handled in Story 5; domain may still expose `version`).
- **Lifecycle transitions** (**`DRAFT`→`OPEN`**, etc.) and the **full illegal-transition matrix** at HTTP or domain-orchestration level — [**Story 6**](../stories/story-6-1a-incident-lifecycle-transitions.md).
- **Signal ingest**, **fingerprint**, **telemetry_context**, **`created_by_rule_id`** writes.
- **AI**, **RAG**, **MCP**, **Docker**, **Kubernetes**, **microservices**.

## 7. API Changes

None (internal/domain + persistence only).

## 8. Data Model Changes

None beyond Story **2** (uses existing **`incidents`** table).

## 9. Business Rules

- States: **`DRAFT` → `OPEN` → `CLOSED`**; **`CANCELLED`** from **`DRAFT`** or **`OPEN`**; **no** transition from **`CLOSED`** back to **`OPEN`** in v1 (transition application: **Story 6**).
- **`POST` create** semantics at domain level: new row **`DRAFT`** only (no “create as **OPEN**”).
- **`source`**: always **`MANUAL`** for code paths in this story.
- **`PATCH`**-aligned field updates: **`title`**, **`description`**, **`severity`** may be updated only while status is **`DRAFT`** or **`OPEN`**; if status is **`CLOSED`** or **`CANCELLED`**, reject such updates (later **409**).

## 10. Acceptance Criteria

- [x] Can persist a new incident as **`DRAFT`** with valid title/severity via service/repository API used by tests.
- [x] Invalid **title** / **severity** / **description** (length after trim) rejected with clear domain/service errors mappable later to **HTTP 400**.
- [x] **`source`** column stored as **`MANUAL`**; **1b** columns remain untouched (null).
- [x] **`version`** behavior documented for updates (increment on successful mutation).
- [x] **`created_at`** and **`updated_at`** set on insert; **`updated_at`** (and **`version`**) updated on successful mutation; documented in code or short **Javadoc** on the service/repository responsible.
- [x] Update path rejects **`title`**/**`description`**/**`severity`** changes when status is **`CLOSED`** or **`CANCELLED`**, with errors mappable to **409**.

## 11. Test Requirements

- **Unit tests:** validation (**title**, **description** length, **severity** enum); **not** the full state-transition matrix (that is **Story 6**).
- **Unit tests:** when an incident is **`CLOSED`** or **`CANCELLED`**, applying **`title`**/**`description`**/**`severity`** updates via the domain/service API fails with an error suitable for later **409** mapping.
- **Integration test** (optional with Testcontainers): round-trip insert/select **MANUAL** row; assert **`created_at`**/**`updated_at`** present and **`updated_at`** changes after an allowed update (status stays **`DRAFT`** or **`OPEN`** for that update).

## 12. Files Expected to Change

- **`src/main/java/com/incidentassistant/`** — e.g. **`domain/`** (entities, value objects, domain exceptions), **`persistence/`** (repository implementations, JPA or JDBC adapters as chosen).
- **`src/test/java/com/incidentassistant/`** — unit tests under **`domain`** / **`persistence`** (and optional integration test module/package mirroring the above).

## 13. Implementation Notes

- Keep a port/repository boundary so **1b** ingest can reuse storage patterns without rewriting **1a** manual flows (**1b** `data-model.md` “SignalIngestPort” note is design guidance for later).

## 14. Human Review Checklist

*(Required before merge.)*

- [x] State machine **documentation** in domain matches **1a** spec (actual transition operations: **Story 6**).
- [x] No accidental write to **1b**-reserved columns.
- [x] **`PATCH`**-aligned rule (**CLOSED**/**`CANCELLED`**) enforced for field updates.
- [x] Timestamps and **`version`** behavior match **`data-model.md`**.

## 15. Completion Notes

- **Date:** 2026-05-02
- **Summary:** Domain types, `IncidentValidator`, `ManualIncidentService`, port `ManualIncidentRepository`, and `JdbcManualIncidentRepository` (insert/update with optimistic CAS). Persistence beans are `@ConditionalOnBean(JdbcTemplate.class)` so tests that exclude `DataSource` (e.g. actuator slice) still start.
- **Tests:** `IncidentValidatorTest`, `ManualIncidentServiceTest`, `ManualIncidentRepositoryIntegrationTest` (Testcontainers PostgreSQL; skipped when Docker unavailable via `@Testcontainers(disabledWithoutDocker = true)`). **CI** should run the integration test (Docker); local skip without Docker is acceptable.
- **Human review (2026-05-02):** §14 checklist completed pre-merge. Policy: optional Testcontainers may skip on dev machines without Docker; **CI** must exercise Testcontainers/DB round-trip.
- **PR/commit:** *(none yet — local implementation)*
