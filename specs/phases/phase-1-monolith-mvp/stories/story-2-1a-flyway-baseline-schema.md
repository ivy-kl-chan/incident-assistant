# Story 2: Phase 1a — Flyway V1 baseline (reserved 1b columns)

*Label: Shared schema baseline*

## 1. Status

Implemented

## 2. Goal

Add **Flyway** with a **single baseline migration `V1`** that creates the **`incidents`** table, **`signal_ingest_audit`**, **`signal_ingest_idempotency`**, and required indexes exactly as the combined **1a + 1b** physical model describes, so later stories do not require schema churn or a greenfield **`V2`**.

## 3. User Value

All subsequent persistence work shares one forward-only schema baseline compatible with **manual incidents** today and **signal-backed** rows tomorrow.

## 4. Spec References

| Document | Relevance |
|----------|-----------|
| [`../README.md`](../README.md) | Phase pointer to **1a** / **1b** |
| [`../../phase-1a-monolith-core/data-model.md`](../../phase-1a-monolith-core/data-model.md) | **`V1`** shape: **1a** columns + nullable **1b** columns + indexes |
| [`../../phase-1b-signal-ingest/data-model.md`](../../phase-1b-signal-ingest/data-model.md) | Partial index; **`signal_ingest_audit`**, **`signal_ingest_idempotency`** (included in **`V1`** per approval) |
| [`../../phase-1a-monolith-core/spec.md`](../../phase-1a-monolith-core/spec.md) | One **`V1`** baseline; **1a** app must not use **1b** columns until **1b** stories |
| [`../../phase-1a-monolith-core/implementation-plan.md`](../../phase-1a-monolith-core/implementation-plan.md) | M1: Flyway **`V1` only** |
| [`../../phase-1a-monolith-core/test-plan.md`](../../phase-1a-monolith-core/test-plan.md) | Integration: Flyway on empty DB; **Testcontainers** normative |
| [`../../../03-acceptance-criteria.md`](../../../03-acceptance-criteria.md) | Phase 1a: schema managed by agreed approach (e.g. Flyway) |
| [`../../phase-1a-monolith-core/api-contract.md`](../../phase-1a-monolith-core/api-contract.md) | No new paths; **`transitionReason`** on **`Incident`** clarified with **`data-model.md`** (**1a**: null, no column) |

## 5. In Scope

- Flyway dependency and configuration against **PostgreSQL** (local/Testcontainers as used by the project).
- **`V1__...sql`** (single versioned script) creating **`incidents`** per **1a** `data-model.md`, including nullable **`created_by_rule_id`**, **`signal_fingerprint`**, **`telemetry_context`**, timestamps, **`version`**, **`source`** default **`MANUAL`**.
- **`telemetry_context`** stored as **JSONB** (human-approved; aligns with **PostgreSQL**-normative **1b** docs).
- Indexes: **`(status, created_at DESC)`**; partial index on **`(signal_fingerprint, created_at DESC)`** where fingerprint is not null (**1b** doc).
- Tables **`signal_ingest_audit`** and **`signal_ingest_idempotency`** per **`phase-1b-signal-ingest/data-model.md`** (human-approved: include in **`V1`** to avoid introducing **`V2`** on greenfield).

## 6. Out of Scope

- Application use of **1b** columns or **`source = SIGNAL`** (**1b** stories).
- Second Flyway version **`V2`** on greenfield (forbidden unless ADR for brownfield repair).
- **AI**, **RAG**, **MCP**, **Docker** packaging, **Kubernetes**, **microservices**.
- Data backfill or production migration runbooks beyond “empty DB forward migrate”.

## 7. API Changes

None (schema only).

## 8. Data Model Changes

- **New:** `incidents` table (**`telemetry_context`**: **JSONB**), indexes per specs; **`signal_ingest_audit`** and **`signal_ingest_idempotency`** per **`phase-1b-signal-ingest/data-model.md`**.

## 9. Business Rules

- **1a** application code introduced in later stories **must not** read/write **1b-only** semantics until **1b** stories; physical nulls remain.

## 10. Acceptance Criteria

- [x] Flyway runs on an empty PostgreSQL database and applies exactly **`V1`** (no duplicate migrations).
- [x] Table/column types and nullability match **1a** + **1b** data-model docs (**`telemetry_context`** **JSONB**; **`signal_ingest_audit`** / **`signal_ingest_idempotency`** present); indexes match (**1b** dedup partial index included).
- [x] Integration test: migration succeeds on clean DB (per **1a** `test-plan.md`: Testcontainers normative).
- [x] No application incident API required for this story’s gate.

## 11. Test Requirements

- Integration test: Flyway **`V1`** applies cleanly; optional assertion on critical columns/index names.
- Forward-only: no downgrade scripts required for demo.

## 12. Files Expected to Change

- **`src/main/resources/db/migration/V1__....sql`**, Flyway config in **`application*.yml`**, **`pom.xml`** if dependencies added, integration tests under **`src/test/java/**`**.

## 13. Implementation Notes

- **Human-approved DDL:** **`telemetry_context`** **JSONB**; **`signal_ingest_audit`** and **`signal_ingest_idempotency`** created in **`V1`** so greenfield needs no **`V2`** before **1b** idempotency/audit stories.
- **Human approval (this story):** Status **Approved**; **`telemetry_context`** type **JSONB** in **`V1`**; **`transitionReason`** in **`api-contract.md`** is **not** a **`V1` `incidents`** column — **1a** returns **`null`** for that field (no persistence of last transition reason in **1a**).

## 14. Human Review Checklist

- [ ] **V1** DDL matches both phase data models (including auxiliary **1b** tables and **JSONB** choice above).
- [ ] No additional Flyway version beyond **`V1`** required for approved scope.

## 15. Completion Notes

- **2026-04-30:** Flyway **`V1`** baseline added (`src/main/resources/db/migration/V1__baseline_incidents_and_signal_ingest.sql`), JDBC + PostgreSQL + Flyway 10 (`flyway-core`, `flyway-database-postgresql`; Spring Boot **3.4** does not publish `spring-boot-starter-flyway` in the BOM). Datasource defaults in `application.yml` with env overrides.
- **Tests:** `FlywayV1BaselineIntegrationTest` (Testcontainers PostgreSQL, `@Testcontainers(disabledWithoutDocker = true)`). `ActuatorHealthTest` excludes `DataSource` / Flyway so actuator checks stay runnable without Docker; migration coverage remains on Testcontainers when Docker is available.
- **Human `Complete`:** pending human sign-off per `review-story-implementation` / checklist §14.
