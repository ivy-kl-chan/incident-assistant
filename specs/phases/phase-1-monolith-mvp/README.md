# Moved: Phase 1a / 1b specs

Detailed specifications were **split** so **1a** and **1b** requirements are not mixed:

| Phase | Location |
|-------|----------|
| **1a** — monolith + containers + **manual** incidents | [`../phase-1a-monolith-core/`](../phase-1a-monolith-core/spec.md) |
| **1b** — **signal ingest** + OpenTelemetry Demo | [`../phase-1b-signal-ingest/`](../phase-1b-signal-ingest/spec.md) |

**Implement 1a first**, then 1b.

Implementation stories (numbered in **implementation order**): Markdown files in [`stories/`](stories/) (`story-*.md`).

**CI prerequisite:** Merge **[Story 18 — PR CI with Docker for Testcontainers](stories/story-18-1a-pr-ci-docker-for-testcontainers.md)** before treating the **[Story 9](stories/story-9-1a-gate-readiness-no-ingest-route.md)** “green default CI with Testcontainers” criterion as satisfied (file number **18** reflects insert after the original **1–17** breakdown, not late-phase scope).

The previous combined files in this folder were removed to avoid duplicate sources of truth.
