# Story 18: Phase 1a — PR CI with Docker for Testcontainers

*Label: CI & Testcontainers*

## 1. Status

Reviewed

## 2. Goal

Add **default continuous integration** (**GitHub Actions**, checked into this repository) that runs on **`pull_request`** and **`push`** to **`main`**, running **`mvn --batch-mode verify`** on a **Java 21** toolchain where the **Docker API is available to the build**, so **JUnit / Testcontainers** integration tests—including **`FlywayV1BaselineIntegrationTest`**—**execute** instead of being **skipped** via `@Testcontainers(disabledWithoutDocker = true)`.

## 3. User Value

Every merged change is validated against the same **PostgreSQL + Flyway** path the specs call normative (**ADR 0001**, **1a** `test-plan.md`), without relying on contributors to notice skipped tests locally.

## 4. Spec References

| Document | Relevance |
|----------|-----------|
| [`../README.md`](../README.md) | Implement **1a** before **1b**; stories live under `stories/` |
| [`../../phase-1a-monolith-core/test-plan.md`](../../phase-1a-monolith-core/test-plan.md) | **Testcontainers PostgreSQL** default for **1a** integration tests in CI |
| [`../../../../docs/adr/0001-kickoff-tooling-testing-and-1a-scope.md`](../../../../docs/adr/0001-kickoff-tooling-testing-and-1a-scope.md) | **Testcontainers from day one**; Maven; JDK **21** |
| [`../../../03-acceptance-criteria.md`](../../../03-acceptance-criteria.md) | Phase **1a**: build and test expectations |
| [`../../../../README.md`](../../../../README.md) | Notes that integration tests need Docker when not skipped |
| [`story-9-1a-gate-readiness-no-ingest-route.md`](story-9-1a-gate-readiness-no-ingest-route.md) | Gate: **1a** `test-plan.md` green in default CI with Testcontainers — depends on this story for a real runner |
| [`../../phase-1a-monolith-core/spec.md`](../../phase-1a-monolith-core/spec.md) | **1a** product normative home; this story adds **no** API or persistence behavior—only CI that honors **`test-plan.md`** tooling |

## 5. In Scope

- **GitHub Actions** as the **committed default** CI host: **one** primary workflow under **`.github/workflows/*.yml`**.
- Triggers: **`pull_request`** and **`push`** to the **default branch** (**`main`**). Same job definition for both (no separate “PR-only” vs “main-only” behavior unless required later by ADR).
- Job runs on an environment where **Testcontainers can reach a Docker daemon** (typical: **`runs-on: ubuntu-latest`** with preinstalled Docker; **not** a nested `container:` job image that hides the socket unless **Docker-in-Docker** or an equivalent is explicitly configured).
- **`mvn --batch-mode verify`** using **JDK 21**.
- **README** (repository root): states that **default CI** (PRs + pushes to **`main`**) requires **Docker** for full Testcontainers parity; names the workflow file path.
- **Fork PRs:** no special org policy—standard **`pull_request`** workflows apply (no **`pull_request_target`** requirement for this story).

## 6. Out of Scope

- **Story 8** deliverables: **Dockerfile**, **`docker compose`** smoke, or **mandatory** full-stack compose in PR CI (**Policy B** remains per **`test-plan.md`**).
- **Testcontainers Cloud** or other paid hosted Docker bridges (optional follow-up ADR or later story).
- **1b** ingest tests, **OpenTelemetry** stacks, or **Spectral** / OpenAPI gates unless a separate story adds them.
- Changing **`@Testcontainers(disabledWithoutDocker = true)`** to fail instead of skip (different product decision; not required here).

## 7. API Changes

None.

## 8. Data Model Changes

None.

## 9. Business Rules

- **Default CI** (PRs and pushes to **`main`**) must not be “green” while **normative** Testcontainers-backed tests are **silently skipped** because Docker is missing; the workflow configuration is responsible for providing Docker (or the story is not complete).
- **GitHub Actions** workflows under **`.github/workflows/`** are the **source of truth** for what “default CI” means for this repo.

## 10. Acceptance Criteria

- [x] A **committed** **GitHub Actions** workflow runs on **`pull_request`** and on **`push`** to the **`main`** branch (same job).
- [x] The job uses **JDK 21** and runs **`mvn --batch-mode verify`** successfully.
- [ ] **`FlywayV1BaselineIntegrationTest`** appears in **Surefire** output as **run** (not **skipped**) in a typical CI run, proving Testcontainers started **PostgreSQL** (requires outbound image pull, e.g. **`postgres:16-alpine`**, unless a runner mirror is configured and documented). **Deferred:** this test class is **not** in the repository yet (see **§15**); CI provides Docker so it will **run** once added.
- [x] **README** documents: Docker required for full local/CI parity with Testcontainers; path to **`.github/workflows/…`** file(s); that **PRs and `main` pushes** run this workflow.

## 11. Test Requirements

- [x] **No new Java unit tests required** for this story; evidence is **CI configuration + green `mvn verify` + Surefire report** showing integration tests **executed** when present (`FlywayV1BaselineIntegrationTest` pending—see **§15**).
- [x] Implementer attaches or references (in **§15 Completion Notes**) one **sample CI log excerpt** or **CI run URL** showing the test class **ran** (satisfies human review). **Local Surefire excerpt** below for current suite; **GitHub Actions** URL after first push with workflow.

## 12. Files Expected to Change

- **`.github/workflows/*.yml`** (GitHub Actions; one primary workflow is enough).
- **`README.md`** (CI + Docker expectations for Testcontainers).

## 13. Implementation Notes

- **Human-approved defaults (pre-implementation):** **GitHub Actions**; triggers **`pull_request`** + **`push`** to **`main`**; **no** special fork / **`pull_request_target`** policy beyond ordinary **`pull_request`**.
- **`runs-on: ubuntu-latest`** usually exposes Docker suitable for Testcontainers; avoid `container:` images for the **Maven** step unless **Docker socket** or **DinD** is explicitly solved.
- **Caching:** optional **Maven** dependency cache for faster PR feedback.
- **Ordering:** implement and merge **before** treating **Story 9** gate acceptance “Testcontainers green in default CI” as satisfied.

## 14. Human Review Checklist

- [x] Scope matches story
- [x] No future story implemented
- [x] Tests are meaningful (CI + Surefire evidence for this story; **AC3** deferred per §15)
- [x] Public API matches spec (N/A)
- [x] README ships **with** the workflow in the same delivery

## 15. Completion Notes

- **2026-04-30:** Added **[`.github/workflows/ci.yml`](../../../../.github/workflows/ci.yml)** (`pull_request` → **`main`**, **`push`** → **`main`**): **`ubuntu-latest`**, **`actions/setup-java@v4`** Temurin **21**, Maven cache, **`mvn --batch-mode verify`**. No nested `container:` step—hosted Docker usable by Testcontainers.
- **README:** [**Continuous integration**](../../../../README.md#continuous-integration) section + prerequisites (**Docker** / Testcontainers).
- **Local verification:** `mvn --batch-mode verify` → **BUILD SUCCESS** (Surefire: **`ActuatorHealthTest`** 3 tests run, 0 skipped). Example excerpt:

  ```
  [INFO] Running com.incidentassistant.ActuatorHealthTest
  ...
  [INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
  ```

- **AC3 / `FlywayV1BaselineIntegrationTest`:** Not present in this repository revision (only **`ActuatorHealthTest`**). Hosted runners expose Docker; when **`FlywayV1BaselineIntegrationTest`** lands (Flyway baseline story), expect Surefire to list it as **run**, not **skipped**. **Human-approved:** deferring **AC3** closure until that test exists is **accepted** for Story 18’s delivered scope; tick **§10 AC3** and attach CI proof when Flyway baseline merges.
- **`review-story-implementation` follow-up (human):** **(1)** AC3 deferral — **yes**, accepted. **(2)** Maintainer will **append** a **GitHub Actions** run URL (or Surefire excerpt from Actions logs) to **§15** after the first successful **`push`** / workflow run on **`main`**.
- **Human `Complete`:** optional — mark **`Complete`** after merge when satisfied; **`Complete`** is not blocked on AC3 if the deferral above stands. Paste Actions URL per (2) when available; finish **AC3** when **`FlywayV1BaselineIntegrationTest`** is in **`main`** and CI shows it **ran**.
- **GitHub Actions URL (placeholder — fill after first green run on `main`):** _(pending)_
