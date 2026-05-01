# Story 9: Phase 1a — Integration gate: readiness, docs, no ingest route

*Label: 1a exit criteria*

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
| [`story-18-1a-pr-ci-docker-for-testcontainers.md`](story-18-1a-pr-ci-docker-for-testcontainers.md) | **PR CI + Docker** so Testcontainers tests (e.g. **`FlywayV1BaselineIntegrationTest`**) **run**, not skip, in default CI |

## 5. In Scope

- **`GET /actuator/health/readiness`**: reflects PostgreSQL connectivity when configured (recommended for production-like demos).
- **README** (project root): prerequisites, **local JVM + PostgreSQL** (or Testcontainers-only dev if documented), how to run tests, links to **1a** specs.
- Verify **no** **`/api/v1/signal-ingest/*`** controller mapping ships in **1a-only** builds (or ingest module not on classpath—team choice; outcome must be **no route**).
- Consolidate any **H2**-only local path limitations per **ADR**/README if offered.

## 6. Out of Scope

- **Container image** and **compose** deliverables required by **`03-acceptance-criteria.md`** Phase **1a** — covered by **Story 8** (`story-8-1a-container-packaging.md`).
- **OpenTelemetry Demo** reproduction and **Journey A** end-to-end walkthrough — **Story 15** (`story-15-1b-otel-demo-journey-a.md`).
- **1b** implementation and demo stack (**Stories 10–17**; see [`README.md`](README.md)).
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
- [ ] **1a** `test-plan.md` scenarios achievable green in default CI (PostgreSQL via Testcontainers), including **[Story 18](story-18-1a-pr-ci-docker-for-testcontainers.md)** merged so Testcontainers-backed tests **execute** (not skipped for missing Docker).

## 11. Test Requirements

- Integration test for readiness **UP** with DB and **DOWN**/degraded when DB unavailable (as contract allows **503** on incident paths—readiness should not claim “UP” if incidents would **503**—align semantics).

## 12. Files Expected to Change

- **`README.md`**, **`src/main/java/**`** actuator/health customization, **`src/test/java/**`**, optional **`docs/`** notes.

## 13. Implementation Notes

- **Story 8** closes the **1a** container/image/compose items in **`03-acceptance-criteria.md`**; **this story (9)** remains the **integration gate** for readiness semantics, **no ingest route**, and **test-plan** green for **1a** APIs.
- **`specs/03-acceptance-criteria.md`** Phase **1a** DoD requires **Stories 1–9** together (**Story 8** + **Story 9** jointly satisfy image/compose/readiness/no-ingest expectations), with **[Story 18](story-18-1a-pr-ci-docker-for-testcontainers.md)** merged so **default CI** exercises **Testcontainers** (not skipped).
- **Story 18** supplies **default PR CI with Docker** for **Testcontainers**; without it, **Story 9** acceptance on “green default CI” is **not** satisfied.

## 14. Human Review Checklist

- [ ] **1a** DoD items covered by **Stories 1–9** and **Story 18** (PR CI + Docker) are checked off in release notes.
- [ ] Readiness semantics match **`api-contract.md`** when DB is up/down.

## 15. Completion Notes

*(Fill when implemented.)*
