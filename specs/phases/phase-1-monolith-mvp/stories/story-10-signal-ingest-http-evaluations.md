# Story 10 — Signal ingest: HTTP surface, auth, validation, and transactional create/dedup

## 1. Status

Planned

## 2. Goal

Deliver **`POST /api/v1/signal-ingest/evaluations`** end-to-end for **enabled** signals: **constant-time** token auth, **`signals.enabled`** **404** when off, full request validation, rule evaluation (Story **9**), **fingerprint** computation, and **Option A** deduplication with **`pg_advisory_xact_lock`** inside **one** database transaction per **1b** `data-model.md`.

## 3. User Value

Matched evaluations create **signal-sourced** **draft** incidents exactly once per cooldown policy, with deterministic **200/201** outcomes for duplicates and new episodes.

## 4. Spec References

| Document | Relevance |
|----------|-----------|
| [`../../phase-1b-signal-ingest/api-contract.md`](../../phase-1b-signal-ingest/api-contract.md) | Ingest route, auth, validation, response bodies (**200** false, **200** dedup, **201** create), errors |
| [`../../phase-1b-signal-ingest/data-model.md`](../../phase-1b-signal-ingest/data-model.md) | Fingerprint, **Option A** matrix, advisory lock mapping, **`SIGNAL_DEDUP_COOLDOWN`**, transaction steps |
| [`../../phase-1b-signal-ingest/spec.md`](../../phase-1b-signal-ingest/spec.md) | **PostgreSQL** normative |
| [`../../phase-1b-signal-ingest/test-plan.md`](../../phase-1b-signal-ingest/test-plan.md) | Integration: **401**, **404**, **200** paths, **201**, **concurrency** two parallel same-fingerprint |
| [`../../phase-1b-signal-ingest/rules/registry.yaml`](../../phase-1b-signal-ingest/rules/registry.yaml) | Defaults for title/severity on create |

## 5. In Scope

- Config **`signals.enabled`**; **404** on ingest when **`false`**.
- **`X-Integration-Token`** vs **`SIGNAL_INGEST_TOKEN`**; **constant-time** compare; **401** on failure.
- **`Content-Type`** enforcement; JSON size limits (**64 KiB**); field validation per **`SignalEvaluationRequest`** table in **`api-contract.md`**.
- Wire **Story 9** evaluators; **200** **`{ "matched": false }`** when not matched (no DB incident writes).
- On match: compute **`signal_fingerprint`**; in a **transaction**: advisory lock, lookup most recent in-window row, apply **Option A** matrix (**200** **`DUPLICATE_SIGNAL`** vs **201** new draft for **OPEN**/**CLOSED**/**CANCELLED** / **DRAFT** cases per table).
- Persist **`telemetry_context`**, **`created_by_rule_id`**, **`source=SIGNAL`**, title/description/severity per **`data-model.md`** “Signal-created defaults”.
- Config **`SIGNAL_DEDUP_COOLDOWN`** (default **15 minutes**).
- Integration test: **two parallel** ingests same fingerprint, empty pre-state → **exactly one** new **DRAFT** row.

## 6. Out of Scope

- **`Idempotency-Key`** header store and replay (**Story 11**).
- **`GET`** incident list **`source`** filter and detail field extensions (**Story 12**).
- **`openapi-1b.yaml`** updates (**Story 12**).
- **OpenTelemetry Demo**, **Docker** compose, **Kubernetes**, **microservices**.
- **AI**, **RAG**, **MCP**.

## 7. API Changes

- **New:** `POST /api/v1/signal-ingest/evaluations` (fully functional per **1b** contract except **Idempotency-Key** deferred to Story **11**).

## 8. Data Model Changes

None if **V1** already included **1b** columns and partial index (Story **2**). Optional **`signal_ingest_audit`** writes—defer to Story **11** if treated as optional.

## 9. Business Rules

- **Normative ingest algorithm** steps **1–6** in **1b** `data-model.md` (**Option A** only unless ADR for **Option B**).
- **Fingerprint** canonicalization: stable sorted JSON of **`{ "ruleId", "fingerprintInputs" }`** → **SHA-256** lowercase hex (**64** chars).
- **Telemetry** stored shape and max **8 KiB** per **1b** `data-model.md`; **no secrets** in **`deepLinks`**.

## 10. Acceptance Criteria

- [ ] **`signals.enabled=false`** → **404** on ingest **POST**.
- [ ] **401**/**415**/**400**/**422** cases per **`api-contract.md`** and **`test-plan.md`** covered by automated tests where practical.
- [ ] **200** **`{ "matched": false }`** when rule does not match.
- [ ] **201** new **`DRAFT`** with **`source=SIGNAL`** when matched and matrix says new row.
- [ ] **200** dedup body **`{ "created": false, "incidentId", "reason": "DUPLICATE_SIGNAL" }`** when matrix applies.
- [ ] **Concurrency** integration test on **PostgreSQL** passes (parallel same fingerprint).
- [ ] **Default CI** uses **Testcontainers PostgreSQL** for these tests (**no** mandatory external stack).

## 11. Test Requirements

- Unit tests: fingerprint stable across key order permutations (**1b** `test-plan.md`).
- Integration tests: matrix spot-checks (**DRAFT** dup vs **OPEN** new episode, etc.) per **`data-model.md`** table.

## 12. Files Expected to Change

- **`src/main/java/**`** ingest controller, service, transaction boundary, JDBC/JPA repositories; **`src/test/java/**`**; **`application*.yml`**.

## 13. Implementation Notes

- **Metrics → traces → logs** ordering in **`implementation-plan.md`** is a **demo narrative** for later fixtures; this story’s HTTP contract already accepts **`telemetryPointers`** as a required object—tests may use minimal stub payloads. Expanding fixture richness is a follow-on thin story if needed (**design note**, not required for gate).

## 14. Human Review Checklist

- [ ] **Option A** matrix matches **`data-model.md`** exactly.
- [ ] Advisory key derivation documented (one approach from spec options).

## 15. Completion Notes

*(Fill when implemented.)*
