# OpenAPI

| File | Phase | Normative docs |
|------|--------|----------------|
| `openapi-1a.yaml` | **1a** | `phases/phase-1a-monolith-core/api-contract.md` |
| `openapi-1b.yaml` | **1b** (ingest + extended incident reads) | `phases/phase-1b-signal-ingest/api-contract.md` |

**Full API (after 1b):** merge **`openapi-1a.yaml`** + **`openapi-1b.yaml`** in your tooling (or a future **`openapi-v1-combined.yaml`**). **`1b`** already defines **`GET /api/v1/incidents`** and **`GET /api/v1/incidents/{id}`**; **`1a`** supplies **`POST`/`PATCH`** incidents, **`transitions`**, and **`/actuator/health`**. Resolve duplicate paths by preferring **`1b`** definitions for **`GET /api/v1/incidents`** and **`GET /api/v1/incidents/{id}`** when both files define them.

**Phase 1a gate:** `openapi-1a.yaml` is complete and matches controllers (including **`ETag`** on **`GET .../{id}`** and **`503`** where the contract applies). **Phase 1b gate:** `openapi-1b.yaml` matches **`phase-1b-signal-ingest/api-contract.md`** for ingest, list **`source`**, get-by-id extensions, and merged-document rules above.
