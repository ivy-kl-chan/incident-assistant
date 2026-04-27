# Acceptance criteria and definition of done — Incident Assistant

This document defines **definition of done (DoD)** per roadmap phase and the **testing strategy** that applies across phases unless overridden.

## Global testing strategy

| Layer | Purpose |
|-------|---------|
| **Unit tests** | Domain logic, validators, prompt builders, tool schema validation, pure functions. |
| **Integration tests** | Spring context with test slices or `@SpringBootTest` where appropriate; persistence; HTTP endpoints with MockMvc or WebTestClient. |
| **Contract-style tests** | HTTP/OpenAPI contracts once introduced; client/server tests if services split (Phase 6+). |
| **AI-specific tests** | **Always** use **mock or stub `LlmClient`** in CI; assert structure (fields, citations, tool call records), not stochastic prose. |

**Cross-cutting requirements**

- CI runs **without** real external LLM keys by default.
- **No flaky** network-dependent tests in default pipeline (wire mocks or stubs for any HTTP client).
- **Coverage targets:** not mandated numerically for demo; **meaningful** tests for each new behavior (no empty tests).

**LLM mockability (mandatory from Phase 2 onward)**

- All LLM access goes through a **single injectable interface**; tests never call vendor SDKs directly from domain services.
- Golden tests may compare **normalized** structured output (e.g. JSON fields) with fixed seeds where applicable.

---

## Phase 0 — Specifications

### Definition of done

- [ ] `specs/00-product-vision.md` exists and states problem, users, journeys, non-goals.
- [ ] `specs/01-architecture.md` exists with monolith-first view and future service boundaries.
- [ ] `specs/02-roadmap.md` exists with ordered phases and dependencies.
- [ ] `specs/03-acceptance-criteria.md` (this file) defines DoD and testing for each phase.
- [ ] `README.md` explains purpose, how to read specs, and honest limitations.
- [ ] **Human reviewer** has signed off on open questions (see README “Before Phase 1a” checklist).

### Tests

- N/A (no application code).

---

## Phase 1a — Monolith core + containers baseline

**Normative detail (1a only):** [`phases/phase-1a-monolith-core/`](phases/phase-1a-monolith-core/spec.md) (`api-contract.md`, `data-model.md`, `test-plan.md`, `implementation-plan.md`).

### Definition of done

- [ ] Java 21 and Spring Boot 3 application builds and runs locally **and via Docker** with documented commands.
- [ ] Incident APIs implemented per updated phase spec/ADR (create/read/list minimum) including **draft** incidents and **documented state transitions** (e.g. promote/edit draft); **manual** incident/draft flows work end-to-end.
- [ ] Data persisted to configured database; schema managed by agreed approach (e.g. Flyway).
- [ ] **Dockerfile** produces a runnable image; **`docker compose`** (or equivalent) starts **app + database** with documented ports, env vars, and health checks.
- [ ] **No** automated **signals → draft** path required in this sub-phase; **no** LLM, RAG, or MCP tool code paths in production classpath (or feature-flagged off entirely if stubs exist).
- [ ] Health (and readiness if applicable) endpoints documented.
- [ ] `README` updated: prerequisites, **bare-metal and container** run paths, test commands.
- [ ] **`specs/openapi/openapi-1a.yaml`** present and **aligned** with `specs/phases/phase-1a-monolith-core/api-contract.md` (paths, methods, `If-Match`/`ETag` expectations).
- [ ] **Optimistic concurrency:** `GET /api/v1/incidents/{id}` returns **`ETag`**; **`PATCH`** and **`POST .../transitions`** require **`If-Match`** and return **`412`** when stale.
- [ ] **`POST /api/v1/incidents`** only creates **`DRAFT`** (direct **`OPEN`** on create **forbidden**).
- [ ] **Signal ingest in 1a:** **`POST /api/v1/signal-ingest/*`** is **not registered** (no route). **`signals.enabled` / ingest `404`** behavior is **Phase 1b** (`phase-1b-signal-ingest/api-contract.md`).

### Tests

- [ ] Unit tests for domain validation and **draft lifecycle**.
- [ ] Integration tests hitting real persistence (Testcontainers or equivalent **recommended**; H2-only acceptable if limitations documented).
- [ ] API integration tests for happy path and representative error cases (**400**, **404**, **409**, **412**, **413**/**422** as specified in `specs/phases/phase-1a-monolith-core/api-contract.md`).

---

## Phase 1b — Signals → draft (OpenTelemetry Demo in Docker)

**Normative detail (1b only):** [`phases/phase-1b-signal-ingest/`](phases/phase-1b-signal-ingest/spec.md).

### Definition of done

- [ ] **[OpenTelemetry Demo](https://github.com/open-telemetry/opentelemetry-demo)** (or **approved subset**) runs via **Docker** with a **pinned** compose revision or image tags documented in README.
- [ ] **Journey A:** Documented integration from demo signals to **draft** incident creation when **abnormality rules** match; drafts include **telemetry references** (trace id, service, time window—minimum set per ADR).
- [ ] **Dedup or cooldown** policy for repeated signals documented and implemented or explicitly deferred with issue link (avoid unbounded duplicate drafts).
- [ ] **Compose story:** single merged compose **or** two compose files with explicit networking and URLs so a reviewer can reproduce Journey A on a fresh machine.
- [ ] **No** LLM, RAG, or MCP tool code paths in production classpath for this sub-phase (or feature-flagged off entirely if stubs exist).
- [ ] `README` updated: **how to start OTel Demo + app**, smoke checks, and resource expectations.
- [ ] **Pluggable rules:** **`specs/phases/phase-1b-signal-ingest/rules/registry.yaml`** loaded at startup; at minimum **`demo.otel.signal_v1`** and **`demo.stub.always_false_v1`** implemented per registry + `api-contract.md`. **`ruleId`** not in registry → **`400`**.
- [ ] **Dedup:** behavior matches **`specs/phases/phase-1b-signal-ingest/data-model.md`** (**Option A** default) including **`200 DUPLICATE_SIGNAL`** vs **`201`** matrix; **PostgreSQL advisory lock** (or equivalent) prevents double-create under concurrency **where DB supports it**.
- [ ] **`specs/openapi/openapi-1b.yaml`** present and aligned with **1b** ingest + incident **extensions** (merge with `openapi-1a.yaml` for a full spec if desired).
- [ ] **Ingest responses:** **`200 { "matched": false }`** when rule does not match; **constant-time** token compare implemented.
- [ ] **Validation:** `observedAt` clock window, nested JSON limits, and **`ruleId`** pattern enforced per `specs/phases/phase-1b-signal-ingest/api-contract.md`.

### Tests

- [ ] Unit tests for **rule** evaluation on **fixture** inputs (including **unknown `ruleId`** → **`400`** and stub rule path).
- [ ] **Signals path:** default CI uses **in-memory doubles or recorded fixtures**—no mandatory dependency on the full OTel Demo stack in the default pipeline; optional manual or nightly job documented if full-stack tests are added later.
- [ ] Integration tests for ingest: **401**, **404** (disabled), **200** not matched, **200** dedup, **201** create, **400** unknown `ruleId`, **415** wrong `Content-Type`, **422** bad telemetry/clock, **500** rule throw (test double), **concurrency** same-fingerprint (PostgreSQL).

---

## Phase 2 — LLM-assisted analysis

### Definition of done

- [ ] `LlmClient` (or agreed name) interface; **mock** implementation used by default in tests and CI.
- [ ] Optional real provider behind explicit config; **documented** env vars; never required for CI.
- [ ] Assistant API returns **structured** sections: facts, assumptions, recommendations (exact schema documented).
- [ ] Timeouts, max input size, and max output tokens enforced at application boundary.
- [ ] Specs/README updated for behavior and configuration.

### Tests

- [ ] Unit tests for prompt assembly and response parsing (pure logic).
- [ ] Integration tests with mock LLM returning fixed strings/JSON; assert structure and error handling (timeouts, provider errors).

---

## Phase 3 — RAG

### Definition of done

- [ ] Document ingestion path for demo corpus (files in repo or seed script—documented).
- [ ] Retrieval integrated into assistant flow **only** when enabled; feature flag acceptable.
- [ ] **Every** response that used retrieval includes **citations** (source id + locator); API contract enforces or validates this.
- [ ] Explicit behavior when retrieval returns nothing useful (no fabricated citations).

### Tests

- [ ] Integration tests with **small fixed corpus** and stubbed embeddings if needed for CI determinism.
- [ ] Assertions on citation presence and correct source reference for at least one golden query.

---

## Phase 4 — MCP-style tools

### Definition of done

- [ ] Tool registry lists all tools with schemas; unknown tool names rejected.
- [ ] Dispatcher logs invocations with correlation id (no secrets).
- [ ] All demo tools are **non-destructive** (read/compute only); documented list.
- [ ] Assistant integration: tool use is **explicit** (no silent side effects).

### Tests

- [ ] Unit tests per tool and for schema validation failures.
- [ ] Integration test: simulated “LLM wants tool X” path invokes handler and records result.

---

## Phase 5 — Container & demo packaging polish

### Definition of done

- [ ] **Image hardening:** Multi-stage or optimized `Dockerfile` where justified; security basics (e.g. non-root) documented if applied.
- [ ] **Compose profiles** include **post-RAG** dependencies when Phase 3 delivered (e.g. vector DB)—or explicitly document “not in compose” with rationale.
- [ ] README **golden path** documents full journeys **A–D** in containers (or subset with honest limits).
- [ ] Optional: CI workflow builds/pushes image (nightly vs PR—document choice).

### Tests

- [ ] Optional: CI job that builds image (may be nightly if slow); document if not in default PR pipeline.
- [ ] Smoke checklist or automated smoke for **compose full profile** documented for maintainers.

---

## Phase 6 — Microservices *(stretch)*

### Definition of done

- [ ] Extracted service runs independently with its own config and health checks.
- [ ] OpenAPI (or equivalent) published and versioned; contract tests pass against both sides.
- [ ] Monolith/BFF documentation updated with sequence diagrams or clear narrative.

### Tests

- [ ] Contract tests; smoke tests for compose/K8s if applicable.

---

## Phase 7 — Kubernetes *(stretch)*

### Definition of done

- [ ] Manifests apply to local cluster without cloud-specific operators for **Incident Assistant** and its **Phase-5-aligned** dependencies.
- [ ] README section: prerequisites, `kubectl`/`helm` commands, troubleshooting.
- [ ] Secrets and config documented; no keys in repo.
- [ ] **Optional OTel Demo on Kubernetes:** README (and manifests or links **if** checked in) describes how to run **OpenTelemetry Demo** (or the **same subset** as Phase 1b) **on-cluster** for an end-to-end **Journey A** demo; **resource requirements** and **optional** nature of this path are explicit.

### Tests

- [ ] Scripted smoke test or checklist run manually before tagging a demo release (document expectation).
- [ ] If OTel-on-K8s path is documented: manual or scripted smoke steps for **one** happy-path signal → draft (or explicitly “bring your own cluster resources”).

---

## AI safety — acceptance checklist (from Phase 2 onward)

Applies to every release that includes LLM/RAG/tools:

- [ ] No pathway for **automatic production remediation** in demo scope.
- [ ] User-facing responses distinguish **fact / assumption / recommendation** where LLM is involved.
- [ ] RAG responses **cite** sources when retrieval is used.
- [ ] Tools are **allowlisted** and **schema-validated**; invocations auditable in logs.

---

## RAG — acceptance checklist (Phase 3+)

- [ ] Citations point to real ingested artifacts in demo corpus.
- [ ] “Insufficient evidence” path tested and user-visible.

---

## MCP-style tools — acceptance checklist (Phase 4+)

- [ ] Tool list and schemas documented in README or `specs/`.
- [ ] Unknown tool → controlled error.
- [ ] Tests do not require external MCP servers unless explicitly opt-in.
