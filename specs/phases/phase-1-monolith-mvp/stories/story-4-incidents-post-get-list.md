# Story 4 — Incidents: create, read by id, list with pagination

## 1. Status

Planned

## 2. Goal

Expose **`POST /api/v1/incidents`**, **`GET /api/v1/incidents/{id}`**, and **`GET /api/v1/incidents`** per **1a** contract: create returns **201** + full body (**`DRAFT` only**), get returns **404** when missing, list is paginated with allowed filters/sort and **`MANUAL`**-only semantics for **1a**.

## 3. User Value

Users can create draft incidents, open a single incident, and browse a paginated catalog with predictable query validation.

## 4. Spec References

| Document | Relevance |
|----------|-----------|
| [`../../phase-1a-monolith-core/api-contract.md`](../../phase-1a-monolith-core/api-contract.md) | **POST**, **GET by id**, **GET list**; pagination; unknown query key **400**; no **`source`** query in **1a** |
| [`../../phase-1a-monolith-core/spec.md`](../../phase-1a-monolith-core/spec.md) | **1a** list returns manual only |
| [`../../phase-1a-monolith-core/test-plan.md`](../../phase-1a-monolith-core/test-plan.md) | List pagination, unknown query **400**, **GET** unknown **404** |

## 5. In Scope

- **`POST /api/v1/incidents`**: required **`title`**, **`severity`**; optional **`description`**; response **201** + **`Incident`** body (**1a** shape without **1b** fields).
- **`GET /api/v1/incidents/{id}`**: **404** if not found; response body per **1a** `Incident` table; **`ETag`** header per **1a** optimistic concurrency (RFC 9110 quoted opaque string from **`version`**—implement in this story or Story 5 before **1a** gate).
- **`GET /api/v1/incidents`**: paging **`page`**, **`size`**; filters **`status`** (comma-separated), **`sort`** (`createdAt,asc|desc`); **`items`**, **`page`**, **`size`**, **`totalElements`**, **`totalPages`**.
- **`X-Request-Id`**: echo or generate per contract.
- **`IncidentSummary`** / **`Incident`** field set for **1a**; **`source`** always **`MANUAL`** on wire for **1a** rows.
- Request size limits for **POST** per contract (max **1 MiB** → **413** or **400** as documented).

## 6. Out of Scope

- **`PATCH`**, **`POST .../transitions`**, **`If-Match` / `ETag`** (Story 5–6).
- RFC 7807 **Problem Details** for all error paths (Story 7; may return minimal errors until then if team sequences Story 7 first—prefer completing Problem Details before calling Story 4 “done” for external reviewers).
- **`source`** query parameter (**1b** extension).
- **Signal ingest** routes.
- **AI**, **RAG**, **MCP**, **Docker**, **Kubernetes**, **microservices**.

## 7. API Changes

- **New:** `POST /api/v1/incidents`, `GET /api/v1/incidents`, `GET /api/v1/incidents/{id}` as per **1a** `api-contract.md`.

## 8. Data Model Changes

None (uses Story **2–3**).

## 9. Business Rules

- **Create** always **`DRAFT`**; reject any client attempt to set **`OPEN`** on create (no such field in **1a** contract—ensure server does not allow bypass via future DTO fields).
- **List** in **1a**: only **`MANUAL`** rows; **`source`** on each item **`MANUAL`**.
- **Query validation**: unknown keys (including **`source`**) → **400**; bad **`status`** tokens → **400**; trim **`status`** tokens; reject duplicates/empty after trim.

## 10. Acceptance Criteria

- [ ] **201** create + **GET** round-trip for **1a** fields.
- [ ] **GET** unknown id → **404**.
- [ ] List pagination defaults and caps per contract (**`size`** max **100**); bad **`page`/`size`** → **400**.
- [ ] Unknown query parameter → **400** (explicit test for **`source`** if present in request).
- [ ] **`X-Request-Id`** behavior implemented.
- [ ] Wrong or missing **`Content-Type`** for JSON bodies → **415** or **400** per server-wide documented policy.

## 11. Test Requirements

- API integration tests (MockMvc/WebTestClient) with **PostgreSQL** via **Testcontainers** per **1a** `test-plan.md`.
- Cases: happy list, pagination edge, unknown query key, unknown id.

## 12. Files Expected to Change

- **`src/main/java/**`** web/controllers, DTOs, services; **`src/test/java/**`** integration tests; configuration for max request size if enforced at container.

## 13. Implementation Notes

- **`ETag`**: Story 5 adds **`ETag`** on **GET by id**; this story may omit **`ETag`** only if explicitly deferred—**acceptance criteria** for Phase 1a gate require **GET** **ETag**; coordinate ordering so **GET by id** includes **`ETag`** before closing **1a** (either implement **`ETag`** here or immediately in Story 5 in same release).

## 14. Human Review Checklist

- [ ] Response JSON matches **1a** shapes (no **1b** fields).
- [ ] List filter semantics match **1a** (manual-only).

## 15. Completion Notes

*(Fill when implemented.)*
