# ADR 0001 — Kickoff: tooling, API, testing, and Phase 1a scope

**Status:** Accepted  
**Date:** 2026-04-26  
**Context:** README “Before Phase 1a” blocking and recommended questions.

## Decision

1. **Build tool:** **Maven** (repo layout, CI, and docs assume Maven).
2. **Persistence / CI:** **Testcontainers (PostgreSQL) from day one** for default integration tests—no reliance on H2 for CI parity without an explicit divergence ADR.
3. **API style:** **REST** (resource-oriented HTTP, JSON) for **all phases** unless a later ADR revisits a specific boundary (e.g. optional WebFlux for a extracted service).
4. **Phase 1a incident scope:** **Minimum** fields and operations per **`specs/phases/phase-1a-monolith-core/api-contract.md`** and **`data-model.md`** (create / get / list, draft lifecycle, manual-only). No scope creep beyond that gate.
5. **Documentation:** **`specs/`** + **README** remain primary; **this `docs/adr/` set** records kickoff commitments. Further ADRs added when choices materially affect implementation.
6. **OpenAPI:** **In Phase 1**—maintain **`specs/openapi/openapi-1a.yaml`** / **`openapi-1b.yaml`** alongside controllers (see acceptance criteria).
7. **Auth (user-facing API):** **Explicitly out of scope** until a **named later phase** (no session/JWT/OAuth for incident CRUD in 1a). **Phase 1b signal ingest** still uses the **shared token** already specified in **`phase-1b-signal-ingest/api-contract.md`** (that is integration auth, not end-user identity).
8. **Observability in Phase 1a:** **Logs only** (structured logging acceptable); **no** Micrometer tracing baseline in 1a unless a future ADR adds it.
9. **Stretch phases 6–7:** **TBD** against portfolio timeline—treat as **optional** outcomes until explicitly promoted (see roadmap).
10. **Assumptions validated:** **JDK 21** everywhere (CI, Docker, dev). **English-only** API and UI copy for the demo. **No** mandatory integration with real vendors (PagerDuty, Slack, Datadog, etc.) in v1; **OpenTelemetry Demo** is the reference telemetry stack, not a production SLA.

## Consequences

- Scaffold with **`pom.xml`**, Surefire/Failsafe, and Testcontainers dependency for PostgreSQL integration tests.
- README and **`phase-1a-monolith-core/test-plan.md`** describe Testcontainers as the **default** CI path.
- Phase 1a implementation does not block on end-user auth product work.

## Links

- `README.md` — Summary for reviewers (mirrors this ADR).
- `specs/phases/phase-1a-monolith-core/implementation-plan.md`
