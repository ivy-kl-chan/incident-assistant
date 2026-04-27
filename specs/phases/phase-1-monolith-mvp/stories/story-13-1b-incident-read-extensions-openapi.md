# Story 13: Phase 1b — Incident read API extensions and OpenAPI 1b

*Label: Read extensions & OpenAPI 1b*

## 1. Status

Planned

## 2. Goal

Extend **`GET /api/v1/incidents`** and **`GET /api/v1/incidents/{id}`** for **1b** when **`signals.enabled=true`**: **`source`** query on list (**default MANUAL-only**), extended detail fields for signal rows, and publish/update **`specs/openapi/openapi-1b.yaml`** merged per **`specs/openapi/README.md`**.

## 3. User Value

Clients can filter mixed manual/signal inventories and inspect **telemetry** metadata on signal-backed drafts using the same read APIs.

## 4. Spec References

| Document | Relevance |
|----------|-----------|
| [`../../phase-1b-signal-ingest/api-contract.md`](../../phase-1b-signal-ingest/api-contract.md) | List **`source`** semantics; detail **`telemetryContext`**, **`createdByRuleId`**, **`signalFingerprint`** |
| [`../../phase-1a-monolith-core/api-contract.md`](../../phase-1a-monolith-core/api-contract.md) | Baseline list/get behavior and unknown query key **400** |
| [`../../phase-1b-signal-ingest/spec.md`](../../phase-1b-signal-ingest/spec.md) | **1b** extensions |
| [`../../../openapi/openapi-1b.yaml`](../../../openapi/openapi-1b.yaml) | Normative **1b** OpenAPI surfaces |
| [`../../../openapi/README.md`](../../../openapi/README.md) | Merge expectations with **`openapi-1a.yaml`** |
| [`../../phase-1b-signal-ingest/test-plan.md`](../../phase-1b-signal-ingest/test-plan.md) | List **`source`** tests; **GET** detail optional fields |
| [`../../../03-acceptance-criteria.md`](../../../03-acceptance-criteria.md) | **1b** DoD: **`openapi-1b.yaml`**; list default **MANUAL**; **`source=ALL`** |

## 5. In Scope

- **`GET /api/v1/incidents`**: optional **`source`** query (**`MANUAL`**, **`SIGNAL`**, **`ALL`**); **omitted** → **MANUAL** rows only; invalid/duplicate/empty/comma-separated → **400**.
- **`GET /api/v1/incidents/{id}`**: include **`telemetryContext`**, **`createdByRuleId`**, **`signalFingerprint`** when present; **`null`/omitted** for pure manual rows per contract.
- **`ETag`** remains on detail **GET** per **1a**/**1b** merge expectations.
- **`specs/openapi/openapi-1b.yaml`**: ingest + extended list/get; align with implemented controllers.
- README updates: **1b** API usage examples (**curl**), **`SIGNAL_INGEST_TOKEN`**, **`signals.enabled`** (full stack walkthrough lives in **Story 15**).

## 6. Out of Scope

- **`POST/PATCH`** incident changes for signal fields (not in **1b** contract as automatic—manual patch rules remain **1a**).
- **New** ingest routes beyond evaluations.
- **AI**, **RAG**, **MCP**, **Kubernetes**, **microservices**, end-to-end **demo stack** runbooks (**Story 15**).

## 7. API Changes

- **Extend:** `GET /api/v1/incidents`, `GET /api/v1/incidents/{id}` per **1b** `api-contract.md`.

## 8. Data Model Changes

None (read-only mapping to existing columns).

## 9. Business Rules

- **Security:** **`deepLinks`** treated as untrusted display URLs; no secrets embedded (**1b** `api-contract.md`).

## 10. Acceptance Criteria

- [ ] **Omitted** **`source`** returns only **`MANUAL`** rows even when **`SIGNAL`** rows exist in DB.
- [ ] **`source=ALL`** returns mixed; **`source=SIGNAL`** only signal rows; **`source=MANUAL`** same as omitted.
- [ ] Invalid **`source`** → **400**.
- [ ] Detail **GET** shows extended fields for **`SIGNAL`** rows created in Story **11** fixtures.
- [ ] **`openapi-1b.yaml`** matches live behavior for listed paths and merges cleanly with **1a** OpenAPI per repo **`openapi/README.md`**.

## 11. Test Requirements

- Integration tests from **1b** `test-plan.md` for list/detail extensions.
- Optional contract check: diff OpenAPI vs controller annotations if tooling exists.

## 12. Files Expected to Change

- **`src/main/java/**`** list/get controllers/DTO mappers; **`specs/openapi/openapi-1b.yaml`**; **`src/test/java/**`**; **`README.md`** (API examples only).

## 13. Implementation Notes

- Ensure **OpenAPI** documents **`Idempotency-Key`** and **ingest** schemas added in Stories **11–12** if not already mirrored.

## 14. Human Review Checklist

- [ ] Default list behavior preserves **1a** expectations (manual-only when **`source`** omitted).
- [ ] OpenAPI merge story is clear for consumers.

## 15. Completion Notes

*(Fill when implemented.)*
