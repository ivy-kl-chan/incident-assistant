# Phase 1b — Signal → draft (OpenTelemetry Demo in Docker)

**Authoritative** detailed spec for **Roadmap Phase 1b** only. Depends on **Phase 1a** complete: `../phase-1a-monolith-core/`.

**High-level** DoD: `specs/03-acceptance-criteria.md` § Phase 1b.

## Purpose

- **Observability-driven draft** incidents from **[OpenTelemetry Demo](https://github.com/open-telemetry/opentelemetry-demo)** (Docker), **rule-based** evaluation, **deduplication**, **telemetry pointers** on incidents.
- **No** LLM, **no** RAG. **Ingest** is HTTP + **shared token**; CI uses **fixtures**, not full OTel in default PRs.

## Prerequisites

- `../phase-1a-monolith-core/` **implementation-plan** “1a gate” passed.
- DB uses the shared **`V1`** baseline (nullable 1b columns) per `data-model.md`; no extra versioned migration required for schema on greenfield.

## In scope (1b)

| Area | Spec |
|------|------|
| Signal HTTP API + incident read **extensions** | `api-contract.md` |
| Signal persistence, dedup, audit | `data-model.md` |
| Rule registry | `rules/registry.yaml` |
| OpenAPI (ingest) | `../../openapi/openapi-1b.yaml` |
| Tests | `test-plan.md` |
| Milestones | `implementation-plan.md` |

## Canonical decisions (1b)

| Topic | See |
|-------|-----|
| Dedup **Option A** / matrix | `data-model.md` |
| Ingest auth | `api-contract.md` |
| `signals.enabled=false` → **404** on ingest (when 1b code present but off) | `api-contract.md` |
| Pluggable `ruleId` | All ids in **`rules/registry.yaml`** (minimum **`demo.otel.signal_v1`**, **`demo.stub.always_false_v1`**); unknown id → **`400`** |

## Out of scope (1b)

- LLM narrative on draft (**Phase 2**).
- Outbound JVM calls to Jaeger (poll mode) — ADR if added later.
- mTLS for ingest in v1.

## Document index

| File | Role |
|------|------|
| `api-contract.md` | Ingest + **extensions** to 1a incident list/get |
| `data-model.md` | Additive schema + dedup |
| `rules/registry.yaml` | Shipped rule ids + metadata + documented match semantics |
| `test-plan.md` | 1b tests only |
| `implementation-plan.md` | 1b milestones after 1a |
| `review-notes.md` | 1b review notes |
| `ai-behavior.md` | No LLM; rules only |
