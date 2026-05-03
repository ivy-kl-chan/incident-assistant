# Story 4: Phase 1a — Incidents: create, read by id, list with pagination

*Label: Incidents HTTP (create/list/get)*

## 1. Status

Approved

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

## 5. Prerequisites, dependencies, and blocked by

- **[Story 2](story-2-1a-flyway-baseline-schema.md)** and **[Story 3](story-3-1a-incident-domain-repository.md)** — manual incident persistence and schema (**see also §9**).
- **[Story 5](story-5-1a-incidents-patch-etag-concurrency.md)** — **`ETag` on `GET` by id** ships in Story **5** (**human decision**), not in Story **4**. Phase **1a** DoD (**`GET` returns `ETag`**) is satisfied when Story **5** completes.

## 6. In Scope

- **`POST /api/v1/incidents`**: required **`title`**, **`severity`**; optional **`description`**; response **201** + **`Incident`** body (**1a** shape without **1b** fields).
- **`GET /api/v1/incidents/{id}`**: **404** if not found; response body per **1a** `Incident` table (**no `ETag` on this route in Story 4** — **[Story 5](story-5-1a-incidents-patch-etag-concurrency.md)**).
- **`GET /api/v1/incidents`**: paging **`page`**, **`size`**; filters **`status`** (comma-separated), **`sort`** (`createdAt,asc|desc`); **`items`**, **`page`**, **`size`**, **`totalElements`**, **`totalPages`**.
- **`X-Request-Id`**: echo or generate per contract.
- **`IncidentSummary`** / **`Incident`** field set for **1a**; **`source`** always **`MANUAL`** on wire for **1a** rows.
- Request size limits for **POST** per contract (max **1 MiB** → **413** or **400** as documented).

## 7. Out of Scope

- **`PATCH`**, **`POST .../transitions`**, **`If-Match`** on mutating routes (Story **5–6**).
- **`ETag` response header on `GET /api/v1/incidents/{id}`** — **[Story 5](story-5-1a-incidents-patch-etag-concurrency.md)** (human decision; required for **1a** optimistic concurrency **via Story 5**, not Story **4**).
- RFC 7807 **Problem Details** for all error paths (**Story 7**). Until then: **minimal JSON errors** are acceptable (**human approval**); converge with **Story 7** when delivered.
- **`source`** query parameter (**1b** extension; see Story **13**).
- **Signal ingest** routes.
- **AI**, **RAG**, **MCP**, **Docker**, **Kubernetes**, **microservices**.

## 8. API Changes

- **New:** `POST /api/v1/incidents`, `GET /api/v1/incidents`, `GET /api/v1/incidents/{id}` as per **1a** `api-contract.md`.

## 9. Data Model Changes

None (uses Story **2–3**).

## 10. Business Rules

- **Create** always **`DRAFT`**; reject any client attempt to set **`OPEN`** on create (no such field in **1a** contract—ensure server does not allow bypass via future DTO fields).
- **List** in **1a**: only **`MANUAL`** rows; **`source`** on each item **`MANUAL`**.
- **Query validation**: unknown keys (including **`source`**) → **400**; bad **`status`** tokens → **400**; trim **`status`** tokens; reject duplicates/empty after trim.

## 11. Acceptance Criteria

- [ ] **201** create + **GET** round-trip for **1a** fields (**without** asserting **`ETag`** on **`GET` by id** — deferred to **Story 5**).
- [ ] **GET** unknown id → **404**.
- [ ] List pagination defaults and caps per contract (**`size`** max **100**); bad **`page`/`size`** → **400**.
- [ ] Unknown query parameter → **400** (explicit test for **`source`** if present in request).
- [ ] **`X-Request-Id`** behavior implemented.
- [ ] Wrong or missing **`Content-Type`** for JSON bodies → **415** or **400** per server-wide documented policy.
- [ ] **`503`** (preferred) or **`500`** when persistence is unavailable for **list** or **GET by id**, **or** behavior explicitly **deferred** and **documented** (README or story **§14**) with rationale aligned to **`api-contract.md`** availability clauses.

## 12. Test Requirements

- API integration tests (MockMvc/WebTestClient) with **PostgreSQL** via **Testcontainers** per **1a** `test-plan.md`.
- Cases: happy list, pagination edge, unknown query key, unknown id.
- **Persistence unavailable:** if **§11** implements **`503`/`500`**, add an integration test **or** a documented deferral in **§14** matching that choice (no silent gap vs **`api-contract.md`**).

## 13. Files Expected to Change

- **`src/main/java/**`** web/controllers, DTOs, services; **`src/test/java/**`** integration tests; configuration for max request size if enforced at container.

## 14. Implementation Notes

- **`ETag`**: **Not** implemented in Story **4**. **[Story 5](story-5-1a-incidents-patch-etag-concurrency.md)** adds **`ETag`** on **`GET` by id** per **`api-contract.md`** (RFC 9110 quoted opaque string from **`version`**). Phase **1a** gate (**`specs/03-acceptance-criteria.md`**) is satisfied once Story **5** ships **`ETag`**.
- **Availability (503 / 500):** Implement **`503`** (preferred) or **`500`** for list/get when persistence is unavailable, **or** record explicit deferral here/README so behavior matches **§11** and **`api-contract.md`** (no unspecified gap).
- **Errors until Story 7:** Minimal JSON bodies for validation and client errors are **in scope** for Story **4**; full RFC 7807 Problem Details remain **Story 7**.

## 15. Human Review Checklist

- [ ] Response JSON matches **1a** shapes (no **1b** fields).
- [ ] List filter semantics match **1a** (manual-only).

## 16. Completion Notes

*(Fill when implemented.)*
