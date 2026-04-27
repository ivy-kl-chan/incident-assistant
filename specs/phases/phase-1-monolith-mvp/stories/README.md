# Phase 1 monolith MVP — implementation stories

Normative specs live under [`../phase-1a-monolith-core/`](../phase-1a-monolith-core/spec.md) and [`../phase-1b-signal-ingest/`](../phase-1b-signal-ingest/spec.md). **Implement Phase 1a stories (1–9) before Phase 1b (10–17).** Story **numbers** match **implementation order**, not historical file order.

| # | File | Sub-phase | Label | Notes |
|---|------|-----------|-------|--------|
| 1 | [`story-1-1a-app-scaffold-actuator.md`](story-1-1a-app-scaffold-actuator.md) | **1a** | Scaffold & Actuator | M1 shell |
| 2 | [`story-2-1a-flyway-baseline-schema.md`](story-2-1a-flyway-baseline-schema.md) | **1a** (+ reserved **1b** DDL) | Shared schema baseline | Single `V1` migration |
| 3 | [`story-3-1a-incident-domain-repository.md`](story-3-1a-incident-domain-repository.md) | **1a** | Domain & persistence | Manual rows only |
| 4 | [`story-4-1a-incidents-post-get-list.md`](story-4-1a-incidents-post-get-list.md) | **1a** | Incidents HTTP (create/list/get) | |
| 5 | [`story-5-1a-incidents-patch-etag-concurrency.md`](story-5-1a-incidents-patch-etag-concurrency.md) | **1a** | Optimistic concurrency | |
| 6 | [`story-6-1a-incident-lifecycle-transitions.md`](story-6-1a-incident-lifecycle-transitions.md) | **1a** | Lifecycle transitions | |
| 7 | [`story-7-1a-problem-details-openapi.md`](story-7-1a-problem-details-openapi.md) | **1a** | Errors & OpenAPI 1a | |
| 8 | [`story-8-1a-container-packaging.md`](story-8-1a-container-packaging.md) | **1a** | Container packaging | Dockerfile + compose (M5) |
| 9 | [`story-9-1a-gate-readiness-no-ingest-route.md`](story-9-1a-gate-readiness-no-ingest-route.md) | **1a** | 1a integration gate | Readiness, docs, **no** ingest route (M6) |
| 10 | [`story-10-1b-rule-registry-and-evaluators.md`](story-10-1b-rule-registry-and-evaluators.md) | **1b** | Rule registry | |
| 11 | [`story-11-1b-signal-ingest-http-evaluations.md`](story-11-1b-signal-ingest-http-evaluations.md) | **1b** | Ingest HTTP & dedup | **1b-M** (metrics-first) core |
| 12 | [`story-12-1b-ingest-idempotency-and-audit.md`](story-12-1b-ingest-idempotency-and-audit.md) | **1b** | Idempotency & audit | |
| 13 | [`story-13-1b-incident-read-extensions-openapi.md`](story-13-1b-incident-read-extensions-openapi.md) | **1b** | Read extensions & OpenAPI 1b | |
| 14 | [`story-14-1b-test-matrix-hardening.md`](story-14-1b-test-matrix-hardening.md) | **1b** | Test matrix | **1b** automated gate |
| 15 | [`story-15-1b-otel-demo-journey-a.md`](story-15-1b-otel-demo-journey-a.md) | **1b** | OTel demo Journey A | Manual / compose demo |
| 16 | [`story-16-1b-traces-telemetry-placeholder.md`](story-16-1b-traces-telemetry-placeholder.md) | **1b** | Placeholder **1b-T** | Traces backlog slot |
| 17 | [`story-17-1b-logs-telemetry-placeholder.md`](story-17-1b-logs-telemetry-placeholder.md) | **1b** | Placeholder **1b-L** | Logs backlog slot |

**Phase 1a definition of done** (`specs/03-acceptance-criteria.md` § Phase 1a) requires **Stories 1–9** together (including **Story 8** image/compose and **Story 9** readiness / no ingest). **Phase 1b** starts at **Story 10**.

**Parallelism:** Story **8** may proceed in parallel with **Story 9** once Stories **1–7** are stable; coordinate README edits.

See [`../README.md`](../README.md) for links to split phase specs.
