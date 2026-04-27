# Story 1 — Application scaffold and health endpoints

## 1. Status

Planned

## 2. Goal

Establish a runnable **Java 21 / Spring Boot 3** monolith with **Maven**, restricted **Actuator** exposure, and baseline configuration so later stories can add persistence and HTTP APIs.

## 3. User Value

Reviewers and developers can build and run the application shell, verify process health, and confirm the technical baseline before incident features land.

## 4. Spec References

| Document | Relevance |
|----------|-----------|
| [`../README.md`](../README.md) | Phase 1 split: **1a** then **1b**; authoritative detail under linked phase folders |
| [`../../phase-1a-monolith-core/spec.md`](../../phase-1a-monolith-core/spec.md) | Spring Boot 3 / Java 21 monolith; logs-only observability in 1a |
| [`../../phase-1a-monolith-core/api-contract.md`](../../phase-1a-monolith-core/api-contract.md) | Actuator: `GET /actuator/health`, `GET /actuator/health/readiness` (readiness wired when DB exists) |
| [`../../phase-1a-monolith-core/implementation-plan.md`](../../phase-1a-monolith-core/implementation-plan.md) | M1 scaffold + Actuator (restricted) |
| [`../../../03-acceptance-criteria.md`](../../../03-acceptance-criteria.md) | Phase 1a: builds and runs locally; health documented |

## 5. In Scope

- Maven project layout for the monolith application.
- Spring Boot application entrypoint and minimal configuration.
- Actuator endpoints **`/actuator/health`** (liveness) and **`/actuator/health/readiness`** per **1a** contract; restrict exposure to **health** and **readiness** only (align with product README guidance).
- Documented local run prerequisites (JDK version, Maven) for **bare JVM** execution.

## 6. Out of Scope

- Incident REST APIs, Flyway, domain entities, OpenAPI YAML (later stories).
- **Signal ingest** routes or `signals.enabled` behavior (**1b**).
- Generative **AI**, **RAG**, **MCP** tooling.
- **Docker**, **docker compose**, image build, **Kubernetes**, **microservices** split.
- Full readiness **database** verification until persistence exists (may return a documented placeholder or degrade gracefully until Story 2–3; exact behavior must match updated README for that interim—prefer aligning readiness completion with Story 2–3 to avoid false green).

## 7. API Changes

- **New (non-versioned):** `GET /actuator/health`, `GET /actuator/health/readiness` as specified in **1a** `api-contract.md`.
- No `/api/v1/*` resources in this story.

## 8. Data Model Changes

None.

## 9. Business Rules

- Actuator surface remains minimal; no extra actuator endpoints exposed without a spec/ADR change.

## 10. Acceptance Criteria

- [ ] Project builds with **Java 21** and **Spring Boot 3** using **Maven**.
- [ ] Application starts locally; **`GET /actuator/health`** returns success when the process is up.
- [ ] **`GET /actuator/health/readiness`** exists and behavior is **documented** (including any interim state before DB wiring).
- [ ] Actuator exposure matches **restricted** contract (health + readiness only).
- [ ] No `/api/v1/signal-ingest/*` and no incident APIs required for this story’s gate.

## 11. Test Requirements

- Smoke or integration test that loads the Spring context and calls **`/actuator/health`** (and readiness if applicable).
- Per **1a** `test-plan.md` spirit: no OpenTelemetry stack in default CI.

## 12. Files Expected to Change

- Root / module **`pom.xml`** (or parent POM), application **`src/main/java/**`** entrypoint, **`src/main/resources/application*.yml`**, test sources under **`src/test/java/**`**, and **README** snippets for local run if acceptance criteria require it.

## 13. Implementation Notes

- Prefer a single module layout consistent with **`docs/adr/0001-kickoff-tooling-testing-and-1a-scope.md`** (Maven).
- Defer readiness strictness until PostgreSQL + Flyway exist if that avoids misleading “UP” while DB is down; document the choice.

## 14. Human Review Checklist

- [ ] JDK and Spring versions match **1a** spec.
- [ ] Actuator lock-down is acceptable for demo/security posture.
- [ ] Interim readiness behavior is honest in docs.

## 15. Completion Notes

*(Fill when implemented: PR link, ADR updates, any deviations.)*
