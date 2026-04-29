# API contract — Phase 1a (incidents + health only)

**Scope:** This document defines **only** resources shipped in **Phase 1a**. It does **not** include signal ingest. **Phase 1b** extends the **same** incident read model in `../phase-1b-signal-ingest/api-contract.md#extensions-to-incident-api`.

**Normative:** JSON under **`/api/v1`**. Times **ISO-8601 UTC**.

## Conventions (1a)

### Versioning

- Path prefix **`/api/v1`**.

### Errors

**RFC 7807** Problem Details (`application/problem+json`): `type`, `title`, `status`, `detail`, `instance`. No stack traces in responses.

**Validation:** minimum **`400`** with `detail` for bad JSON; **`422`** optional with `errors[]`.

### Pagination (list)

- `page` (0-based, default `0`), `size` (default `20`, max `100`).
- **Unknown query key** → **`400`** (includes **`source`** and any other key not listed for this phase). Bad `size` / `page` → **`400`**.

### Correlation

- **`X-Request-Id`**: echo or generate.

### Request size (1a)

- **`POST/PATCH` incidents:** max **1 MiB**; → **`413`** or **`400`** (document).

### Rate limiting (1a, optional)

- Document if not implemented. Recommended: **~120 mutating req/min** per IP for demo.

### JSON and `Content-Type` (1a)

- **`POST`**, **`PATCH`**, **`POST .../transitions`**: body MUST be **`application/json`**. Wrong or missing JSON `Content-Type` where a JSON body is expected → **`415`** (or **`400`** if server documents unified validation).
- **`PATCH`** with **empty body** or **non-object JSON** (when object expected) → **`400`**.

### Optimistic concurrency (**required**)

- **`GET /api/v1/incidents/{id}`** returns a **strong `ETag`** (RFC 9110): the entity-tag is a **quoted** opaque string derived from `version`. Normative wire format: ASCII **`"`** + decimal string of `version` + **`"`** (example: **`ETag: "7"`**). Same token is sent on **`If-Match`** for **`PATCH`** and **`POST .../transitions`**; mismatch → **`412`**. **`If-Match: *`** is **not** supported unless documented in an ADR.

### Idempotency (1a)

- No server-side `Idempotency-Key` required for 1a (optional client retry handling only).

---

## `GET /api/v1/incidents`

**`IncidentSummary`** in `items[]`:

| Field | Type |
|-------|------|
| `id` | UUID |
| `title` | string |
| `status` | `DRAFT` \| `OPEN` \| `CLOSED` \| `CANCELLED` |
| `severity` | `SEV1`…`SEV4` |
| `source` | In **1a**, always **`MANUAL`** (column default / enforcement). |
| `createdAt` | instant |
| `updatedAt` | instant |
| `version` | long |

**Query filters:** `status` (comma-separated), `sort` (`createdAt,asc` \| `createdAt,desc`).

**No `source` query in 1a:** list returns **manual** incidents only (`source` on each item is always **`MANUAL`**). Filtering by **`SIGNAL`** is **`phase-1b-signal-ingest/api-contract.md`**.

**`status`:** trim tokens; **empty** token, **duplicates** after trim, or **unknown** enum → **`400`**.

**Response:** paged JSON `{ "items", "page", "size", "totalElements", "totalPages" }`.

**Availability:** if the persistence layer is unavailable for the list path, respond **`503`** (preferred) or **`500`** — document which.

---

## `POST /api/v1/incidents`

| Field | Required | Rules |
|-------|----------|--------|
| `title` | yes | 1–200 chars trimmed |
| `description` | no | max 32_768 |
| `severity` | yes | SEV1…SEV4 |

**Creates `DRAFT` only;** no field to request `OPEN` on create. **`201`** + full `Incident` body (1a shape below).

---

## `GET /api/v1/incidents/{id}`

**`Incident` (1a)** — all summary fields plus:

| Field | Type | Notes (1a) |
|-------|------|------------|
| `description` | string \| null | |
| `transitionReason` | string \| null | optional, last transition; **1a**: not stored on `incidents` — **always `null`** until a later schema/API decision adds persistence |

**No** `telemetryContext`, `createdByRuleId`, or `signalFingerprint` in **1a** (they appear in 1b extension doc).

**`404`** if missing.

**Availability:** if the persistence layer is unavailable, respond **`503`** (preferred) or **`500`** — same policy as list; document which.

---

## `PATCH /api/v1/incidents/{id}`

- **`If-Match`** required.
- JSON body must be a **non-empty object** with at least one of `title`, `description`, `severity` (all optional keys absent or empty patch → **`400`**).
- `title`, `description`, `severity` when status `DRAFT` or `OPEN`; else **`409`**.

---

## `POST /api/v1/incidents/{id}/transitions`

- **`If-Match`** required.
- Body: `{ "to": "OPEN" \| "CLOSED" \| "CANCELLED", "reason"?: string ≤500 }`
- **JSON:** must be **`application/json`**; malformed JSON, **wrong `to` enum**, missing **`to`**, or **`reason`** over length → **`400`**.
- **Transitions:** `DRAFT`→`OPEN`/`CANCELLED`; `OPEN`→`CLOSED`/`CANCELLED`; none from `CLOSED`/`CANCELLED`.
- **Errors:** `400`, `404`, `409`, `412`.

---

## Actuator (non-versioned)

- `GET /actuator/health` — liveness.
- `GET /actuator/health/readiness` — DB (recommended for Docker).
- **Restrict** exposure (`health`, `readiness` only) per README.

---

## OpenAPI

- **Artifact:** `specs/openapi/openapi-1a.yaml` (normative for **1a**; expand schemas in M3–M4). **`GET /api/v1/incidents/{id}`** MUST document **`ETag`** response header and **`503`** when applicable (see `openapi-1a.yaml` vs this contract at **1a** gate).
- **CI:** Spectral on `openapi-1a.yaml` optional, recommended.
