# Story 8: Phase 1a — Container packaging: image and local compose

*Label: Docker & Compose*

## 1. Status

Planned

## 2. Goal

Satisfy **Phase 1a** acceptance criteria that require a **runnable container image** and a **documented compose path** for **application + PostgreSQL**, aligned with **`phase-1a-monolith-core/implementation-plan.md`** M5.

## 3. User Value

Reviewers can reproduce the **1a** stack on a clean machine using the same paths documented in **`specs/03-acceptance-criteria.md`**, without relying only on bare JVM instructions.

## 4. Spec References

| Document | Relevance |
|----------|-----------|
| [`../../../03-acceptance-criteria.md`](../../../03-acceptance-criteria.md) | Phase **1a** DoD: image, compose, ports, env, health |
| [`../../phase-1a-monolith-core/implementation-plan.md`](../../phase-1a-monolith-core/implementation-plan.md) | M5 Dockerfile + compose app + DB |
| [`../../phase-1a-monolith-core/api-contract.md`](../../phase-1a-monolith-core/api-contract.md) | Health/readiness for probe wiring |
| [`../../phase-1a-monolith-core/spec.md`](../../phase-1a-monolith-core/spec.md) | PostgreSQL baseline; logs-only **1a** |
| [`../../phase-1a-monolith-core/test-plan.md`](../../phase-1a-monolith-core/test-plan.md) | Policy B; default CI unchanged |

## 5. In Scope

- **`Dockerfile`** (or agreed multi-stage equivalent) producing a **runnable** image for the monolith per ADR **`docs/adr/0001-kickoff-tooling-testing-and-1a-scope.md`**.
- **`docker compose`** (or equivalent) manifest starting **app + database** with **documented** ports, environment variables, and health/readiness checks wired to **`api-contract.md`** endpoints.
- **README** (project root): **bare-metal and container** run paths, prerequisites, and test commands—coordinate with **Story 9** (bare JVM quickstart + tests) so overlapping sections stay consistent.
- Honest documentation of **resource expectations** and **non-root** / security basics if applied (per acceptance spirit; exact posture per team ADR).

## 6. Out of Scope

- **Signal ingest**, rules, **1b** OpenAPI surfaces, and **1b** follow-on (**Stories 10–17**; see [`README.md`](README.md)).
- **Generative AI**, **RAG**, **MCP** tooling.
- **Multi-service** extraction, **Kubernetes** manifests, production-grade **registry** push CI (optional nightly may be Phase **5** per roadmap—here only what **1a** DoD requires).
- **OpenTelemetry Demo** and **1b** webhook walkthroughs (**Story 15**).

## 7. API Changes

None beyond ensuring **health/readiness** URLs remain stable for compose health checks (already specified in **1a** contract).

## 8. Data Model Changes

None (compose may **mount** or **seed** data only if spec allows; default greenfield uses Flyway from Story **2**).

## 9. Business Rules

- Compose defaults must not **enable** **`signals.enabled`** or register ingest unless the team explicitly documents a **1b** profile (prefer separate compose override file for **1b** in Story **15**).

## 10. Acceptance Criteria

- [ ] **`specs/03-acceptance-criteria.md`** Phase **1a** bullets for **Dockerfile** and **compose (app + DB)** are satisfied with **documented** commands.
- [ ] Image builds and container stack starts; **health** (and **readiness** when DB up) succeed per **`phase-1a-monolith-core/api-contract.md`**.
- [ ] README lists **both** bare JVM and container quickstarts without contradicting **`test-plan.md`** Policy B.
- [ ] No **`POST /api/v1/signal-ingest/*`** route required for this story’s smoke path (**1a** boundary preserved).

## 11. Test Requirements

- Default **PR CI** remains free of mandatory full-compose runs unless **`test-plan.md`** explicitly requires it; if compose tests are optional, document as **manual** or **nightly** checklist.
- If automated compose smoke exists, it must be **deterministic** and not depend on external registries without pinning.

## 12. Files Expected to Change

- **`Dockerfile`**, **`docker-compose*.yml`** (or `compose.yaml`), **`README.md`**, optional **`.dockerignore`**, CI workflow only if required by **`test-plan.md`**.

## 13. Implementation Notes

- **Depends on:** Stories **1–7** (functional **1a** API + OpenAPI **1a** baseline); may proceed in parallel with final **1a** polish in Story **9** if health/DB wiring already stable—coordinate to avoid duplicate README edits.
- Prefer **one** merged compose file for **1a** or a **base** file extended by **Story 15** for demo profiles (**ADR 0002** alignment).
- **`specs/03-acceptance-criteria.md`** Phase **1a** DoD for image/compose is satisfied together with Story **9** (readiness, no ingest); neither story alone closes **1a**.

## 14. Human Review Checklist

- [ ] Secrets: no tokens committed; **`SIGNAL_INGEST_TOKEN`** absent or placeholder until **1b** stories.
- [ ] Ports and env names match **`api-contract.md`** and **`data-model.md`** connection settings.
- [ ] Image size and build time acceptable for demo maintainers.

## 15. Completion Notes

*(Fill when implemented: image tags, compose revision pins, PR link.)*
