# Story 2: Phase 1a — Flyway V1 baseline (reserved 1b columns)

*Label: Shared schema baseline*

## 1. Status

Planned

## 2. Goal

Add **Flyway** with a **single baseline migration `V1`** that creates the **`incidents`** table (and optional **1b** tables/indexes) exactly as the combined **1a + 1b** physical model describes, so later stories do not require schema churn.

## 3. User Value

All subsequent persistence work shares one forward-only schema baseline compatible with **manual incidents** today and **signal-backed** rows tomorrow.

## 4. Spec References

| Document | Relevance |
|----------|-----------|
| [`../README.md`](../README.md) | Phase pointer to **1a** / **1b** |
| [`../../phase-1a-monolith-core/data-model.md`](../../phase-1a-monolith-core/data-model.md) | **`V1`** shape: **1a** columns + nullable **1b** columns + indexes |
| [`../../phase-1b-signal-ingest/data-model.md`](../../phase-1b-signal-ingest/data-model.md) | Partial index, optional **`signal_ingest_audit`**, **`signal_ingest_idempotency`** |
| [`../../phase-1a-monolith-core/spec.md`](../../phase-1a-monolith-core/spec.md) | One **`V1`** baseline; **1a** app must not use **1b** columns until **1b** stories |
| [`../../phase-1a-monolith-core/implementation-plan.md`](../../phase-1a-monolith-core/implementation-plan.md) | M1: Flyway **`V1` only** |
| [`../../../03-acceptance-criteria.md`](../../../03-acceptance-criteria.md) | Phase 1a: schema managed by agreed approach (e.g. Flyway) |

## 5. In Scope

- Flyway dependency and configuration against **PostgreSQL** (local/Testcontainers as used by the project).
- **`V1__...sql`** (single versioned script) creating **`incidents`** per **1a** `data-model.md`, including nullable **`created_by_rule_id`**, **`signal_fingerprint`**, **`telemetry_context`**, timestamps, **`version`**, **`source`** default **`MANUAL`**.
- Indexes: **`(status, created_at DESC)`**; partial index on **`(signal_fingerprint, created_at DESC)`** where fingerprint is not null (**1b** doc).
- Optional tables **`signal_ingest_audit`** and **`signal_ingest_idempotency`** if the team commits to **1b** idempotency/audit in **V1** (recommended if **1b** will land without **V2**).

## 6. Out of Scope

- Application use of **1b** columns or **`source = SIGNAL`** (**1b** stories).
- Second Flyway version **`V2`** on greenfield (forbidden unless ADR for brownfield repair).
- **AI**, **RAG**, **MCP**, **Docker** packaging, **Kubernetes**, **microservices**.
- Data backfill or production migration runbooks beyond “empty DB forward migrate”.

## 7. API Changes

None (schema only).

## 8. Data Model Changes

- **New:** `incidents` table and indexes per specs; optional **1b** auxiliary tables per **`phase-1b-signal-ingest/data-model.md`**.

## 9. Business Rules

- **1a** application code introduced in later stories **must not** read/write **1b-only** semantics until **1b** stories; physical nulls remain.

## 10. Acceptance Criteria

- [ ] Flyway runs on an empty PostgreSQL database and applies exactly **`V1`** (no duplicate migrations).
- [ ] Table/column types and nullability match **1a** + **1b** data-model docs (including index definitions required for **1b** dedup).
- [ ] Integration test: migration succeeds on clean DB (per **1a** `test-plan.md`: Testcontainers normative).
- [ ] No application incident API required for this story’s gate.

## 11. Test Requirements

- Integration test: Flyway **`V1`** applies cleanly; optional assertion on critical columns/index names.
- Forward-only: no downgrade scripts required for demo.

## 12. Files Expected to Change

- **`src/main/resources/db/migration/V1__....sql`**, Flyway config in **`application*.yml`**, **`pom.xml`** if dependencies added, integration tests under **`src/test/java/**`**.

## 13. Implementation Notes

- Resolve optional **1b** tables in **V1** with team preference: including them avoids a later **`V2`** for greenfield; omitting them blocks **1b** idempotency until a follow-up migration (conflicts with “no **V2** on greenfield” for **1b**—prefer including optional tables if **1b** is in scope for Phase 1).

## 14. Human Review Checklist

- [ ] **V1** DDL matches both phase data models.
- [ ] Choice on optional **1b** tables is explicit and documented.

## 15. Completion Notes

*(Fill when implemented.)*
