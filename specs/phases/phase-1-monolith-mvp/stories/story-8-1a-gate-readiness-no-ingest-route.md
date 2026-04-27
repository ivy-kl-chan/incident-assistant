# Story 8 — Phase 1a integration gate: readiness, docs, no ingest route

## 1. Status

Planned

## 2. Goal

Close **1a**-specific acceptance gaps: **readiness** reflects database availability, documentation describes **local bare-metal** run and tests, and **`POST /api/v1/signal-ingest/*`** is **not registered** (ingest absent in **1a**).

## 3. User Value

Operators can trust readiness probes once the DB is wired; contributors have a clear **non-container** quickstart; the **1a** boundary (no signal ingest) is unambiguous.

## 4. Spec References

| Document | Relevance |
|----------|-----------|
| [`../README.md`](../README.md) | Implement **1a** before **1b** |
| [`../../phase-1a-monolith-core/spec.md`](../../phase-1a-monolith-core/spec.md) | Ingest route absent in **1a**; **404** **`signals.enabled`** is **1b** only |
| [`../../phase-1a-monolith-core/api-contract.md`](../../phase-1a-monolith-core/api-contract.md) | Readiness checks DB |
| [`../../phase-1a-monolith-core/test-plan.md`](../../phase-1a-monolith-core/test-plan.md) | Policy B CI; Testcontainers |
| [`../../phase-1a-monolith-core/implementation-plan.md`](../../phase-1a-monolith-core/implementation-plan.md) | M6 **1a** gate |
| [`../../../03-acceptance-criteria.md`](../../../03-acceptance-criteria.md) | Phase **1a** DoD (see Out of Scope for items this story set intentionally omits) |

## 5. In Scope

- **`GET /actuator/health/readiness`**: reflects PostgreSQL connectivity when configured (recommended for production-like demos).
- **README** (project root): prerequisites, **local JVM + PostgreSQL** (or Testcontainers-only dev if documented), how to run tests, links to **1a** specs.
- Verify **no** **`/api/v1/signal-ingest/*`** controller mapping ships in **1a-only** builds (or ingest module not on classpath—team choice; outcome must be **no route**).
- Consolidate any **H2**-only local path limitations per **ADR**/README if offered.

## 6. Out of Scope

- **Container image** and **compose** deliverables required by **`03-acceptance-criteria.md`** Phase **1a** — covered by **Story 14** (`story-14-1a-container-packaging.md`).
- **OpenTelemetry Demo** reproduction and **Journey A** end-to-end walkthrough — **Story 15** (`story-15-1b-otel-demo-journey-a.md`).
- **1b** ingest implementation (**Stories 9–13**).
- **AI**, **RAG**, **MCP**, **Kubernetes**, **microservices** product scope.

## 7. API Changes

None new; **readiness** behavior clarified/finalized only.

## 8. Data Model Changes

None.

## 9. Business Rules

- **1a** remains **manual incidents** only at the HTTP surface.

## 10. Acceptance Criteria

- [ ] Readiness status accurately reports DB up/down in integration tests or manual checklist (documented).
- [ ] README enables clone → configure DB → run app → hit **health** + **incident** smoke path on **bare metal**.
- [ ] Confirmed: **`POST /api/v1/signal-ingest/*`** not registered in **1a** delivery.
- [ ] **1a** `test-plan.md` scenarios achievable green in default CI (PostgreSQL via Testcontainers).

## 11. Test Requirements

- Integration test for readiness **UP** with DB and **DOWN**/degraded when DB unavailable (as contract allows **503** on incident paths—readiness should not claim “UP” if incidents would **503**—align semantics).

## 12. Files Expected to Change

- **`README.md`**, **`src/main/java/**`** actuator/health customization, **`src/test/java/**`**, optional **`docs/`** notes.

## 13. Implementation Notes

- **Story 14** closes the **1a** container/image/compose items in **`03-acceptance-criteria.md`**; this story (**8**) remains the **integration gate** for readiness semantics, **no ingest route**, and **test-plan** green for **1a** APIs.

## 14. Human Review Checklist

- [ ] **1a** DoD items covered by Stories **1–8** plus **Story 14** are checked off in release notes.
- [ ] Readiness semantics match **`api-contract.md`** when DB is up/down.

## 15. Completion Notes

*(Fill when implemented.)*
