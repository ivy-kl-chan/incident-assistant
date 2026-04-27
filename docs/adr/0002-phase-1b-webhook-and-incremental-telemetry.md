# ADR 0002 — Phase 1b: webhook-style ingest and incremental telemetry delivery

**Status:** Accepted  
**Date:** 2026-04-26  
**Context:** README “should decide” items for OpenTelemetry Demo scope and integration shape.

## Decision

1. **Integration shape:** **Webhook-style** intake—external systems (adapters, demo automation, or future forwarders) call **Incident Assistant’s HTTP ingest** (`POST /api/v1/signal-ingest/evaluations` per spec). **No** first-class **poll-from-JVM** against Jaeger/Grafana as the primary 1b integration (poll remains out of scope per **`phase-1b-signal-ingest/spec.md`** unless a future ADR adds it).
2. **OpenTelemetry Demo footprint:** Prefer a **minimal compose profile** (or documented subset) for local demos and README, with **pinned** images or compose revision.
3. **Incremental delivery within Phase 1b** (each implementable as its own backlog **story**, in order):
   - **1b-M (metrics first):** metrics-driven evaluation path and fixtures; first **Journey A** slice that creates drafts from **metrics** context.
   - **1b-T (traces):** extend rules / **`telemetryPointers`** / docs for **traces** (e.g. trace id, spans) on top of 1b-M.
   - **1b-L (logs):** extend for **logs**-driven signals and pointers after 1b-T.
4. **Demo narrative:** Prefer a **single vertical story** (one coherent playbook, e.g. service degradation) across later phases for RAG/demo—**content** can start lightweight in 1b docs and tighten in Phase 2+.

## Consequences

- **`specs/phases/phase-1b-signal-ingest/implementation-plan.md`** orders work so **metrics** path lands before trace- and log-heavy rules.
- **`specs/02-roadmap.md`** Phase **1b** text references **minimal profile** and **webhook-style** intake explicitly.
- Demo compose README should name the **minimal profile** (or link upstream guidance) once implementation starts.

## Links

- `specs/phases/phase-1b-signal-ingest/spec.md`
- `specs/phases/phase-1b-signal-ingest/api-contract.md`
- `specs/phases/phase-1b-signal-ingest/implementation-plan.md`
