# Story 9 — Rule registry loading and pluggable evaluators

## 1. Status

Planned

## 2. Goal

Load **`specs/phases/phase-1b-signal-ingest/rules/registry.yaml`**, bind every registry **`id`** to a code evaluator at startup (**fail-fast** if missing), and implement **normative** match behavior for **`demo.otel.signal_v1`** and **`demo.stub.always_false_v1`** per **1b** `api-contract.md`.

## 3. User Value

Signal evaluation is deterministic, testable, and extensible: unknown **`ruleId`** can be rejected before any persistence work.

## 4. Spec References

| Document | Relevance |
|----------|-----------|
| [`../README.md`](../README.md) | **1b** after **1a** |
| [`../../phase-1b-signal-ingest/spec.md`](../../phase-1b-signal-ingest/spec.md) | Pluggable **`ruleId`**; registry vs code |
| [`../../phase-1b-signal-ingest/api-contract.md`](../../phase-1b-signal-ingest/api-contract.md) | Normative match bullets; unknown id **400**; startup binding |
| [`../../phase-1b-signal-ingest/rules/registry.yaml`](../../phase-1b-signal-ingest/rules/registry.yaml) | Shipped ids and metadata (**`matchSemantics`** informative only) |
| [`../../phase-1b-signal-ingest/test-plan.md`](../../phase-1b-signal-ingest/test-plan.md) | Unit tests for rules + startup misconfiguration |

## 5. In Scope

- Classpath or filesystem load of **`registry.yaml`** (team choice; must ship with artifact).
- Spring **`@PostConstruct`** / **`ApplicationRunner`** / equivalent validation: **every** YAML **`id`** has evaluator bean or registry entry map.
- Implement **`demo.otel.signal_v1`**: matches if **`fingerprintInputs.match`** is boolean **`true`** OR **`fingerprintInputs.metric`** equals **`demo.synthetic`**; else not matched.
- Implement **`demo.stub.always_false_v1`**: never matches in reference implementation.
- Unit tests for match / no-match / always-false.

## 6. Out of Scope

- HTTP **`POST /api/v1/signal-ingest/evaluations`** (Story **10–12**).
- **Deduplication**, **fingerprint**, **advisory locks** (Story **11**).
- **OpenAPI 1b**.
- **AI**, **RAG**, **MCP**, **Docker**, **OpenTelemetry Demo**, **Kubernetes**, **microservices**.

## 7. API Changes

None (library/module behavior only until HTTP story wires it).

## 8. Data Model Changes

None.

## 9. Business Rules

- **`matchSemantics`** in YAML must not contradict normative bullets in **`api-contract.md`**; code follows **API doc**, not YAML prose, if ever in conflict.

## 10. Acceptance Criteria

- [ ] Application fails to start if a YAML **`id`** lacks an evaluator binding (test with temporary broken fixture in test module, not committed production YAML).
- [ ] **`demo.otel.signal_v1`** matches only per normative rules; otherwise evaluation yields “not matched”.
- [ ] **`demo.stub.always_false_v1`** never matches.
- [ ] Unknown **`ruleId`** at runtime is classified for HTTP layer as **400** (covered again in Story **10** integration tests).

## 11. Test Requirements

- Unit tests per **1b** `test-plan.md` registry/rule section.
- Startup test hook or slice test verifying fail-fast wiring.

## 12. Files Expected to Change

- **`src/main/java/**`** rule module (loader, evaluator interface, implementations), **`src/test/java/**`**, possibly **`src/test/resources/`** broken registry for negative startup test.

## 13. Implementation Notes

- Keep **`SignalEvaluationEvaluator`** (or agreed name) behind an interface for future **`SignalIngestPort`** extraction (**1b** `data-model.md` note)—design only, no separate service.

## 14. Human Review Checklist

- [ ] Normative bullets in **`api-contract.md`** satisfied exactly.
- [ ] Fail-fast behavior is acceptable ops-wise (crash on bad deploy).

## 15. Completion Notes

*(Fill when implemented.)*
