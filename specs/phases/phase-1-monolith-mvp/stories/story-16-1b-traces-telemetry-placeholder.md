# Story 16: Phase 1b — Traces telemetry slice (**1b-T**) *(placeholder)*

*Label: Placeholder — traces (**1b-T**)*

## 1. Status

Planned

## 2. Goal

Reserve an explicit backlog slot for **1b-T** (**traces**): extend **`telemetryPointers`**, rules, fixtures, **`phase-1b-signal-ingest/test-plan.md`** coverage, and README Journey A narrative so **trace**-centric signals are first-class **after** **1b-M** (Story **11**) is stable—per **`phase-1b-signal-ingest/implementation-plan.md`** (B7+ / **1b-T** row) and **`docs/adr/0002-phase-1b-webhook-and-incremental-telemetry.md`**.

## 3. User Value

When implemented, reviewers can validate **Journey A** with **trace**-grounded evaluations without expanding scope inside Stories **11** or **15**.

## 4. Spec References

| Document | Relevance |
|----------|-----------|
| [`../../phase-1b-signal-ingest/implementation-plan.md`](../../phase-1b-signal-ingest/implementation-plan.md) | **1b-T** row: traces path after **1b-M** |
| [`../../phase-1b-signal-ingest/spec.md`](../../phase-1b-signal-ingest/spec.md) | Incremental telemetry ordering |
| [`docs/adr/0002-phase-1b-webhook-and-incremental-telemetry.md`](../../../../docs/adr/0002-phase-1b-webhook-and-incremental-telemetry.md) | Demo narrative, webhook shape |
| [`../../phase-1b-signal-ingest/api-contract.md`](../../phase-1b-signal-ingest/api-contract.md) | **`telemetryPointers`** and validation (extend only as spec evolves) |
| [`../README.md`](../README.md) | **Stories** index |

## 5. In Scope

- None until this placeholder is promoted to an active story (replace *placeholder* in title, narrow **In Scope**, and link a PR).

## 6. Out of Scope

- All implementation work while status remains an unchecked placeholder (use **Stories 11** and **15** for **1b-M** delivery first).
- **1b-L** (**Story 17**).

## 7. API Changes

Deferred—define when story is activated (must remain compatible with **1b-M** contracts from Story **11**).

## 8. Data Model Changes

None as placeholder; any DDL requires spec + ADR update when activated.

## 9. Business Rules

- Must not break **1b-M** dedup, idempotency, or ingest auth semantics established in Stories **10–12**.

## 10. Acceptance Criteria

- [ ] *(When activated)* **`implementation-plan.md`** **1b-T** row satisfied with automated tests and README subsection for trace-driven Journey A (or explicit deferral with issue link).

## 11. Test Requirements

- [ ] *(When activated)* Integration/unit tests per updated **`phase-1b-signal-ingest/test-plan.md`** for trace-heavy payloads.

## 12. Files Expected to Change

- *(When activated)* Rules, fixtures, **`README.md`**, **`src/main/java/**`**, **`src/test/java/**`**, possibly **`openapi-1b.yaml`** examples.

## 13. Implementation Notes

- **Depends on:** Stories **10–15** minimum; prefer completing **Story 14** (**1b** automated gate) for **1b-M** before expanding evaluators for traces.
- Split from **Story 11** intentionally to keep **1b-M** PR-sized; do not fold **1b-T** into **Story 11** without human approval.

## 14. Human Review Checklist

- [ ] Placeholder removed from title before implementation starts.
- [ ] Specs updated before code if **1b-T** changes contract shapes.

## 15. Completion Notes

*(Placeholder — fill when story is activated or cancelled.)*
