# Story 7: Phase 1a — RFC 7807 Problem Details and OpenAPI 1a alignment

*Label: Errors & OpenAPI 1a*

## 1. Status

Planned

## 2. Goal

Standardize error responses as **RFC 7807** **`application/problem+json`** across **1a** incident endpoints and bring **`specs/openapi/openapi-1a.yaml`** into alignment with implemented behavior and **`phase-1a-monolith-core/api-contract.md`.

## 3. User Value

API consumers get consistent, machine-readable errors; reviewers can validate the HTTP surface against a checked-in OpenAPI artifact.

## 4. Spec References

| Document | Relevance |
|----------|-----------|
| [`../../phase-1a-monolith-core/api-contract.md`](../../phase-1a-monolith-core/api-contract.md) | Problem Details fields; status codes for incidents + availability (**503** preference) |
| [`../../phase-1a-monolith-core/spec.md`](../../phase-1a-monolith-core/spec.md) | OpenAPI artifact path |
| [`../../../openapi/openapi-1a.yaml`](../../../openapi/openapi-1a.yaml) | Normative **1a** OpenAPI (paths, **`ETag`**, **503** where applicable) |
| [`../../phase-1a-monolith-core/test-plan.md`](../../phase-1a-monolith-core/test-plan.md) | Error coverage matrix |
| [`../../../03-acceptance-criteria.md`](../../../03-acceptance-criteria.md) | **1a** gate: **`openapi-1a.yaml`** aligned |

## 5. In Scope

- Global exception handling producing **`type`**, **`title`**, **`status`**, **`detail`**, **`instance`** without stack traces in responses.
- Representative tests for **400**, **404**, **409**, **412**, **413**/oversize, **415**, **503** (where applicable per contract—e.g. persistence unavailable simulation).
- Update **`specs/openapi/openapi-1a.yaml`** for all **1a** incident paths + **`GET` by id** **`ETag`** + documented **503**/**503** policy consistency.
- Optional: Spectral lint for **`openapi-1a.yaml`** if already in repo policy.

## 6. Out of Scope

- **`openapi-1b.yaml`** (**1b** stories).
- **Signal ingest** paths.
- **AI**, **RAG**, **MCP**, **Docker**, **Kubernetes**, **microservices**.

## 7. API Changes

- **Behavioral:** error bodies normalized to Problem Details for **1a** resources (no path additions unless gaps found vs contract).

## 8. Data Model Changes

None.

## 9. Business Rules

- **503** vs **500** policy for DB down: pick one, document in README and OpenAPI, match tests.

## 10. Acceptance Criteria

- [ ] Incident API errors return **`application/problem+json`** with required fields.
- [ ] **`openapi-1a.yaml`** documents **1a** incident + actuator surfaces per **1a** spec (including **`If-Match`** on **`PATCH`**/**`transitions`**, **`ETag`** on **GET by id**).
- [ ] At least one automated test per major error class listed in **1a** `test-plan.md` where the server supports simulation (e.g. **503** via test double or config).

## 11. Test Requirements

- Integration tests asserting **`Content-Type`** and JSON shape for errors.
- Optional CI step: Spectral on **`openapi-1a.yaml`**.

## 12. Files Expected to Change

- **`src/main/java/**`** `@ControllerAdvice` or equivalent, **`specs/openapi/openapi-1a.yaml`**, **`src/test/java/**`**, CI workflow if Spectral added.

## 13. Implementation Notes

- List endpoint must remain documented without **`source`** query (**1b** adds it in **1b** OpenAPI; Story **13**).

## 14. Human Review Checklist

- [ ] OpenAPI matches live controllers (paths, methods, headers).
- [ ] No stack traces leak in JSON.

## 15. Completion Notes

*(Fill when implemented.)*
