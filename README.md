# Incident Assistant - WIP

## Introduction

**Incident Assistant** is a **Java 21 / Spring Boot 3** demo project that shows how to integrate **AI into an enterprise-style backend** in a **specification-driven**, **testable**, and **safe** way. It is intended for **learning and portfolios**, not as a production incident management system.

## Authorship

**Author:** Ivy Chan

Incident Assistant is a portfolio and learning demo. Ivy Chan owns product direction, specification and architecture choices, implementation review, validation, and final decisions. **ChatGPT**, **Claude**, and **Cursor** supported planning, drafting, code generation, and refinement.

## What this repo contains (today)

- `**specs/`** — product vision, architecture, phased roadmap, acceptance criteria, and `**specs/phases/**` per-phase detail: **1a** → `[phase-1a-monolith-core/](specs/phases/phase-1a-monolith-core/spec.md)`, **1b\*\* → `[phase-1b-signal-ingest/](specs/phases/phase-1b-signal-ingest/spec.md)` (1b after 1a).
- `**.cursor/rules/`\*\* — Cursor project rules aligned with spec-driven delivery (optional for contributors using Cursor).

There is **no application code yet**; implementation starts in **Phase 1a** after specs are reviewed.

## Quick links

| Document                                                                                   | Description                                                                       |
| ------------------------------------------------------------------------------------------ | --------------------------------------------------------------------------------- |
| [specs/00-product-vision.md](specs/00-product-vision.md)                                   | Problem, users, journeys, goals, non-goals                                        |
| [specs/01-architecture.md](specs/01-architecture.md)                                       | Monolith-first design, modules, future service boundaries, AI/RAG/tool principles |
| [specs/02-roadmap.md](specs/02-roadmap.md)                                                 | Phased roadmap from monolith through optional K8s                                 |
| [specs/03-acceptance-criteria.md](specs/03-acceptance-criteria.md)                         | Definition of done and testing strategy per phase                                 |
| [specs/phases/phase-1a-monolith-core/spec.md](specs/phases/phase-1a-monolith-core/spec.md) | **Phase 1a** — manual incidents, Docker baseline (API, model, tests)              |
| [specs/phases/phase-1b-signal-ingest/spec.md](specs/phases/phase-1b-signal-ingest/spec.md) | **Phase 1b** — signal ingest, OTel Demo (after 1a)                                |
| [specs/openapi/openapi-1a.yaml](specs/openapi/openapi-1a.yaml)                             | OpenAPI for **1a**                                                                |
| [specs/openapi/openapi-1b.yaml](specs/openapi/openapi-1b.yaml)                             | OpenAPI for **1b** ingest (merge with 1a after 1b)                                |

## Vision (one paragraph)

Engineers investigating incidents need assistance that is **grounded in evidence**, **honest about uncertainty**, and **safe to run in automation-aware pipelines**. The project starts with **Journey A**: when **[OpenTelemetry Demo](https://github.com/open-telemetry/opentelemetry-demo)** (or a documented subset) shows **abnormality**, the backend can open a **draft incident** with pointers back to traces/metrics/logs—**reviewable intake**, not auto-fix. Incident Assistant then demonstrates **structured LLM outputs**, **RAG with citations**, and **MCP-style tools** behind clear interfaces—starting as a **modular monolith** with **Docker/compose from Phase 1a**, **OpenTelemetry Demo in Docker** for **1b**, then **packaging polish**, and only later optional **microservices** and **Kubernetes** (with **optional OTel-on-cluster** in Phase 7)—without **automatic remediation** of real production systems in early phases.

## Roadmap at a glance

1. **Phase 0** — Specifications _(you are here)_
2. **Phase 1a** — Monolith core: Spring Boot, incident APIs + **draft lifecycle**, persistence, tests; **Dockerfile + compose (app + DB)** (**no AI**, **no OTel signals**)
3. **Phase 1b** — **OpenTelemetry Demo in Docker** + **signals → draft** incidents (rule-based, CI uses doubles/fixtures) (**no AI**)
4. **Phase 2** — LLM integration: mockable client, structured facts/assumptions/recommendations
5. **Phase 3** — RAG: retrieval + **mandatory citations** when evidence is used
6. **Phase 4** — MCP-style tools: explicit registry, schemas, safe demo tools only
7. **Phase 5** — **Packaging polish**: hardened images, compose with **post-RAG** deps when present, optional CI image, README golden path _(Docker introduced in 1a)_
8. **Phase 6** _(stretch)_ — Split one bounded context behind HTTP + contracts
9. **Phase 7** _(stretch)_ — Kubernetes for local cluster; **optional** OTel Demo **on-cluster** for end-to-end Journey A demos

Details, dependencies, and risks: **[specs/02-roadmap.md](specs/02-roadmap.md)**.

## Principles (non-negotiable for implementation)

- **Specs before features** — behavior changes start with spec updates.
- **Phases fully tested** before starting the next phase (see [specs/03-acceptance-criteria.md](specs/03-acceptance-criteria.md)).
- **No auto-remediation** of production in early phases; assist investigation, do not silently “fix” prod.
- **LLMs mockable in CI**; external dependencies behind interfaces.
- **RAG cites sources**; **tools are allowlisted and testable**.

## Before Phase 1a starts (human reviewer)

Answer the open questions in the **Summary** section at the end of this README (or delegate ownership). **Phase 1a** implementation planning should not proceed until **must-decide** items have owners and decisions.

## Contributing (after code exists)

Commands for build and test will be added in **Phase 1a** when the project is scaffolded. Until then, contributions are **spec-only** unless explicitly agreed.

## License

_To be determined by repository owner._

---

## Summary for reviewers

### Proposed roadmap (concise)

| Order | Phase                               | Outcome                                                                                                                               |
| ----- | ----------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------- |
| 0     | Specifications                      | Vision, architecture, roadmap, acceptance criteria, README                                                                            |
| 1a    | Monolith core + containers baseline | Runnable Spring Boot app, incident APIs + drafts, persistence, tests, **Dockerfile + compose (app + DB)**, no AI, **no OTel signals** |
| 1b    | Signals → draft (OTel in Docker)    | **OpenTelemetry Demo** via Docker + **rule-based** signals → **draft** incidents + dedup story; CI uses doubles/fixtures              |
| 2     | LLM-assisted analysis               | Interface-based LLM, structured safe responses, mocks in CI                                                                           |
| 3     | RAG                                 | Retrieval + answers with **citations**; honest low-evidence path                                                                      |
| 4     | MCP-style tools                     | Registry, schemas, dispatcher, non-destructive demo tools                                                                             |
| 5     | Container & demo packaging polish   | Hardened images, compose with **post-RAG** deps when present, optional CI image, README golden path                                   |
| 6     | Microservices _(stretch)_           | One extracted service + contract tests                                                                                                |
| 7     | Kubernetes _(stretch)_              | Local-cluster manifests; **optional** OTel Demo **on-cluster** for Journey A                                                          |

### Questions a human reviewer should answer before Phase 1a

**Must decide (blocking Phase 1a kickoff)**

1. **Build tool:** Maven or Gradle? (Affects repo layout and CI templates.)
2. **Persistence for local dev vs CI:** Testcontainers from day one vs H2 for speed—with explicit tradeoff acceptance?
3. **API style:** REST only for all phases, or allow Spring MVC vs WebFlux decision now?
4. **Incident scope for Phase 1a vs 1b:** Minimum fields and operations for **1a** (create/get/list + **draft lifecycle**, promote/edit, **manual** only). For **1b**: which **signals** and **one** integration shape (webhook vs poll) for OTel Demo.
5. **Documentation home:** Are `specs/` + README sufficient, or do you require ADRs in `docs/adr/` for Phase 1a decisions?

**Should decide (strongly recommended)**

1. **Demo narrative:** Single vertical story (e.g. “API outage playbook”) for later RAG corpus, or generic placeholders?
2. **OpenTelemetry Demo scope:** Full stack vs **minimal compose profile** for local dev; which **signals** (metrics only, traces, logs) gate **draft** creation in **Phase 1b**?
3. **OpenAPI:** Generate and publish from Phase **1a** controllers, or defer to Phase 2?
4. **Auth:** Explicitly out of scope until a named phase, or minimal API key from Phase **1a** (especially for any **signal ingress** in **1b**)?
5. **Observability:** Micrometer + logs only in Phase **1a**, or any tracing baseline (e.g. Micrometer tracing) from the start?
6. **Stretch phases 6–7:** Confirm they are optional for portfolio timeline vs required outcomes.

**Assumptions to validate**

1. Target JDK is **21** everywhere (CI, Docker, dev)—any exception for contributors?
2. **English-only** UI/API messages for demo is acceptable.
3. No requirement to integrate with a real vendor (PagerDuty, Slack, Datadog) in v1—stubs/mocks only until a later optional phase; **OpenTelemetry Demo** is the intentional telemetry reference, not a production vendor SLA.

Once these are answered, capture decisions in short **Phase 1a** and **Phase 1b** implementation plans (per project rules: summary, affected files, plan, test plan, risks) before writing application code.
