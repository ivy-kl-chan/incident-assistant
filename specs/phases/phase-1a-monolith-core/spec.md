# Phase 1a — Monolith core + containers baseline

**Authoritative** detailed spec for **Roadmap Phase 1a** only (`specs/02-roadmap.md`). **High-level** DoD: `specs/03-acceptance-criteria.md` § Phase 1a. **Next phase** (OTel, ingest): `../phase-1b-signal-ingest/` — do **not** implement before 1a is complete.

## Purpose

- **Spring Boot 3 / Java 21** monolith: **incidents** (manual create/edit), **draft → open → closed / cancelled** lifecycle, **versioned JSON API** + **OpenAPI**, **PostgreSQL** + **Docker Compose** (app + DB only).
- **No** OpenTelemetry Demo dependency in default CI, **no** LLM, **no** RAG, **no** signal ingest **endpoint** (may ship **stub Spring profile** with route **unregistered** or **404**—see `api-contract.md`).

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
- **Columns / tables** for `signal_fingerprint`, `created_by_rule_id`, `telemetry_context` **unless** you choose a single migration for both phases (see `implementation-plan.md` note; **normative 1a model** in `data-model.md` is **without** those fields in the *logical* 1a deliverable; DB may pre-add null columns only if documented as “reserved for 1b” in ADR).

## Canonical decisions (1a — ADR to override)

| Topic | Decision |
|-------|----------|
| State machine | `DRAFT` → `OPEN` → `CLOSED`; `CANCELLED` from `DRAFT` or `OPEN`; no `OPEN` from `CLOSED` in v1 |
| Create | `POST /incidents` → **`DRAFT` only** (no `OPEN` on create) |
| Concurrency | `ETag` + **`If-Match`** on `PATCH` and `transitions` |
| DB | **PostgreSQL**; Testcontainers in CI; H2 local only with divergence doc |
| Docker CI | **Policy B** default (`test-plan.md`) |
| OpenAPI | `specs/openapi/openapi-1a.yaml` **required** at 1a complete |

## Document index

| File | Role |
|------|------|
| `api-contract.md` | Incidents + Actuator; **1a-only** request/response shapes |
| `data-model.md` | `incidents` table **without** signal-specific columns in logical model |
| `test-plan.md` | Unit + integration for incidents only |
| `implementation-plan.md` | M1–M4 / gate 1a |
| `review-notes.md` | 1a review notes |
| `ai-behavior.md` | No generative AI in 1a |

## Dependency

- **Phase 0** specs signed off. **No** dependency on `phase-1b`.
