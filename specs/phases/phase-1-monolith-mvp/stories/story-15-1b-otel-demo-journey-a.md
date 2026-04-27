# Story 15: Phase 1b — Demo reproduction: observability stack and Journey A

*Label: OTel demo Journey A*

## 1. Status

Planned

## 2. Goal

Satisfy **Phase 1b** acceptance criteria that require the **[OpenTelemetry Demo](https://github.com/open-telemetry/opentelemetry-demo)** (or **approved subset**) to run with **pinned** revisions and a **documented Journey A** from demo signals → **draft** incidents via the webhook **`POST`** path, per **`phase-1b-signal-ingest/spec.md`** and **`docs/adr/0002-phase-1b-webhook-and-incremental-telemetry.md`**.

## 3. User Value

Demo operators can run **end-to-end Journey A** on a fresh machine using repo docs, validating **1b** beyond fixtures-only CI.

## 4. Spec References

| Document | Relevance |
|----------|-----------|
| [`../../../03-acceptance-criteria.md`](../../../03-acceptance-criteria.md) | Phase **1b** DoD: OTel Demo, compose, Journey A, dedup, rules |
| [`../../phase-1b-signal-ingest/spec.md`](../../phase-1b-signal-ingest/spec.md) | Purpose, incremental telemetry order |
| [`../../phase-1b-signal-ingest/implementation-plan.md`](../../phase-1b-signal-ingest/implementation-plan.md) | B5 README minimal compose + smoke; **1b-M/T/L** ordering |
| [`../../phase-1b-signal-ingest/api-contract.md`](../../phase-1b-signal-ingest/api-contract.md) | Ingest auth, **`signals.enabled`**, validation |
| [`docs/adr/0002-phase-1b-webhook-and-incremental-telemetry.md`](../../../../docs/adr/0002-phase-1b-webhook-and-incremental-telemetry.md) | Compose profile preference, webhook shape |

## 5. In Scope

- Documented **minimal compose profile** (or two compose files with explicit networking and URLs) so **Journey A** is reproducible per **`03-acceptance-criteria.md`** § Phase **1b**.
- **Pinned** image tags or compose revision reference in README (and optional `docs/` pointer) per **1b** DoD.
- README section: **how to start** observability demo stack **+** Incident Assistant, **smoke checks**, and **resource expectations**.
- Wire-up narrative for **metrics-first** (**1b-M**) at minimum; **traces** (**1b-T**) and **logs** (**1b-L**) documented per **`implementation-plan.md`** when those milestones are in scope—**Stories 16–17** own implementation; this story’s README may stub honest “not yet proven” notes until they land.

## 6. Out of Scope

- **New** rule IDs or ingest contract changes beyond Stories **10–13** (implementation already landed there).
- **LLM**, **RAG**, **MCP** narrative or tooling.
- **Kubernetes** deployment of the demo or app (**Phase 7** optional path).
- **Microservices** extraction or split compose ownership across repos.
- Replacing **default CI** with mandatory full-stack demo runs (fixtures remain default per **`test-plan.md`**).

## 7. API Changes

None (uses **`POST /api/v1/signal-ingest/evaluations`** and read APIs from prior stories).

## 8. Data Model Changes

None (schema already from Stories **2**, **11**, **12**).

## 9. Business Rules

- Demo instructions must state **`SIGNAL_INGEST_TOKEN`** (or agreed header) and **`signals.enabled=true`** requirements without leaking real secrets into the repo.
- **Journey A** steps must match **dedup Option A** expectations so operators are not surprised by **200 DUPLICATE_SIGNAL** vs **201** (**`data-model.md`**).

## 10. Acceptance Criteria

- [ ] **`specs/03-acceptance-criteria.md`** Phase **1b** bullets for **OpenTelemetry Demo**, **compose story**, and **README** updates are satisfied.
- [ ] A reviewer on a **fresh machine** can follow README and observe at least one **201** draft creation from a **documented** demo-driven evaluation path (**metrics** path minimum per **1b-M**).
- [ ] **Pinned** versions documented; **resource expectations** explicit.
- [ ] **Pluggable rules** and **registry** behavior remain as implemented in Story **10** (no undocumented rule ids).

## 11. Test Requirements

- **Default CI** remains aligned with **`phase-1b-signal-ingest/test-plan.md`** (fixtures / in-process doubles); full-stack steps are **manual** or **optional job** if documented.
- If an **optional** CI or script is added, it must be **opt-in** and not block PRs.

## 12. Files Expected to Change

- **`README.md`**, **`docker-compose*.yml`** (or overlay files), optional **`docs/`** demo playbook, optional **`.env.example`** fragments (no real secrets).

## 13. Implementation Notes

- **Depends on:** Stories **10–13** (ingest + read + OpenAPI **1b**); coordinate with Story **8** so **1a** base compose and **1b** demo overlays do not conflict.
- **Design note:** incremental telemetry (**metrics → traces → logs**) may ship across multiple releases; keep README honest about which pointers are proven in CI vs manual only; link **Stories 16–17** when deferred.

## 14. Human Review Checklist

- [ ] Legal/license OK for pinned demo images.
- [ ] Machine requirements (CPU/RAM/disk) stated for the chosen profile.
- [ ] Network ports documented; no collisions with **Story 8** defaults.

## 15. Completion Notes

*(Fill when implemented: compose revision, profile names, PR link, any deferred **1b-T**/**1b-L** items with issue links to Stories **16–17**.)*
