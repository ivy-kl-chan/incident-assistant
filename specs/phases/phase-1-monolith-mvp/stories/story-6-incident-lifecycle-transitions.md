# Story 6 — Incident lifecycle transitions

## 1. Status

Planned

## 2. Goal

Implement **`POST /api/v1/incidents/{id}/transitions`** with **`If-Match`**, valid **`to`** targets, and correct **HTTP** status codes per **1a** state machine.

## 3. User Value

Operators can promote a draft to open, close an incident, or cancel from draft or open states with an auditable optional reason.

## 4. Spec References

| Document | Relevance |
|----------|-----------|
| [`../../phase-1a-monolith-core/api-contract.md`](../../phase-1a-monolith-core/api-contract.md) | Transitions body, allowed **`to`** values, errors **400/404/409/412** |
| [`../../phase-1a-monolith-core/spec.md`](../../phase-1a-monolith-core/spec.md) | Canonical state machine |
| [`../../phase-1a-monolith-core/test-plan.md`](../../phase-1a-monolith-core/test-plan.md) | Transition matrix unit tests; **412** on transitions |

## 5. In Scope

- **`POST /api/v1/incidents/{id}/transitions`** with **`If-Match`** required.
- Body: **`{ "to": "OPEN" | "CLOSED" | "CANCELLED", "reason"?: string ≤500 }`**.
- Allowed transitions: **`DRAFT`→`OPEN`/`CANCELLED`**; **`OPEN`→`CLOSED`/`CANCELLED`**; none from **`CLOSED`/`CANCELLED`**.
- **404** if id missing; **409** if transition illegal from current status; **412** if **`If-Match`** stale; **400** for bad JSON, wrong enum, missing **`to`**, **`reason`** too long.
- Persist **`transitionReason`** (or equivalent “last transition reason”) on **`Incident`** read model per **1a** contract.

## 6. Out of Scope

- **Automatic** transitions from signals (**1b**).
- **Problem Details** completeness (Story 7).
- **AI**, **RAG**, **MCP**, **Docker**, **Kubernetes**, **microservices**.

## 7. API Changes

- **New:** `POST /api/v1/incidents/{id}/transitions`.

## 8. Data Model Changes

Optional column for last transition reason if not already in **V1**—should be covered by **1a** `Incident` **`transitionReason`** (verify **V1** DDL vs contract; adjust Story **2** if discovery shows gap—human gate).

## 9. Business Rules

- Same optimistic concurrency as **PATCH**: **`ETag`**/**`version`** drives **`If-Match`**.
- Terminal states cannot transition further in v1.

## 10. Acceptance Criteria

- [ ] Each allowed transition returns success with updated **`status`**, **`version`**, **`ETag`**, and timestamps consistent with persistence.
- [ ] Illegal transition → **409**; unknown id → **404**; stale **`If-Match`** → **412**; malformed body → **400**.
- [ ] **`reason`** optional; length validation enforced.

## 11. Test Requirements

- Unit tests: full transition matrix including illegal paths.
- Integration tests: **`POST`→`OPEN`→`CLOSED`** with **`If-Match`** updates.

## 12. Files Expected to Change

- **`src/main/java/**`** domain/service/controller; **`src/test/java/**`**.

## 13. Implementation Notes

- If **`transitionReason`** is missing from **V1**, amend Story **2** DDL before merging this story (schema correction is not “new feature migration” if done before first release).

## 14. Human Review Checklist

- [ ] Matrix matches **1a** spec table exactly.
- [ ] **`If-Match`** parity with **PATCH** behavior.

## 15. Completion Notes

*(Fill when implemented.)*
