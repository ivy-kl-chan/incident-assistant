# Story 1: Phase 1a — Application scaffold and health endpoints

*Label: Scaffold & Actuator*

## 1. Status

Implemented

## 2. Goal

Establish a runnable **Java 21 / Spring Boot 3** monolith with **Maven**, restricted **Actuator** exposure, and baseline configuration so later stories can add persistence and HTTP APIs.

## 3. User Value

Reviewers and developers can build and run the application shell, verify process health, and confirm the technical baseline before incident features land.

## 4. Spec References

| Document | Relevance |
|----------|-----------|
| [`../README.md`](../README.md) | Phase 1 split: **1a** then **1b**; authoritative detail under linked phase folders |
| [`../../../../README.md`](../../../../README.md) | **Deliverable with code:** update **in the same implementation change set** as the scaffold so **`api-contract.md`** *per README* (actuator restriction, health/readiness, prerequisites, run/test commands) is satisfied **after merge**—do not pre-author that README content in a spec-only pass |
| [`../../phase-1a-monolith-core/spec.md`](../../phase-1a-monolith-core/spec.md) | Spring Boot 3 / Java 21 monolith; logs-only observability in 1a |
| [`../../phase-1a-monolith-core/api-contract.md`](../../phase-1a-monolith-core/api-contract.md) | Actuator: `GET /actuator/health`, `GET /actuator/health/readiness` (readiness reflects DB when wired); **restrict** exposure **per README** |
| [`../../phase-1a-monolith-core/implementation-plan.md`](../../phase-1a-monolith-core/implementation-plan.md) | M1: scaffold + Actuator here; **Flyway `V1`** in **[`story-2-1a-flyway-baseline-schema.md`](story-2-1a-flyway-baseline-schema.md)** (see **Implementation Notes**) |
| [`../../../03-acceptance-criteria.md`](../../../03-acceptance-criteria.md) | Phase 1a: builds and runs locally; health documented |
| [`../../../../docs/adr/0001-kickoff-tooling-testing-and-1a-scope.md`](../../../../docs/adr/0001-kickoff-tooling-testing-and-1a-scope.md) | Maven; Testcontainers spirit for later stories |

## 5. In Scope

- Maven project layout for the monolith application.
- Spring Boot application entrypoint and minimal configuration.
- Actuator endpoints **`/actuator/health`** (liveness) and **`/actuator/health/readiness`** per **1a** `api-contract.md`; **restrict** HTTP exposure to **health** and **readiness** only; **repository root `README.md`** documents the policy and satisfies **`api-contract.md`** *per README* **when this story is implemented** (same PR / delivery as code).
- Documented local run prerequisites (JDK version, Maven) and run/test commands for **bare JVM** execution **in that README update** (not ahead of implementation).

## 6. Out of Scope

- Incident REST APIs, **Flyway** / DB migrations (**[`story-2-1a-flyway-baseline-schema.md`](story-2-1a-flyway-baseline-schema.md)**), domain entities, OpenAPI YAML (later stories).
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

- [x] Project builds with **Java 21** and **Spring Boot 3** using **Maven**.
- [x] Application starts locally; **`GET /actuator/health`** returns success when the process is up.
- [x] **`GET /actuator/health/readiness`** exists and behavior is **documented in root `README.md`** as part of this story’s implementation (including any interim state before DB wiring).
- [x] Actuator exposure matches **restricted** contract (health + readiness only).
- [x] **In the same implementation delivery as the code:** **root `README.md`** is updated with **prerequisites** (JDK 21, Maven), **bare JVM** build/run/test commands, **`GET /actuator/health`** / **`GET /actuator/health/readiness`**, **restricted** actuator exposure (health + readiness only), and **interim readiness** semantics before PostgreSQL is configured (**Story 2+**).
- [x] No `/api/v1/signal-ingest/*` and no incident APIs required for this story’s gate.

## 11. Test Requirements

- Integration-style test (`@SpringBootTest` or equivalent) that loads the Spring context and asserts **`GET /actuator/health`** and **`GET /actuator/health/readiness`** respond successfully for this story’s configuration (interim readiness semantics match **root `README.md`** updated in this story).
- Per **1a** `test-plan.md` spirit: no OpenTelemetry stack in default CI.

## 12. Files Expected to Change

- Root / module **`pom.xml`** (or parent POM), application **`src/main/java/**`** entrypoint, **`src/main/resources/application*.yml`**, test sources under **`src/test/java/**`**, and **repository root `README.md`** (updated **with** the scaffold in this story—not a separate spec-only edit).

## 13. Implementation Notes

- Prefer a single module layout consistent with **`docs/adr/0001-kickoff-tooling-testing-and-1a-scope.md`** (Maven).
- **`phase-1a-monolith-core/implementation-plan.md`** M1 lists Flyway alongside scaffold; this repo **splits M1**: **Story 1** = shell + Actuator only; **Flyway `V1`** is entirely **[`story-2-1a-flyway-baseline-schema.md`](story-2-1a-flyway-baseline-schema.md)**—do not add Flyway here.
- Defer readiness strictness until PostgreSQL + Flyway exist if that avoids misleading “UP” while DB is down; document the choice in **`README.md`** in the **same implementation pass** as the app.
- Do **not** satisfy **`api-contract.md`** *per README* by editing root **`README.md`** without the scaffold; README actuator/run sections are **part of this story’s implementation**, not pre-work.

## 14. Human Review Checklist

- [x] JDK and Spring versions match **1a** spec.
- [x] Actuator lock-down is acceptable for demo/security posture.
- [x] Interim readiness behavior is honest in docs.

## 15. Completion Notes

- **README:** Root [`README.md`](../../../../README.md) documents prerequisites (JDK 21, Maven), `mvn clean verify` / `mvn spring-boot:run`, restricted Actuator (health group only → liveness + readiness URLs), and **human-approved** interim readiness: `GET /actuator/health/readiness` returns **UP** before DB/Flyway; post–Story 2+ readiness will reflect DB per 1a contract.
- **Tests:** `ActuatorHealthTest` (`@SpringBootTest`, Surefire default `*Test` pattern) covers health, readiness, and non-exposure of `/actuator/env`. `mvn test` runs this class.
- **Build outputs:** `target/**` build artifacts were removed from version control; `.gitignore` already ignores `/target/`.
- **PR link:** _add on merge_
- **ADR updates:** none
