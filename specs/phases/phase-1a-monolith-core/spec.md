# Phase 1a — Monolith core + containers baseline

**Authoritative** detailed spec for **Roadmap Phase 1a** only (`specs/02-roadmap.md`). **High-level** DoD: `specs/03-acceptance-criteria.md` § Phase 1a. **Next phase** (OTel, ingest): `../phase-1b-signal-ingest/` — do **not** implement before 1a is complete.

## Purpose

- **Spring Boot 3 / Java 21** monolith: **incidents** (manual create/edit), **draft → open → closed / cancelled** lifecycle, **versioned JSON API** + **OpenAPI**, **PostgreSQL** + **Docker Compose** (app + DB only).
- **No** OpenTelemetry Demo dependency in default CI, **no** LLM, **no** RAG, **no** signal ingest **route** (controller not registered; path does not exist). **404** on ingest when `signals.enabled=false` is **Phase 1b** only.

## In scope (1a)

| Area | Spec |
|------|------|
| HTTP incidents API | `api-contract.md` |
| Persistence (manual-only rows) | `data-model.md` |
| OpenAPI (1a only) | `../../openapi/openapi-1a.yaml` |
| Tests | `test-plan.md` |
| Milestones | `implementation-plan.md` |
| No AI / tools | `ai-behavior.md` |

## Out of scope (1a) — do not implement here

- **`POST /api/v1/signal-ingest/*`** and **rule engine** (Phase **1b**).
- **Using** `signal_fingerprint`, `created_by_rule_id`, `telemetry_context`, or **`SIGNAL`** rows in the API — **1b**. The **physical** DB may still include **nullable** columns from the shared **`V1`** baseline (see `data-model.md`); **1a** logic ignores them.

## Canonical decisions (1a — ADR to override)

| Topic | Decision |
|-------|----------|
| Build | **Maven** (`docs/adr/0001-…`) |
| State machine | `DRAFT` → `OPEN` → `CLOSED`; `CANCELLED` from `DRAFT` or `OPEN`; no `OPEN` from `CLOSED` in v1 |
| Create | `POST /incidents` → **`DRAFT` only** (no `OPEN` on create) |
| API | **REST** / JSON HTTP for this repo’s phases unless a later ADR revisits |
| Concurrency | `ETag` + **`If-Match`** on `PATCH` and `transitions` |
| DB | **PostgreSQL**; **Testcontainers from day one** for default CI integration tests; **H2** local only with divergence doc (**not** normative for **Phase 1b**—see **`../phase-1b-signal-ingest/api-contract.md`**) |
| Docker CI | **Policy B** default (`test-plan.md`) |
| OpenAPI | `specs/openapi/openapi-1a.yaml` **required** at 1a complete (maintain with controllers) |
| Observability (**1a**) | **Logs only**—no tracing baseline in **1a** (`docs/adr/0001-…`) |
| End-user auth | **Out of scope** until a named later phase (`docs/adr/0001-…`) |
| Locale | **English-only** API/UI copy for demo (`docs/adr/0001-…`) |

## Document index

| File | Role |
|------|------|
| `api-contract.md` | Incidents + Actuator; **1a-only** request/response shapes |
| `data-model.md` | `incidents` physical **`V1`** shape (1a use + null 1b columns) |
| `test-plan.md` | Unit + integration for incidents only |
| `implementation-plan.md` | M1–M4 / gate 1a |
| `review-notes.md` | 1a review notes |
| `ai-behavior.md` | No generative AI in 1a |

## Dependency

- **Phase 0** specs signed off. **No** dependency on `phase-1b`.
