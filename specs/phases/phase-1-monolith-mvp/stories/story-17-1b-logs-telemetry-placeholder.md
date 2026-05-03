# Story 17: Phase 1b — Logs telemetry slice (**1b-L**) *(placeholder)*

*Label: Placeholder — logs (**1b-L**)*

## 1. Status

Planned

## 2. Goal

Reserve an explicit backlog slot for **1b-L** (**logs**): extend rules, fixtures, tests, and README so **log**-driven evaluations and **`telemetryPointers`** align with **`phase-1b-signal-ingest/implementation-plan.md`** (B7+ / **1b-L** row)—**after** **1b-T** (Story **16**) unless a human approves parallelizing.

## 3. User Value

When implemented, **Journey A** can demonstrate **log**-grounded abnormality detection without overloading **Story 15** or **Story 11**.

## 4. Spec References

| Document | Relevance |
|----------|-----------|
| [`../../phase-1b-signal-ingest/implementation-plan.md`](../../phase-1b-signal-ingest/implementation-plan.md) | **1b-L** row |
| [`../../phase-1b-signal-ingest/spec.md`](../../phase-1b-signal-ingest/spec.md) | Incremental telemetry ordering |
| [`docs/adr/0002-phase-1b-webhook-and-incremental-telemetry.md`](../../../../docs/adr/0002-phase-1b-webhook-and-incremental-telemetry.md) | Demo narrative |
| [`../../phase-1b-signal-ingest/api-contract.md`](../../phase-1b-signal-ingest/api-contract.md) | Validation limits for nested telemetry |
| [`../README.md`](../README.md) | **Stories** index |

## 5. Prerequisites, dependencies, and blocked by

- **Placeholders split from [Story 16](story-16-1b-traces-telemetry-placeholder.md)** per **§7**; full **1b** core (**Stories 10+**) should be stable before activation.

## 6. In Scope

- None until this placeholder is promoted to an active story.

## 7. Out of Scope

- Implementation while this file remains a placeholder.
- **1b-T** scope (**Story 16**).

## 8. API Changes

Deferred—define when story is activated.

## 9. Data Model Changes

None as placeholder.

## 10. Business Rules

- Must not regress **1b-M**/**1b-T** ingest or read contracts.

## 11. Acceptance Criteria

- [ ] *(When activated)* **`implementation-plan.md`** **1b-L** row satisfied with tests + README updates (or explicit deferral with issue link).

## 12. Test Requirements

- [ ] *(When activated)* Tests per **`phase-1b-signal-ingest/test-plan.md`** additions for log-centric fixtures.

## 13. Files Expected to Change

- *(When activated)* Rules, **`README.md`**, tests, optional OpenAPI examples.

## 14. Implementation Notes

- **Depends on:** Story **16** unless ADR documents parallel **1b-T**/**1b-L** delivery.

## 15. Human Review Checklist

- [ ] Placeholder removed from title before implementation starts.

## 16. Completion Notes

*(Placeholder — fill when story is activated or cancelled.)*
