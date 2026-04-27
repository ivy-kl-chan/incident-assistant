# Architecture — Incident Assistant

## Context

Incident Assistant is implemented as a **Java 21 / Spring Boot 3** application. **Phases 1a–1b** target a **modular monolith**: one deployable artifact with **internal package/module boundaries** that map to potential future services. **Phase 1a** ships core incidents and **Docker** baseline; **Phase 1b** adds **signals-ingestion** against **OpenTelemetry Demo in Docker**.

## High-level architecture (conceptual)

```text
        ┌────────────────────────────────────────────────────────────┐
        │  Signals path (Journey A) — optional HTTP / webhook / poll   │
        │  OpenTelemetry Demo–aligned collectors or alert callbacks    │
        └────────────────────────────┬───────────────────────────────┘
                                     │ draft incident requests
                                     ▼
                    ┌─────────────────────────────────────┐
                    │         API layer (HTTP)            │
                    │  incidents, signals, assistant, health │
                    └──────────────┬──────────────────────┘
                                   │
         ┌─────────────────────────┼─────────────────────────┐
         │                         │                         │
         ▼                         ▼                         ▼
┌─────────────────┐    ┌─────────────────────┐    ┌──────────────────┐
│ Incident domain │    │ Assistant / AI      │    │ Tooling (MCP-    │
│ (commands/queries│    │ orchestration       │    │ style) registry  │
│ drafts, promote) │    │ prompts, policies   │    │ allowlist, audit │
└────────┬────────┘    └──────────┬──────────┘    └────────┬─────────┘
         │                        │                        │
         │              ┌─────────┴─────────┐              │
         │              │                   │              │
         │              ▼                   ▼              │
         │     ┌──────────────┐    ┌──────────────┐        │
         │     │ LLM client   │    │ RAG pipeline │        │
         │     │ (interface)  │    │ retrieve+cite│        │
         │     └──────────────┘    └──────────────┘        │
         │                                                   │
         └───────────────────────┬─────────────────────────┘
                                   ▼
                    ┌─────────────────────────────────────┐
                    │ Infrastructure adapters             │
                    │ DB, vector store, HTTP clients,     │
                    │ config, observability (per phase)   │
                    └─────────────────────────────────────┘
```

This diagram is **logical**. Physical deployment starts as **one process**; later phases may split processes.

## Monolith-first structure (recommended direction)

The following **logical modules** (Maven/Gradle multi-module or strict packages—decision deferred to **Phase 1a** implementation plan) keep the codebase ready for extraction:

| Module / boundary | Responsibility |
|-------------------|------------------|
| **`incident-api`** | HTTP controllers, DTOs, validation for incident lifecycle, reads, and **draft promotion** (state transitions per spec). |
| **`signals-ingestion`** *(logical)* | **Abnormality → draft incident:** adapters for OpenTelemetry Demo–compatible inputs (e.g. webhook from demo alert rule, OTLP-derived summaries, or scheduled evaluation of exported metrics—**exact wiring in Phase 1b plan**). Emits **commands** to incident domain; **no** remediation side effects. |
| **`incident-domain`** | Domain model, application services, **draft vs active** (or equivalent) lifecycle, domain events (if any)—no Spring Web. |
| **`assistant`** | Orchestration: when to call LLM, RAG, tools; response shaping (facts / assumptions / recommendations). |
| **`rag`** | Ingestion hooks, chunking strategy interface, retrieval, citation assembly. |
| **`tools`** | MCP-style tool definitions: names, JSON schemas, handlers, registration. |
| **`infrastructure`** | Persistence, external clients, vector store adapter, configuration binding. |

**Rule:** domain and assistant logic depend on **interfaces** for IO; adapters live at the edge and are swapped in tests.

## Future microservice boundaries

These boundaries are **candidates for extraction**, not commitments for Phases **1a–1b**:

1. **Incident service** — CRUD/read APIs, incident persistence, authorization hooks (demo may stay open).
2. **Assistant / reasoning path** — LLM + RAG orchestration; could become a separate service if rate limits, GPU, or model routing differ from core API scaling.
3. **Knowledge / RAG service** — document ingestion, embedding jobs, vector search; natural split if storage and compute grow.
4. **Tool execution gateway** — if tools ever integrate with real systems, isolate execution, quotas, and audit behind a dedicated service.
5. **Signals / alert ingestion** — if OTel Collector, alertmanager-style routers, or high-volume webhooks need isolation from the core API, extract **ingestion + normalization** behind a queue and a dedicated service (later phase only).

**Migration strategy (later phases):** preserve **DTO contracts** and **interface-based** clients between modules so HTTP or message-based clients can replace in-process calls.

## External dependencies (abstraction)

All external capabilities are accessed through **interfaces** implemented in `infrastructure`:

| Capability | Principle |
|------------|-----------|
| Relational DB | Repository interfaces in domain; JPA/JDBC adapters in infrastructure. |
| Observability / demo signals | **`SignalsSource`**, **`AbnormalityEvaluator`**, **`IncidentDraftFactory`** (names TBD): consume normalized signals from OTel Demo or **test doubles**; **no** direct coupling from domain to vendor SDKs. |
| LLM provider | `LlmClient` (name TBD in implementation) with **no** direct SDK calls from domain services. |
| Embeddings / vector store | `EmbeddingClient`, `VectorStore` interfaces; in-memory or test doubles in CI. |
| “MCP” remote servers | Optional later; demo may use **in-process** tool handlers that mirror MCP **discoverability and schemas**. |

## AI safety (architectural)

- **Structured output channel:** API responses expose distinct fields (or clearly labeled sections) for **facts**, **assumptions**, and **recommendations**.
- **Policy layer:** max tokens, timeouts, prompt templates versioned as resources or configuration; **no** silent prompt injection from untrusted incident text without sanitization strategy (to be detailed in Phase 2+ specs).
- **Evidence linkage:** when RAG is used, **citations** are mandatory in the contract returned to clients.
- **Tool safety:** tools are **allowlisted**, **schema-validated**, and **logged**; destructive or production-touching tools are **out of scope** for early phases.

## RAG principles (architectural)

- **Retrieval is explicit:** assistant decides when to retrieve; retrieval steps are traceable in logs/tests.
- **Citation integrity:** generated text that uses retrieved chunks must reference **source id + chunk/locator**; integration tests assert presence of citations when RAG path is active.
- **Fallback:** insufficient retrieval quality → assistant responds with uncertainty and **does not** fabricate citations.

## MCP-style tool principles (architectural)

- **Explicit catalog:** tools are registered with stable names and versioned input/output schemas (JSON Schema or equivalent).
- **No ambient execution:** the LLM (when used) only invokes tools through a **narrow, reviewed** dispatcher the application controls.
- **Testability:** each tool has unit tests; dispatcher has integration tests with fake LLM that requests known tools.
- **Observability:** each invocation logs tool name, correlation id, duration, and success/failure (no secrets in logs).

## Observability and configuration (directional)

- **Internal app telemetry:** Health and readiness endpoints for containers/Kubernetes later phases; **Micrometer / OTLP export** for the Spring app itself is encouraged where it does not block **Phase 1a** scope.
- **Journey A telemetry:** Incident drafts **reference** external trace/metric/log identifiers from the **OpenTelemetry Demo** (or documented stand-in) so reviewers can jump from ticket to evidence in Jaeger/Grafana/etc.
- **Correlation IDs** propagated from HTTP through assistant, LLM, RAG, and tool calls.
- **Configuration** via Spring `application.yml` and environment variables; **feature flags** for optional real LLM in dev.

## Security (demo-appropriate)

- Authentication/authorization may be **minimal or stubbed** in early phases; if added, it must not weaken the **no auto-remediation** posture.
- Secrets (API keys) only via environment or secret mounts—not committed.

## Technology anchors

- **Java 21**, **Spring Boot 3.x**.
- Build tool and exact dependencies: **to be chosen in Phase 1a** (Maven or Gradle) and recorded in README when implementation starts.

## Out of scope for architecture v1 (this document)

- Exact database schema and API paths (defined in phase-level specs or ADRs during implementation).
- Choice of vector database vs in-memory for CI (decision belongs to Phase 3+ planning).
