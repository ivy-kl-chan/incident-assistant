# Story 14: Phase 1b — Automated test matrix completion

*Label: Test matrix (1b gate)*

## 1. Status

Planned

## 2. Goal

Close remaining **1b** `test-plan.md` items not explicitly gated by Stories **11–13**: rule internal **500**, validation **422** paths (clock / nested limits), unknown **`ruleId`** integration coverage cross-check, and documented **429** rate limiting stance (**implemented** vs **not implemented**).

## 3. User Value

Ingest and read paths behave predictably under failure and strict validation—reducing surprise during demos and CI.

## 4. Spec References

| Document | Relevance |
|----------|-----------|
| [`../../phase-1b-signal-ingest/test-plan.md`](../../phase-1b-signal-ingest/test-plan.md) | Failure matrix: **500**, **422**, **429** if on |
| [`../../phase-1b-signal-ingest/api-contract.md`](../../phase-1b-signal-ingest/api-contract.md) | Status codes and validation rules |
| [`../../phase-1b-signal-ingest/implementation-plan.md`](../../phase-1b-signal-ingest/implementation-plan.md) | **B6** **1b** gate |
| [`../../../03-acceptance-criteria.md`](../../../03-acceptance-criteria.md) | Phase **1b** tests bullet list |

## 5. Prerequisites, dependencies, and blocked by

- **Stories [11](story-11-1b-signal-ingest-http-evaluations.md)–[13](story-13-1b-incident-read-extensions-openapi.md)** — primary **1b** HTTP surfaces so the test matrix can target real controllers and contracts.

## 6. In Scope

- Automated tests proving **500** path when a rule evaluator throws (test double / stub implementation).
- Automated tests for **422** (or **400** where contract allows) on **`observedAt`** window violations and oversized/too-deep **`fingerprintInputs`** / **`telemetryPointers`** per **`api-contract.md`**.
- Document **429**: if not implemented, state “not implemented” in README/OpenAPI; if implemented, add tests per **`test-plan.md`**.
- Audit Stories **11–13** for any **test-plan.md** row still missing and add targeted tests.

## 7. Out of Scope

- New product features or rule ids.
- **Docker**, **OpenTelemetry Demo**, **Kubernetes**, **microservices**, **AI**, **RAG**, **MCP**.

## 8. API Changes

None unless **429** is newly implemented (then document in OpenAPI/README).

## 9. Data Model Changes

None.

## 10. Business Rules

- **500** responses must not include stack traces in JSON bodies (**RFC 7807** alignment with **1a** error style where applicable).

## 11. Acceptance Criteria

- [ ] **test-plan.md** **Integration** and **Unit** sections for **1b** are satisfied by automated tests in default CI (**PostgreSQL** via Testcontainers).
- [ ] **500** rule-throw path covered.
- [ ] **422**/validation paths for clock and nested JSON limits covered.
- [ ] **429** behavior explicitly documented; tests exist **if** feature exists.

## 12. Test Requirements

- JUnit / Spring tests only; no new external services.

## 13. Files Expected to Change

- **`src/test/java/**`** primarily; small README/OpenAPI notes if **429** stance changes.

## 14. Implementation Notes

- Prefer test doubles over changing production rule implementations.

## 15. Human Review Checklist

- [ ] No skipped **test-plan.md** rows without explicit issue link (per portfolio rules for deferrals).

## 16. Completion Notes

*(Fill when implemented.)*
