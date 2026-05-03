# Phase 1 MVP — implementation stories

Stories live here as `story-*.md` (see [phase README](../README.md) for **1a** / **1b** spec locations). Implement **1a** stories in numeric order unless a story explicitly depends on another.

## Flyway baseline ownership

The single baseline migration **`V1__baseline_incidents_and_signal_tables.sql`** is the **Story 2** deliverable:

- Path: [`../../../../src/main/resources/db/migration/V1__baseline_incidents_and_signal_tables.sql`](../../../../src/main/resources/db/migration/V1__baseline_incidents_and_signal_tables.sql)
- Spec: [`story-2-1a-flyway-baseline-schema.md`](story-2-1a-flyway-baseline-schema.md)

Later stories (for example **Story 3** — domain and manual persistence) **consume** that schema; they do **not** introduce a competing baseline or redefine **`V1`** ownership.

## CI and Testcontainers

Default **GitHub Actions** runs **`mvn --batch-mode verify`** on **`ubuntu-latest`** where **Docker** is available to the runner, so JUnit tests using **Testcontainers** (PostgreSQL, Flyway apply-on-startup) **run** instead of being skipped. Workflow: [`.github/workflows/ci.yml`](../../../../.github/workflows/ci.yml). Repository root [README](../../../../README.md) documents the same expectation.
