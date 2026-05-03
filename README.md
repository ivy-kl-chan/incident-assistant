# Incident Assistant - WIP

## Introduction

**Incident Assistant** is a **Java 21 / Spring Boot 3** demo project that shows how to integrate **AI into an enterprise-style backend** in a **specification-driven**, **testable**, and **safe** way. It is intended for **learning and portfolios**, not as a production incident management system.

## Authorship

**Author:** Ivy Chan

Incident Assistant is a portfolio and learning demo. Ivy Chan owns product direction, specification and architecture choices, implementation review, validation, and final decisions. **ChatGPT**, **Claude**, and **Cursor** supported planning, drafting, code generation, and refinement.

## What this repo contains (today)

- **`specs/`** — product vision, architecture, phased roadmap, acceptance criteria, and **`specs/phases/`** per-phase detail: **1a** → [`phase-1a-monolith-core/`](specs/phases/phase-1a-monolith-core/spec.md), **1b** → [`phase-1b-signal-ingest/`](specs/phases/phase-1b-signal-ingest/spec.md) (1b after 1a).
- **`docs/adr/`** — architecture decision records (kickoff tooling, Phase 1b delivery shape).
- **`.cursor/rules/`** — Cursor project rules aligned with spec-driven delivery (optional for contributors using Cursor).
- **Spring Boot monolith** — Java 21, Maven; **Flyway `V1`** baseline schema (PostgreSQL); health/readiness via Actuator (see [Local development](#local-development)).

## Local development

### Prerequisites

- **JDK 21**
- **Maven** (3.9+ recommended)
- **Docker** (recommended): integration tests that use **Testcontainers** (PostgreSQL, Flyway migrations) need a Docker daemon. If Docker is not available, those tests may be **skipped** (`@Testcontainers(disabledWithoutDocker = true)`). **`mvn verify`** matches full Phase **1a** coverage when Docker runs locally or in **[default CI](#continuous-integration)** (GitHub-hosted runners provide Docker, so **`FlywayV1BaselineIntegrationTest`** runs there).

### Build, run, and test (bare JVM)

From the repository root:

```bash
mvn clean verify
```

Run the application:

```bash
mvn spring-boot:run
```

Default HTTP port is **8080** unless overridden.

### Actuator (restricted)

Only the **health** endpoint group is exposed over HTTP. That yields:

| Endpoint                         | Role                                               |
| -------------------------------- | -------------------------------------------------- |
| `GET /actuator/health`           | Liveness — process is up.                          |
| `GET /actuator/health/readiness` | Readiness probe URL (see interim semantics below). |

Other actuator endpoints (for example `/actuator/env`, `/actuator/metrics`) are **not** exposed by default configuration.

### Readiness and the database

When the application starts **with** JDBC and a live PostgreSQL instance (normal `mvn spring-boot:run` after the database exists), Spring Boot’s readiness probe can reflect **database** availability like any standard JDBC-backed service.

The **`ActuatorHealthTest`** suite intentionally starts a slice **without** `DataSource` / Flyway auto-configuration so actuator HTTP exposure and the **approved interim aggregate `"UP"`** readiness behavior can be asserted **without** Docker or a local database in minimal environments. Full **Flyway `V1`** application on an empty database is covered by **`FlywayV1BaselineIntegrationTest`** (Testcontainers; requires Docker when not skipped).

### Quick checks

With the app running:

```bash
curl -sSf http://localhost:8080/actuator/health
curl -sSf http://localhost:8080/actuator/health/readiness
```

There is **no** `/api/v1/signal-ingest/*` in this scaffold; signal ingest arrives in Phase 1b per specs.

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
| [specs/openapi/openapi-1b.yaml](specs/openapi/openapi-1b.yaml)                             | OpenAPI for **1b** (ingest + extended reads; merge with 1a)                       |
| [docs/adr/README.md](docs/adr/README.md)                                                   | Architecture decision records (ADR index)                                         |

## Vision (one paragraph)

Engineers investigating incidents need assistance that is **grounded in evidence**, **honest about uncertainty**, and **safe to run in automation-aware pipelines**. The project starts with **Journey A**: when **[OpenTelemetry Demo](https://github.com/open-telemetry/opentelemetry-demo)** (or a documented subset) shows **abnormality**, the backend can open a **draft incident** with pointers back to traces/metrics/logs—**reviewable intake**, not auto-fix. Incident Assistant then demonstrates **structured LLM outputs**, **RAG with citations**, and **MCP-style tools** behind clear interfaces—starting as a **modular monolith** with **Docker/compose from Phase 1a**, **OpenTelemetry Demo in Docker** for **1b**, then **packaging polish**, and only later optional **microservices** and **Kubernetes** (with **optional OTel-on-cluster** in Phase 7)—without **automatic remediation** of real production systems in early phases.

## Roadmap at a glance

1. **Phase 0** — Specifications
2. **Phase 1a** — Monolith core _(scaffold + health in progress; incident APIs and Docker compose follow per roadmap)_: Spring Boot, incident APIs + **draft lifecycle**, persistence, tests; **Dockerfile + compose (app + DB)** (**no AI**, **no OTel signals**)
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

## Kickoff decisions (human reviewer — recorded 2026-04-26)

Blocking and recommended questions from the **Summary** below are **answered**. Full prose and consequences: **[`docs/adr/0001-kickoff-tooling-testing-and-1a-scope.md`](docs/adr/0001-kickoff-tooling-testing-and-1a-scope.md)** and **[`docs/adr/0002-phase-1b-webhook-and-incremental-telemetry.md`](docs/adr/0002-phase-1b-webhook-and-incremental-telemetry.md)**.

## Contributing

Use **`mvn clean verify`** before submitting changes. Follow **`specs/`** and project rules in **`.cursor/rules/`** (Cursor optional). Feature work should match the active phase story and acceptance criteria.

## Continuous integration

**GitHub Actions** is the default CI host. Workflow: **[`.github/workflows/ci.yml`](.github/workflows/ci.yml)**.

| Trigger                                 | Behavior                                                                             |
| --------------------------------------- | ------------------------------------------------------------------------------------ |
| **`pull_request`** targeting **`main`** | Runs **`mvn --batch-mode verify`** on **`ubuntu-latest`** with **JDK 21** (Temurin). |
| **`push`** to **`main`**                | Same job.                                                                            |

Hosted runners provide **Docker**, so **Testcontainers** integration tests—including **`FlywayV1BaselineIntegrationTest`** (PostgreSQL + Flyway **`V1`**)—**run** there instead of being skipped. Image pulls (for example **`postgres:16-alpine`**) require outbound network access from the runner.

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

### Kickoff answers (Phase 1a / 1b)

**Must decide — resolved**

| #   | Topic          | Decision                                                                                                                                                                                                                                    |
| --- | -------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | Build tool     | **Maven**                                                                                                                                                                                                                                   |
| 2   | CI vs local DB | **Testcontainers (PostgreSQL) from day one** for default integration tests                                                                                                                                                                  |
| 3   | API style      | **REST** for all phases (resource JSON HTTP)                                                                                                                                                                                                |
| 4   | 1a vs 1b scope | **1a:** minimum fields/ops per **`specs/phases/phase-1a-monolith-core/`** (manual create/get/list + draft lifecycle). **1b:** **webhook-style** HTTP ingest to this service (not poll-from-JVM as primary); signals detailed in phase specs |
| 5   | Docs home      | **`specs/` + README + `docs/adr/`** (ADRs **0001**, **0002**)                                                                                                                                                                               |

**Should decide — resolved**

| #   | Topic                   | Decision                                                                                                                                 |
| --- | ----------------------- | ---------------------------------------------------------------------------------------------------------------------------------------- |
| 1   | Demo narrative          | **Single vertical** playbook for later RAG/demo cohesion (content can grow iteratively)                                                  |
| 2   | OTel Demo + signals     | **Minimal compose profile**; Phase **1b** split into stories: **metrics first**, then **traces**, then **logs** (each own backlog story) |
| 3   | OpenAPI                 | **In Phase 1** (maintain **`specs/openapi/`** with controllers)                                                                          |
| 4   | End-user auth           | **Out of scope** until a named later phase (ingest **token** remains per **1b** `api-contract.md`)                                       |
| 5   | Observability in **1a** | **Logs only** (no Micrometer tracing baseline in **1a**)                                                                                 |
| 6   | Stretch **6–7**         | **TBD** vs portfolio timeline—remain **optional** until promoted                                                                         |

**Assumptions — validated**

| #   | Assumption                                       | Validated |
| --- | ------------------------------------------------ | --------- |
| 1   | JDK **21** everywhere                            | **Yes**   |
| 2   | **English-only** UI/API for demo                 | **Yes**   |
| 3   | No real vendor SLA in v1; OTel Demo as reference | **Yes**   |

Implementation plans under **`specs/phases/`** track execution; ADRs above are the durable record.
