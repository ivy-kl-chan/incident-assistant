# Story 6: Phase 1a — Incident lifecycle transitions

*Label: Lifecycle transitions*

## 1. Status

Planned

## 2. Goal

Implement **`POST /api/v1/incidents/{id}/transitions`** with **`If-Match`**, valid **`to`** targets, and correct **HTTP** status codes per **1a** state machine.

## 3. User Value

Operators can promote a draft to open, close an incident, or cancel from draft or open states; optional **`reason`** on the request is validated per **`api-contract.md`** (**1a** does **not** persist **`transitionReason`** on **`GET`**—always **`null`**).

## 4. Spec References

| Document | Relevance |
|----------|-----------|
| [`../../phase-1a-monolith-core/api-contract.md`](../../phase-1a-monolith-core/api-contract.md) | Transitions body, allowed **`to`** values, errors **400/404/409/412**; **`transitionReason`** on **`GET`** (**1a**: **`null`**) |
| [`../../phase-1a-monolith-core/data-model.md`](../../phase-1a-monolith-core/data-model.md) | **`transitionReason`** not an **`incidents`** column in **`V1`** |
| [`../../phase-1a-monolith-core/spec.md`](../../phase-1a-monolith-core/spec.md) | Canonical state machine |
| [`../../phase-1a-monolith-core/test-plan.md`](../../phase-1a-monolith-core/test-plan.md) | Transition matrix unit tests; **412** on transitions |

## 5. In Scope

- **`POST /api/v1/incidents/{id}/transitions`** with **`If-Match`** required.
- Body: **`{ "to": "OPEN" | "CLOSED" | "CANCELLED", "reason"?: string ≤500 }`**.
- Allowed transitions: **`DRAFT`→`OPEN`/`CANCELLED`**; **`OPEN`→`CLOSED`/`CANCELLED`**; none from **`CLOSED`/`CANCELLED`**.
- **404** if id missing; **409** if transition illegal from current status; **412** if **`If-Match`** stale; **400** for bad JSON, wrong enum, missing **`to`**, **`reason`** too long.
- **`transitionReason`** on **`GET /api/v1/incidents/{id}`** remains **`null`** in **1a** (**not** persisted on **`incidents`** per **`../../phase-1a-monolith-core/api-contract.md`** and **`../../phase-1a-monolith-core/data-model.md`**). Validate optional body **`reason`** (length, presence rules) only; no schema column for it in **1a**.

## 6. Out of Scope

- **Automatic** transitions from signals (**1b**).
- **Problem Details** completeness (Story 7).
- **AI**, **RAG**, **MCP**, **Docker**, **Kubernetes**, **microservices**.

## 7. API Changes

- **New:** `POST /api/v1/incidents/{id}/transitions`.

## 8. Data Model Changes

**None** for **1a**: **`transitionReason`** is a **wire-only** field on **`Incident`** responses; **`1a`** always returns **`null`**. A future schema/API change may add persistence; until then, no **`V1`**/`Story 2` DDL change for this field.

## 9. Business Rules

- Same optimistic concurrency as **PATCH**: **`ETag`**/**`version`** drives **`If-Match`**.
- Terminal states cannot transition further in v1.

## 10. Acceptance Criteria

- [ ] Each allowed transition returns success with updated **`status`**, **`version`**, **`ETag`**, and timestamps consistent with persistence.
- [ ] Illegal transition → **409**; unknown id → **404**; stale **`If-Match`** → **412**; malformed body → **400**.
- [ ] **`reason`** optional; length validation enforced.
- [ ] **`GET`** after a successful transition still returns **`transitionReason`: `null`** (**1a** contract — not stored).

## 11. Test Requirements

- Unit tests: full transition matrix including illegal paths.
- Integration tests: **`POST`→`OPEN`→`CLOSED`** with **`If-Match`** updates.

## 12. Files Expected to Change

- **`src/main/java/**`** domain/service/controller; **`src/test/java/**`**.

## 13. Implementation Notes

- Do **not** add an **`incidents`** column for **`transitionReason`** in **1a**; align **`GET`** payloads with **`api-contract.md`**. Logging or audit of **`reason`** at the application layer is optional and out of scope unless a later story/spec adds it.

## 14. Human Review Checklist

- [ ] Matrix matches **1a** spec table exactly.
- [ ] **`If-Match`** parity with **PATCH** behavior.
- [ ] **`transitionReason`** on **`GET`** matches **1a** contract (**`null`**); no **`V1`** column added for it.

## 15. Completion Notes

*(Fill when implemented.)*
