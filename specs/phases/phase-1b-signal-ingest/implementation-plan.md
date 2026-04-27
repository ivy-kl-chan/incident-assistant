# Implementation plan — Phase 1b (after 1a)

## Preconditions

- Phase **1a** gate passed (`../phase-1a-monolith-core/implementation-plan.md`).
- ADR if 1a DB skipped 1b columns (then migration here adds all additive pieces).

## Milestones (1b)

| Step | Work |
|------|------|
| B1 | Flyway `V2` (or next): `created_by_rule_id`, `signal_fingerprint`, `telemetry_context`, audit table optional, indexes |
| B2 | Rule registry load `rules/demo-rule-v1.yaml`; implement `demo.otel.signal_v1` |
| B3 | `POST /api/v1/signal-ingest/evaluations` + dedup + lock + `signals.enabled` |
| B4 | Extend `GET` list/get for `SIGNAL` + optional fields |
| B5 | `specs/openapi/openapi-1b.yaml` + README OTel/compose + smoke |
| B6 | **1b gate** — `03-acceptance-criteria` Phase 1b + this `test-plan.md` |

**Out:** Jaeger client poll from JVM, LLM, MCP.
