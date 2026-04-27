# Architecture Decision Records (ADRs)

Human-reviewed decisions that are easy to lose in chat or README-only bullets live here. **Normative behavior** for the product remains in **`specs/`**; ADRs capture **why** and **commitments** that drive repo layout and process.

| ADR | Summary |
|-----|---------|
| [0001 — Kickoff: tooling, API, testing, 1a scope](0001-kickoff-tooling-testing-and-1a-scope.md) | Maven, Testcontainers from day one, REST, OpenAPI in Phase 1, logs-only observability in 1a, auth deferral, JDK 21, English-only, vendor assumptions |
| [0002 — Phase 1b: webhook ingest and incremental telemetry](0002-phase-1b-webhook-and-incremental-telemetry.md) | Webhook-style HTTP ingest, minimal OTel Demo compose, delivery order metrics → traces → logs, single vertical demo narrative |

When an ADR conflicts with older README prose, **the ADR and updated `specs/` win** after the README is refreshed.
