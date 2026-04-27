# Implementation plan — Phase 1b (after 1a)

## Preconditions

- Phase **1a** gate passed (`../phase-1a-monolith-core/implementation-plan.md`).
- **`V1`** baseline already includes nullable 1b columns + indexes (see **1a** `data-model.md`). If an older deployment omitted them, use a **one-time ADR** (out of scope for greenfield spec).

## Milestones (1b)

| Step | Work |
|------|------|
| B1 | Confirm DB matches **`data-model.md`** (columns, partial index, audit if used); **no** `V2` on greenfield |
| B2 | Load **`rules/registry.yaml`**; implement evaluators for **each** shipped `ruleId` |
| B3 | `POST /api/v1/signal-ingest/evaluations` + dedup + lock + `signals.enabled` |
| B4 | Extend `GET` list/get for `SIGNAL` + optional fields |
| B5 | `specs/openapi/openapi-1b.yaml` + README OTel/compose + smoke |
| B6 | **1b gate** — `03-acceptance-criteria` Phase 1b + this `test-plan.md` |

**Out:** Jaeger client poll from JVM, LLM, MCP.
