# Product vision — Incident Assistant

## Summary

Incident Assistant is a **portfolio-grade demo** of how to integrate AI into an **enterprise-style Java 21 / Spring Boot 3** backend. It emphasizes **specification-driven development**, **safe and testable AI**, **RAG with citations**, **MCP-style tools**, and a path from **monolith → containers → microservices → Kubernetes**—without pretending to be a full production platform.

## Problem statement

During incidents, engineers juggle logs, metrics, runbooks, and chat threads. Generic AI assistants can **hallucinate**, **blur facts with guesses**, or suggest **risky actions** without evidence. Organizations need patterns that are:

- **Grounded** in retrievable evidence (e.g. runbooks, past incidents, configuration snippets).
- **Transparent** about what is known vs assumed vs recommended.
- **Testable** in CI (mocked LLMs, deterministic tool contracts).
- **Bounded** in scope: assist investigation and summarization, not autonomous remediation.

Incident Assistant demonstrates those patterns in a **small, realistic** codebase suitable for interviews and learning—not a replacement for PagerDuty, Datadog, or enterprise incident platforms.

**Clarification:** **Automatically opening a *draft* incident** from observability signals (e.g. elevated error rates, latency SLO burn, anomalous trace patterns) is **in scope** for the demo: it **creates reviewable work**, it does **not** apply fixes or change production systems. That is **not** remediation; see non-goals below.

## Target users


| Persona                                                                       | Need                                                                                                 |
| ----------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------- |
| **Primary** — Backend / platform engineers evaluating AI integration patterns | Reference architecture for Spring Boot + AI + RAG + tools with strong testing and safety boundaries. |
| **Secondary** — Tech leads and architects                                     | Clear phased roadmap, future service boundaries, and tradeoffs (monolith first).                     |
| **Tertiary** — Reviewers of a portfolio or technical blog                     | Understandable scope, honest limitations, reproducible demo story.                                   |


## Goals

- Show **spec-first** delivery: behavior is defined in `specs/` before implementation phases.
- Start as a **single deployable monolith** with **clean modules** and **interfaces** that anticipate later splits.
- Show **observability-driven intake** using a **realistic demo stack** (e.g. **[OpenTelemetry Demo](https://github.com/open-telemetry/opentelemetry-demo)**): signals and context from logs/metrics/traces feed **draft** incident creation—not silent fixes.
- Integrate **LLM-assisted** incident analysis with **structured** outputs (facts, assumptions, recommendations).
- Add **RAG** so answers **cite sources** when using retrieved context.
- Add **MCP-style tools**: explicit registry, allowlisted operations, audit-friendly logging, fully mockable in tests.
- Progress to **Docker**, then optional **microservices**, then **Kubernetes** for a credible “enterprise demo arc.”

## Non-goals (explicit)

- **No automatic remediation** in early phases (no auto-restarts, config applies, or production writes without human-in-the-loop design—and not in scope for initial demos). **Draft incident creation from telemetry is explicitly allowed** and is **not** considered remediation.
- **Not** a complete incident management product (on-call scheduling, paging, full observability backends are out of scope or stubbed).
- **Not** legal or compliance certification; the project illustrates **engineering practices**, not certified controls.

## Core user journeys

### Journey A — Observability-driven draft incidents (foundation)

**Intent:** When the platform observes **abnormality** in a **demo-friendly telemetry stack**, Incident Assistant **opens a draft incident ticket** so humans can triage—mirroring how real organizations connect observability to incident workflow, without auto-fixing anything.

1. **Signal context:** The demo environment is anchored on **[OpenTelemetry Demo](https://github.com/open-telemetry/opentelemetry-demo)** (or a **documented subset** of its collectors, backends, and services). Traces, metrics, and/or logs (per phase implementation plan) provide **evidence**—for example correlated trace IDs, service names, error counts, or latency regression windows.
2. **Abnormality detection (demo-realistic):** The system applies **explicit, configurable rules** (thresholds, simple anomaly heuristics, or webhook-style alerts from the demo stack—not a black-box “AI says fire” in **Phase 1b**). What counts as “abnormal” is **versioned and testable** (fixtures in CI; **OpenTelemetry Demo** runs in **Docker** for manual demos per `specs/02-roadmap.md`).
3. **Draft ticket:** When rules fire, the system creates an incident in **draft** state with **structured fields** (title, severity suggestion, summary text, **links or IDs** back to telemetry—e.g. trace id, time window, service). No automatic escalation to paging, no runbook execution, no infrastructure changes.
4. **Human review:** User lists and opens incidents, filters drafts, and **promotes or edits** a draft (exact transitions per **Phase 1a/1b** implementation plan). Manual create/update remains available for comparison.

*Success:* Draft incidents are **durable**, **traceable to signal evidence**, **idempotent where possible** (duplicate storms do not spam—dedup strategy TBD in implementation plan), and covered by **unit and integration tests** without requiring the full OTel Demo in default CI.

**Later enhancement (optional phases):** Phase 2+ may **enrich** the same draft with LLM-generated narrative *after* the draft exists, still under facts/assumptions/recommendations and without remediation—never a substitute for cited RAG when organizational knowledge is required.

### Journey B — Ask the assistant for analysis (LLM, grounded)

1. User attaches context (incident fields, optional pasted logs—within limits).
2. User requests a structured analysis (timeline hypothesis, questions to ask, similar patterns).
3. System returns **facts** (from inputs), **assumptions** (explicit), and **recommendations** (non-destructive), with **no hidden tool execution** in early phases.

*Success:* Responses expose **facts / assumptions / recommendations** (or equivalent contract); behavior is **deterministic in CI** via mocked LLMs; timeouts and input caps are enforced; outputs are **never** labeled as verified production truth; **no hidden tool calls** in early phases.

### Journey C — Ask with organizational knowledge (RAG)

1. User asks a question that should use internal reference material (runbooks, postmortems, architecture notes—in demo, seeded documents).
2. System retrieves relevant chunks and answers **with citations** to those sources.
3. If evidence is insufficient, the system says so instead of inventing detail.

*Success:* When retrieval is used, answers include **traceable citations** (source id + locator); when evidence is weak or absent, the system returns an explicit **insufficient evidence** path with **no fabricated citations**; RAG paths are covered by **integration tests** on a small fixed corpus.

### Journey D — Use tools safely (MCP-style)

1. User or agent workflow requests an **explicit** tool (e.g. “format timeline,” “fetch mock metric snapshot”—concrete list per phase).
2. System validates the tool name and parameters against an **allowlist** and **schema**.
3. Tool runs with **logging** suitable for demo audit; tests use **fakes** or in-memory implementations.

*Success:* Only **registered, allowlisted** tools run; **unknown names and invalid arguments** are rejected at the boundary; each invocation is **auditable** (tool name, correlation id, outcome—no secrets in logs); demo tools remain **non-destructive**; **unit tests per tool** and **integration tests** for the dispatcher ship with the feature.

### Journey E — Run locally in containers (later)

1. Developer runs `docker compose` (or equivalent) to start app and dependencies.
2. Same journeys A–D work against containerized services.

*Success:* **Container image(s)** build **reproducibly** from locked dependencies; **Compose** (or documented equivalent) starts the app and its dependencies with **documented ports, env vars, and health checks**; a **fresh-machine README path** validates journeys **A–D** (or an explicitly documented subset); **resource expectations** and known **platform limits** are stated so the demo is repeatable.

### Journey F — Optional split deployment (much later)

1. Incident API and AI/RAG pathways deploy as separate services behind clear contracts.
2. Kubernetes manifests support a minimal demo deployment (e.g. local cluster).

*Success:* **Published contracts** (e.g. OpenAPI) exist for cross-service boundaries; **contract or smoke tests** cover at least one critical call path; manifests apply to a **local** Kubernetes target (e.g. kind, minikube) **without vendor-specific operators**; **README** documents apply, port-forward or ingress, secrets handling, and **known gaps** vs the monolith/compose path.

## Principles (cross-cutting)

- **Start small:** ship thin vertical slices per phase; each phase has a **definition of done** in `specs/03-acceptance-criteria.md`.
- **Human approval for scope:** roadmap phases are not expanded ad hoc without updating specs and reviewer sign-off.
- **Demo realism:** prefer clear boundaries and documentation over feature count.

## Document map


| Document                          | Purpose                                                      |
| --------------------------------- | ------------------------------------------------------------ |
| `specs/01-architecture.md`        | Components, monolith-first layout, future service boundaries |
| `specs/02-roadmap.md`             | Phased delivery order and dependencies                       |
| `specs/03-acceptance-criteria.md` | Per-phase definition of done and test expectations           |
| `specs/phases/phase-1a-monolith-core/` | Detailed spec for **Phase 1a** (manual incidents, containers) |
| `specs/phases/phase-1b-signal-ingest/` | Detailed spec for **Phase 1b** (OTel, ingest) — after 1a |
| `specs/openapi/openapi-1a.yaml` | OpenAPI artifact for **1a** |
| `specs/openapi/openapi-1b.yaml` | OpenAPI artifact for **1b** ingest (merge for full API) |


