# Implementation plan — Phase 1b (after 1a)

## Preconditions

- Phase **1a** gate passed (`../phase-1a-monolith-core/implementation-plan.md`).
- **`V1`** baseline already includes nullable 1b columns + indexes (see **1a** `data-model.md`). If an older deployment omitted them, use a **one-time ADR** (out of scope for greenfield spec).

## Incremental stories (same phase; ship in order)

Aligned with **`docs/adr/0002-phase-1b-webhook-and-incremental-telemetry.md`**:

| Story | Scope |
|-------|--------|
| **1b-M** | **Metrics-first** path: minimal OTel Demo **compose profile** focused on metrics; rules + ingest + fixtures that prove **draft** creation from **metrics** context. |
| **1b-T** | **Traces:** extend **`telemetryPointers`**, rules, and README for **trace**-centric Journey A. |
| **1b-L** | **Logs:** extend rules/docs for **log**-driven evaluation and pointers. |

**Backlog mapping (Phase 1 monolith MVP stories):** **1b-M** → [`../phase-1-monolith-mvp/stories/story-11-1b-signal-ingest-http-evaluations.md`](../phase-1-monolith-mvp/stories/story-11-1b-signal-ingest-http-evaluations.md) (+ supporting **10**, **12**, **13**, **14**, **15**). **1b-T** / **1b-L** → placeholders [`story-16-1b-traces-telemetry-placeholder.md`](../phase-1-monolith-mvp/stories/story-16-1b-traces-telemetry-placeholder.md), [`story-17-1b-logs-telemetry-placeholder.md`](../phase-1-monolith-mvp/stories/story-17-1b-logs-telemetry-placeholder.md) until promoted to active stories.

**1b-M** should reach a demonstrable **201** path before **1b-T** expands scope.

## Milestones (1b)

| Step | Work |
|------|------|
| B1 | Confirm DB matches **`data-model.md`** (columns, partial index, audit if used); **no** `V2` on greenfield |
| B2 | Load **`rules/registry.yaml`**; implement evaluators for **each** shipped `ruleId`; **fail startup** if YAML entry lacks evaluator |
| B3 | `POST /api/v1/signal-ingest/evaluations` + dedup + lock + **`Idempotency-Key`** store + `signals.enabled` (**1b-M** core) |
| B4 | Extend `GET` list/get (`source` default **`MANUAL`**, **`ALL`**) + optional fields |
| B5 | `specs/openapi/openapi-1b.yaml` (ingest + **`GET /api/v1/incidents`** + **`GET /api/v1/incidents/{id}`** per `api-contract.md`) + README **minimal** OTel compose + smoke |
| B6 | **1b gate** — `03-acceptance-criteria` Phase 1b + this `test-plan.md` |
| B7+ | **1b-T** / **1b-L** stories (traces, logs)—extend rules, fixtures, and docs without breaking **1b-M** contracts |

**Out:** **Poll-from-JVM** as the **primary** signal source (webhook **`POST`** is in scope), LLM, MCP.

**Demo narrative:** prefer one **vertical playbook** across stories (see ADR **0002**); document the chosen scenario in README when implementation starts.
