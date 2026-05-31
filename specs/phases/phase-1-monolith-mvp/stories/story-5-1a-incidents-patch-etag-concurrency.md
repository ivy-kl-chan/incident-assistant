# Story 5: Phase 1a — Incidents: PATCH, ETag, and optimistic concurrency

*Label: Optimistic concurrency*

## 1. Status

Implemented

## 2. Goal

Implement `**PATCH /api/v1/incidents/{id}`** with required `**If-Match`**, strong `**ETag*`* on `**GET /api/v1/incidents/{id}**`, and `**412**` on stale concurrency—matching **1a** `api-contract.md`.

## 3. User Value

Clients can safely edit draft or open incidents without silently overwriting concurrent changes.

## 4. Spec References


| Document                                                                                       | Relevance                                                                                                                            |
| ---------------------------------------------------------------------------------------------- | ------------------------------------------------------------------------------------------------------------------------------------ |
| `[../../phase-1a-monolith-core/api-contract.md](../../phase-1a-monolith-core/api-contract.md)` | `**ETag`** format (`"` + decimal `version` + `"`**); `**If-Match`** required on `**PATCH**`; **400** rules for empty/non-object body |
| `[../../phase-1a-monolith-core/test-plan.md](../../phase-1a-monolith-core/test-plan.md)`       | **412** missing/wrong `**If-Match`** for `**PATCH`**                                                                                 |
| `[../../../03-acceptance-criteria.md](../../../03-acceptance-criteria.md)`                     | Phase **1a**: optimistic concurrency on **GET**/**PATCH**; `**openapi-1a.yaml`** aligned with contract                               |
| `[../../../openapi/openapi-1a.yaml](../../../openapi/openapi-1a.yaml)`                         | `**ETag`** on `**GET /{id}`** (required when implemented); `**PATCH**` parameters/responses                                          |


## 5. Prerequisites, dependencies, and blocked by

- **[Story 4](story-4-1a-incidents-post-get-list.md)** — `**POST` / list / `GET` by id** for incidents must exist; `**PATCH`** and `**ETag`** extend that surface and the existing `**version`** column.

## 6. In Scope

- `**GET /api/v1/incidents/{id}**` returns `**ETag**` derived from `**version**` (normative wire example `**ETag: "7"**`).
- `**PATCH /api/v1/incidents/{id}**`:
  - Requires `**If-Match**` matching current `**ETag**`; else **412**.
  - JSON body: non-empty object with at least one of `**title`**, `**description`**, `**severity**`; otherwise **400**.
  - Edits allowed only when status is `**DRAFT`** or `**OPEN`**; else **409**.
- `**If-Match: *`** not supported unless an ADR says otherwise; treat as unsupported wildcard → `**412`** (same family as stale/mismatch precondition).
- Successful `**PATCH`** updates `**updated_at**` and increments `**version**`.

## 7. Out of Scope

- `**POST .../transitions**` (Story 6).
- **Problem Details** envelope for every error (Story 7—may share work).
- **Signal** fields and **1b** extensions.
- **AI**, **RAG**, **MCP**, **Docker**, **Kubernetes**, **microservices**.

## 8. API Changes

- **Extend:** `GET /api/v1/incidents/{id}` response headers with `**ETag`**.
- **New:** `PATCH /api/v1/incidents/{id}`.

## 9. Data Model Changes

None (uses `**version`** column already in **V1**).

## 10. Business Rules

- Optimistic locking is authoritative: mismatch `**If-Match`** → **412**, not silent merge.
- Patch fields validated with same bounds as create (**title**/**description**/**severity**).

## 11. Acceptance Criteria

- **GET** returns `**ETag`** consistent with persisted `**version`** after create and after patch.
- **PATCH** with correct `**If-Match`** succeeds; wrong/missing `**If-Match`** or `**If-Match: *`** → **412**.
- **PATCH** on `**CLOSED`**/`**CANCELLED`** → **409**.
- **PATCH** with empty object or no updatable keys → **400**.
- Wrong `**Content-Type`** / malformed JSON → **415**/**400** per documented policy.

## 12. Test Requirements

- Integration tests: **GET** then **PATCH** success; concurrent **PATCH** second writer gets **412**.
- Unit tests for `**If-Match`** parsing/compare if logic is isolated (include `**If-Match: *`** → **412**).

## 13. Files Expected to Change

- `**src/main/java/com/incidentassistant/web/incident/`** (e.g. `**IncidentController`**, `**If-Match`** / `**ETag**` helper if extracted); exception mapping as needed.
- `**src/test/java/****` integration and unit tests for this story’s scope.
- `**specs/openapi/openapi-1a.yaml**` — `**GET /api/v1/incidents/{id}**`: `**ETag**` response header required when this story ships; remove or tighten “Story 4 does not emit” placeholder text; keep `**PATCH**` contract aligned with `**api-contract.md**`.

## 14. Implementation Notes

- Ensure list summaries expose `version` for clients that cache `**ETag**` only from detail reads—contract already includes `**version**` on summaries.
- **OpenAPI** updates for **1a** are owned by this story (see **§13**); Phase **1a** DoD in `**specs/03-acceptance-criteria.md`** requires `**openapi-1a.yaml`** aligned with `**api-contract.md`** after `**ETag**`/`**PATCH**` behavior is real.

## 15. Human Review Checklist

- `**ETag**` format matches RFC and spec example.
- No `**PATCH**` allowed on terminal states.
- `**If-Match: ***` → **412**; `**openapi-1a.yaml`** reflects required `**ETag`** on `**GET /{id}`**.

## 16. Completion Notes

*(Fill when implemented.)*