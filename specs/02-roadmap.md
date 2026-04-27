# Roadmap — Incident Assistant

## Guiding constraints

- **Monolith first:** Phases **1a** and **1b** deliver a single Spring Boot application with clear internal modules; **1b** adds observability-driven intake using **OpenTelemetry Demo in Docker**.
- **Containers early:** **Dockerfile** and **`docker compose`** for the app and database land in **Phase 1a**; **1b** extends compose (or documented multi-compose) to include the **OTel Demo** stack for manual demos. Later **Phase 5** is **packaging polish** (not the first time the app runs in Docker).
- **Specs before code:** each phase begins only after specs and acceptance criteria are updated for that phase’s scope.
- **Fully tested phases:** no phase is “closed” until its acceptance criteria and test strategy are met.
- **Safe AI:** structured outputs, citations for RAG, explicit tools, mockable LLMs—**no automatic production remediation** in early phases.

## Phase overview

| Phase | Name | Focus |
|-------|------|--------|
| **0** | Specifications | Product vision, architecture, roadmap, acceptance criteria, README. |
| **1a** | Monolith core + containers baseline | Spring Boot skeleton, incident APIs, **draft lifecycle**, persistence, tests; **Dockerfile** + **`docker compose`** (app + database only)—**no OTel signals path**, **no LLM/RAG/tools**. |
| **1b** | Signals → draft (OTel Demo in Docker) | **[OpenTelemetry Demo](https://github.com/open-telemetry/opentelemetry-demo)** runs via **Docker** (full or **documented minimal profile**); **abnormality rules** → **`draft`** incidents with **telemetry pointers**; dedup/cooldown; CI uses **doubles/fixtures**—still **no LLM/RAG/tools**. |
| **2** | LLM-assisted analysis | `LlmClient` abstraction, structured assistant responses, safety limits. |
| **3** | RAG | Document store, retrieval, **answers with citations**. |
| **4** | MCP-style tools | Tool registry, schemas, dispatcher, audit logging—**safe, non-destructive** demo tools. |
| **5** | Container & demo packaging polish | Multi-stage image **hardening**, compose profiles that add **post-RAG** deps (e.g. vector DB) when present, optional **CI image build**, README “golden path” refresh—**app is already containerized** from 1a. |
| **6** | Microservices (optional demo) | Split **one** boundary (e.g. assistant+RAG) behind HTTP with contract tests. |
| **7** | Kubernetes (optional demo) | Manifests for **local** cluster (e.g. kind, minikube); **optional** docs/manifests to run **OpenTelemetry Demo** (or **approved subset**) on-cluster **alongside** Incident Assistant for an end-to-end telemetry + intake demo; same compose behavior **where feasible**. |

Phases **6–7** are **stretch goals** for a portfolio narrative; they depend on **Phase 5** (polish) and healthy test coverage across **1a–4**.

---

## Phase 0 — Specifications *(current)*

**Objectives**

- Align on vision, architecture, phased delivery, and per-phase definition of done.
- Establish README as the entry point for contributors and reviewers.

**Dependencies:** None.

**Deliverables:** This `specs/` set and `README.md`.

---

## Phase 1a — Monolith core + containers baseline

**Objectives**

- Runnable Spring Boot 3 / Java 21 app with health/readiness-style endpoints.
- **Incident** domain: create/read/list and **draft lifecycle** (e.g. draft → acknowledged/promoted—exact states per implementation plan) with validation. **Manual** create/update of drafts is supported; **no automated signal ingestion** in this sub-phase.
- Persistence (relational DB; H2 acceptable for local demo if clearly documented); schema migrations strategy (Flyway/Liquibase—TBD).
- **Containerization (moved early):** **Dockerfile** for the application (multi-stage acceptable if small) and **`docker compose`** (or equivalent) that starts **app + database** with documented ports, env vars, and health checks. README: clone → `docker compose up` → hit health and incident APIs (smoke path).
- **No** generative AI, **no** vector store, **no** MCP tools, **no** OpenTelemetry Demo dependency in default CI.

**Dependencies:** Phase 0 accepted.

**Risks to manage:** Over-scoping UI; keep API-first. Compose vs bare-metal dev drift—document **both** if supported.

---

## Phase 1b — Signals → draft (OpenTelemetry Demo in Docker)

**Objectives**

- **Journey A — signals → draft:** Run **[OpenTelemetry Demo](https://github.com/open-telemetry/opentelemetry-demo)** in **Docker** (upstream compose or a **named minimal profile** checked into this repo or documented as a pinned revision). Incident Assistant consumes signals via **webhook, poll, or documented adapter** (per implementation plan)—**not** LLM-gated.
- When **abnormality rules** fire, create **`draft`** incidents carrying **telemetry pointers** (trace id, service, time window—minimum set per ADR). **Dedup or cooldown** policy documented and implemented (or explicitly deferred with issue link).
- **Compose integration:** Either **one compose stack** (recommended for demos: shared network) or **two compose files** with explicit networking/URL documentation so a reviewer can reproduce Journey A on a fresh machine. Default **CI** continues to use **in-memory doubles or recorded fixtures**—no mandatory full OTel Demo in PR checks.
- **No** generative AI, **no** vector store, **no** MCP tools. Optional **LLM copy polish** on drafts belongs to **Phase 2+** and must remain non-remediating.

**Dependencies:** Phase 1a complete.

**Risks to manage:** OTel Demo stack weight (CPU, images)—**minimal profile** and pinned versions. Alert noise / duplicate drafts. Breaking changes in upstream demo—pin **image digests or release tag** in docs.

**Detailed specifications:** **1a** [`phases/phase-1a-monolith-core/spec.md`](phases/phase-1a-monolith-core/spec.md) → **1b** [`phases/phase-1b-signal-ingest/spec.md`](phases/phase-1b-signal-ingest/spec.md) (implement in order; do not mix requirements).

---

## Phase 2 — LLM-assisted analysis

**Objectives**

- Introduce **LLM client interface** + **mock/stub** implementation for default CI and local without keys.
- Optional real provider behind configuration flag.
- API (or sub-resource) that returns **facts / assumptions / recommendations** with clear semantics.
- Rate limits, timeouts, and input size caps.

**Dependencies:** Phase **1b** complete.

**Risks:** prompt injection from incident text; mitigate in implementation plan (sanitization, system prompts, separation of trusted vs untrusted content).

---

## Phase 3 — RAG

**Objectives**

- Seed or upload **demo documents** (runbook-like markdown) with a defined ingestion path.
- Retrieval + generation pipeline that **requires citations** when using retrieved content.
- Integration tests for “answer references known chunk” and “no evidence → no fake citations.”

**Dependencies:** Phase 2 complete.

**Risks:** embedding costs and flakiness; use test doubles and small fixtures in CI.

---

## Phase 4 — MCP-style tools

**Objectives**

- Tool catalog: name, description, JSON Schema for arguments, implementation class.
- Dispatcher validates tool + args; rejects unknown tools.
- A **small** set of tools (e.g. summarize timeline, list related incident IDs from mock data—**non-destructive**).
- Correlation with assistant flow: tool calls are explicit steps (logged), not hidden side effects.

**Dependencies:** Phase 2 (and ideally Phase 3) complete; exact coupling TBD in phase kickoff.

**Risks:** scope creep into “real” automation; enforce **read-only / compute-only** tools for demo.

---

## Phase 5 — Container & demo packaging polish

**Objectives**

- **Harden** container story after features exist: multi-stage **optimized** image where worthwhile, **non-root** user if appropriate, image size notes.
- **Compose profiles:** extend `docker compose` (or layered files) to include **Phase 3+** dependencies when present (e.g. **vector DB**, embedding-sidecar—exact list per ADR).
- **CI / release:** optional workflow to **build and push** image (document if nightly vs PR); README **“golden path”** updated for full journeys **A–D** in containers.
- **Not** the first introduction of Docker—that is **Phase 1a**; this phase **refreshes** packaging for the whole demo.

**Dependencies:** Phases **1b** and **4** complete (Phase 3 must be complete before compose includes vector dependencies).

---

## Phase 6 — Microservices *(stretch)*

**Objectives**

- Extract **one** bounded context (e.g. `assistant` + RAG) into a separate Spring Boot service **or** a thin sidecar-style service.
- Contract tests at HTTP boundary; shared API spec (OpenAPI) checked into repo.
- Monolith or BFF may remain for incidents.

**Dependencies:** Phase **5**; module boundaries clean in Phases **1a–1b** and **2–4**.

**Risks:** distributed tracing and duplicate configuration; keep demo **small**.

---

## Phase 7 — Kubernetes *(stretch)*

**Objectives**

- Manifests (or Helm chart) for **local** deployment (e.g. minikube, kind) for **Incident Assistant** and its dependencies (DB, etc.)—**aligned with compose behavior where feasible**.
- **OpenTelemetry Demo on Kubernetes (optional stretch):** Document—and optionally check in minimal **kustomize/Helm overlays** or links to upstream guidance—to run **[OpenTelemetry Demo](https://github.com/open-telemetry/opentelemetry-demo)** (or the **same subset** as Phase 1b) **on-cluster** alongside or peer-networked to Incident Assistant, so reviewers can validate **Journey A** without Docker-only compose. Clearly state **resource requirements** (CPU/RAM) and that this path is **optional** for portfolio timelines.
- Document resource limits, secrets via Kubernetes Secrets, and how to run **without cloud vendor lock-in**.

**Dependencies:** Phase **5** (Phase **6** optional if Kubernetes deploys the monolith only).

---

## What we are not sequencing yet

- Exact REST paths, DTO names, or UI framework—these belong in **Phase 1a implementation plan** and ADRs, not in this roadmap v1.
